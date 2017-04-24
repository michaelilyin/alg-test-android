package ru.michaelilyin.alg;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private TextView tv;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Example of a call to a native method
        tv = (TextView) view.findViewById(R.id.log);
        Button btnTest = (Button) view.findViewById(R.id.test);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTest();
            }
        });
    }

    private void startTest() {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                StringBuilder builder = new StringBuilder("Test started\n");
                setLog(builder.toString());
                ArrayComputation computation = new ArrayComputation();
                for (int i = 10000000; i < 1000000000; i*=2) {
                    try {
                        builder.append("Test for ").append(i).append("\n");
                        setLog(builder.toString());
                        double[] array = computation.genArray(i);

                        long start = 0;
                        double res = 0;
                        long stop = 0;

                        builder.append("Test native ");
                        setLog(builder.toString());
                        start = SystemClock.currentThreadTimeMillis();
                        res = computation.sumArrayNative(array);
                        stop = SystemClock.currentThreadTimeMillis();
                        builder.append("elapsed: ").append(stop - start).append(" with result ").append(res).append("\n");
                        setLog(builder.toString());

                        builder.append("Test java ");
                        setLog(builder.toString());
                        start = SystemClock.currentThreadTimeMillis();
                        res = computation.sumArrayJava(array);
                        stop = SystemClock.currentThreadTimeMillis();
                        builder.append("elapsed: ").append(stop - start).append(" with result ").append(res).append("\n");
                        setLog(builder.toString());

                        builder.append("Test finished for ").append(i).append("\n================\n\n");
                        setLog(builder.toString());

                        array = null;
                        System.gc();
                        Thread.sleep(1000);
                    } catch (Error e) {
                        builder.append("Error: ").append(e.getMessage());
                        setLog(builder.toString());
                        break;
                    } catch (Exception e) {
                        builder.append("Exception: ").append(e.getMessage());
                        setLog(builder.toString());
                    }
                }
                builder.append("Test finished");
                setLog(builder.toString());
                return null;
            }
        };
        task.execute((Void) null);
    }

    private void setLog(final String s) {
        tv.post(new Runnable() {
            @Override
            public void run() {
                tv.setText(s);
                tv.scrollTo(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
