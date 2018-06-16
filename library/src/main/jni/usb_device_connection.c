//
// Created by Jared Woolston (Jared.Woolston@gmail.com)
//

#include <unistd.h>
#include <string.h>
#include "common.h"

JNIEXPORT jbyteArray JNICALL
Java_com_jwoolston_android_libusb_UsbDeviceConnection_nativeGetRawDescriptor(JNIEnv *env, jobject instance, jint fd) {
    char buffer[16384];
    if (fd < 0) return NULL;
    lseek(fd, 0, SEEK_SET);
    int length = read(fd, buffer, sizeof(buffer));
    if (length < 0) return NULL;
    jbyteArray ret = (*env)->NewByteArray(env, length);
    if (ret) {
        jbyte* bytes = (jbyte*)(*env)->GetPrimitiveArrayCritical(env, ret, 0);
        if (bytes) {
            memcpy(bytes, buffer, length);
            (*env)->ReleasePrimitiveArrayCritical(env, ret, bytes, 0);
        }
    }
    return ret;
}