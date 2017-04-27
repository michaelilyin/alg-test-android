package ru.michaelilyin.alg1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.support.v7.app.AppCompatActivity;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private LinearLayout lt;

    private class ImagePair {
        String name;
        Bitmap image;

        public ImagePair(String name, int image) {
            this.name = name;
            this.image = BitmapFactory.decodeResource(getResources(), image);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lt = (LinearLayout) findViewById(R.id.lt);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ArrayList<ImagePair> images = new ArrayList<>();
                images.add(new ImagePair("128", R.mipmap.p128));
                images.add(new ImagePair("256", R.mipmap.p256));
                images.add(new ImagePair("512", R.mipmap.p512));
//                images.add(new ImagePair("1024", R.mipmap.p1024));
//                images.add(new ImagePair("2048", R.mipmap.p2048));

                long start;
                long stop;
                float duration;

                RenderScript rs = initRS();
                initCL();
                try {
                    for (ImagePair image : images) {
                        Log.i(TAG, "Test for " + image.name);

                        Log.i(TAG, "Test RS");
                        duration = testRS(rs, image);
                        Log.i(TAG, "Test RS completed: " + duration);

                        Log.i(TAG, "Test OCL");
                        duration = testOCL(image);
                        Log.i(TAG, "Test OCL completed: " + duration);

                        Log.i(TAG, "Test for " + image.name + " completed");
                    }
                } finally {
                    CLUtils.dealloc();
                }

                return null;
            }
        }.execute((Void) null);
    }

    private void initCL() {
        if (CLUtils.initCL() == 0) {
            Log.i(TAG, "CL init successful");
        } else {
            Log.e(TAG, "CL init error");
        }

        // ClJNIGlue.printInfo();

        ByteBuffer progSrc;

            Log.i(TAG, "Loading program source code in file " + "gauss_5x5.cl");
            progSrc = CLUtils.getProgramBuffer(getBaseContext(), "gauss_5x5.cl");
            Log.i(TAG, "Loaded CL program source code containing " + progSrc.capacity() + " bytes");

//            progSrc = ByteBuffer.allocateDirect(clKernelProgSrc.length());
//            progSrc.put(clKernelProgSrc.getBytes());
//            progSrc.rewind();
//            Log.i(TAG, "Using generated kernel source of size " + progSrc.capacity() + " bytes");
//        }

        if (CLUtils.createProg("cl_filter", progSrc) == 0) {
            Log.i(TAG, "CL createProg successful");
        } else {
            Log.e(TAG, "CL createProg error");
        }

        if (CLUtils.createKernel() == 0) {
            Log.i(TAG, "CL createKernel successful");
        } else {
            Log.e(TAG, "CL createKernel error");
        }

        if (CLUtils.createCmdQueue() == 0) {
            Log.i(TAG, "CL createCmdQueue successful");
        } else {
            Log.e(TAG, "CL createCmdQueue error");
        }
    }

    private RenderScript initRS() {
        return RenderScript.create(MainActivity.this);
    }

    private float testOCL(ImagePair image) {
        int imgWidth = image.image.getWidth();
        int imgHeight = image.image.getHeight();
        int imgChan = image.image.getRowBytes() / imgWidth;
        int imgByteSize = imgWidth * imgHeight * imgChan;
        ByteBuffer outputImgBuf = null;

        ByteBuffer inputImgBuf = ByteBuffer.allocateDirect(imgByteSize);
        image.image.copyPixelsToBuffer(inputImgBuf);
        inputImgBuf.rewind();

        // allocate output buffer
        outputImgBuf = ByteBuffer.allocateDirect(imgByteSize);

        float pushMsSum = 0.0f;
        float execMsSum = 0.0f;
        float pullMsSum = 0.0f;
        int depth = 1;
        for (int test = 0; test < depth; test++) {
            float pushMs = CLUtils.setKernelArgs(imgWidth, imgHeight, inputImgBuf);
            if (pushMs < 0.0f) {
                Log.e(TAG, "CL createKernel error");
                return -1;
            }
            pushMsSum += pushMs;

            float execMs = CLUtils.executeKernel();
            if (execMs < 0.0f) {
                Log.e(TAG, "CL executeKernel error");
                return -1;
            }
            execMsSum += execMs;

            float pullMs = CLUtils.getResultImg(outputImgBuf);
            if (pullMs < 0.0f)  {
                Log.e(TAG, "CL getResultImg error");
                return -1;
            }
            pullMsSum += pullMs;
        }

        float pushMsAvg = pushMsSum / (float)depth;
        float execMsAvg = execMsSum / (float)depth;
        float pullMsAvg = pullMsSum / (float)depth;

        Bitmap outputBitmap = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
        outputBitmap.copyPixelsFromBuffer(outputImgBuf);
        showRes(outputBitmap);

        return pushMsAvg + execMsAvg + pullMsAvg;
    }

    private float testRS(RenderScript rs, final ImagePair image) {
        long start;
        long stop;
        long duration = 0;

        ScriptC_gauss_for_5x5 gauss = new ScriptC_gauss_for_5x5(rs);
        Allocation in = Allocation.createFromBitmap(rs, image.image,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
        Bitmap outBm = Bitmap.createBitmap(image.image.getWidth(),
                image.image.getHeight(), image.image.getConfig());
        int depth = 1;
        for (int i = 0; i < depth; i++) {
            Allocation out = Allocation.createFromBitmap(rs, outBm,
                    Allocation.MipmapControl.MIPMAP_NONE,
                    Allocation.USAGE_SCRIPT);
            start = System.nanoTime();
            gauss.set_in(in);
            gauss.set_size(image.image.getWidth());
            gauss.forEach_make_gauss(out);
            stop = System.nanoTime();
            duration += (float)((double)(stop - start));
            out.copyTo(outBm);
            in = Allocation.createFromBitmap(rs, image.image,
                    Allocation.MipmapControl.MIPMAP_NONE,
                    Allocation.USAGE_SCRIPT);
        }
        showRes(outBm);
        return duration;
    }

    private void showRes(final Bitmap bitmap) {
        this.lt.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout layout = new LinearLayout(MainActivity.this);
                layout.setOrientation(LinearLayout.HORIZONTAL);

                {
//                    ImageView imageView = new ImageView(MainActivity.this);
    //                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), image.image);
    //                imageView.setImageDrawable(bitmapDrawable);
    //                layout.addView(imageView);
                }
                {
                    ImageView imageView = new ImageView(MainActivity.this);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                    imageView.setImageDrawable(bitmapDrawable);
                    layout.addView(imageView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                }

                lt.addView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
        });
    }

}
