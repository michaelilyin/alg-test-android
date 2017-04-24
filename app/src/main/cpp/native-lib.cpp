#include <jni.h>
#include <string>

extern "C"
jstring
Java_ru_michaelilyin_alg_MainActivityFragment_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
jdouble Java_ru_michaelilyin_alg_ArrayComputation_sumArrayNative(JNIEnv *env, jobject, jdoubleArray array) {
    double result = 0.0;
    int size = env->GetArrayLength(array);
    double *body = env->GetDoubleArrayElements(array, 0);
    for (int i = 0; i < size; i++) {
        if (i > 0 && i < size - 1) {
            result += body[i-1] * body[i] * body[i+1];
        } else {
            result += body[i];
        }
    }
    env->ReleaseDoubleArrayElements(array, body, 0);
    return result;
}