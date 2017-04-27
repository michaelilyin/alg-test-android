#ifndef ALG_CL_TEST_H
#define ALG_CL_TEST_H

#include <jni.h>

#include <android/log.h>

#define LOGINFO(x...) __android_log_print(ANDROID_LOG_INFO,"CL_TEST",x)
#define LOGERR(x...) __android_log_print(ANDROID_LOG_ERROR,"CL_TEST",x)

extern "C" {

jint 	Java_ru_michaelilyin_alg1_CLUtils_initCL(JNIEnv *env, jobject obj);

jint 	Java_ru_michaelilyin_alg1_CLUtils_createProg(JNIEnv *env, jobject obj,
                                                          jstring progName, jobject progSrcBuf);

jint	Java_ru_michaelilyin_alg1_CLUtils_createKernel(JNIEnv *env, jobject obj);

jint 	Java_ru_michaelilyin_alg1_CLUtils_createCmdQueue(JNIEnv *env, jobject obj);

jfloat 	Java_ru_michaelilyin_alg1_CLUtils_setKernelArgs(JNIEnv *env, jobject obj,
                                                               jint width, jint height,
                                                               jobject inputImgBuf);

jfloat 	Java_ru_michaelilyin_alg1_CLUtils_executeKernel(JNIEnv *env, jobject obj);

jfloat 	Java_ru_michaelilyin_alg1_CLUtils_getResultImg(JNIEnv *env, jobject obj,
                                                              jobject outputImgBuf);

void 	Java_ru_michaelilyin_alg1_CLUtils_dealloc(JNIEnv *env, jobject obj);


void	Java_ru_michaelilyin_alg1_CLUtils_printInfo(JNIEnv *env, jobject obj);

}

#endif
