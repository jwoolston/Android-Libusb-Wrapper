package com.jwoolston.libusb.async;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface BulkTransferCallback {

    void onBulkTransferComplete(@Nullable ByteBuffer data, int result) throws IOException;
}
