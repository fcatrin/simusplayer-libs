/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class xtvapps_simusplayer_NativeInterface */

#ifndef _Included_xtvapps_simusplayer_NativeInterface
#define _Included_xtvapps_simusplayer_NativeInterface
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     xtvapps_simusplayer_NativeInterface
 * Method:    alsaInit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_xtvapps_simusplayer_NativeInterface_alsaInit
  (JNIEnv *, jclass);

/*
 * Class:     xtvapps_simusplayer_NativeInterface
 * Method:    alsaDone
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_xtvapps_simusplayer_NativeInterface_alsaDone
  (JNIEnv *, jclass);

/*
 * Class:     xtvapps_simusplayer_NativeInterface
 * Method:    alsaGetPortsCount
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_NativeInterface_alsaGetPortsCount
  (JNIEnv *, jclass);

/*
 * Class:     xtvapps_simusplayer_NativeInterface
 * Method:    alsaGetPortIds
 * Signature: (I)[I
 */
JNIEXPORT jintArray JNICALL Java_xtvapps_simusplayer_NativeInterface_alsaGetPortIds
  (JNIEnv *, jclass, jint);

/*
 * Class:     xtvapps_simusplayer_NativeInterface
 * Method:    alsaGetPortNames
 * Signature: (I)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_xtvapps_simusplayer_NativeInterface_alsaGetPortNames
  (JNIEnv *, jclass, jint);

/*
 * Class:     xtvapps_simusplayer_NativeInterface
 * Method:    alsaConnectPort
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_xtvapps_simusplayer_NativeInterface_alsaConnectPort
  (JNIEnv *, jclass, jint);

/*
 * Class:     xtvapps_simusplayer_NativeInterface
 * Method:    midiLoad
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_NativeInterface_midiLoad
  (JNIEnv *, jclass, jstring);

/*
 * Class:     xtvapps_simusplayer_NativeInterface
 * Method:    midiUnload
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_xtvapps_simusplayer_NativeInterface_midiUnload
  (JNIEnv *, jclass, jint);

/*
 * Class:     xtvapps_simusplayer_NativeInterface
 * Method:    midiGetTracksCount
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_NativeInterface_midiGetTracksCount
  (JNIEnv *, jclass, jint);

/*
 * Class:     xtvapps_simusplayer_NativeInterface
 * Method:    midiGetTrackName
 * Signature: (II)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_xtvapps_simusplayer_NativeInterface_midiGetTrackName
  (JNIEnv *, jclass, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
