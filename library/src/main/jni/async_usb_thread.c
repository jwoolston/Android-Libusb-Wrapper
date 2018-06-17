//
// Created by Jared Woolston (Jared.Woolston@gmail.com)
//

#include <common.h>

#define  LOG_TAG    "AsyncUsbThread-Native"

JNIEXPORT void JNICALL
Java_com_jwoolston_android_libusb_AsyncUSBThread_nativeHandleEvents(JNIEnv *env, jclass type, jobject context) {
    struct libusb_context *ctx = (libusb_context *) (*env)->GetDirectBufferAddress(env, context);
    libusb_handle_events(ctx);
}