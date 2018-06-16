package com.jwoolston.android.libusb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@RequiresDevice
@MediumTest
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UsbManagerTest extends USBTestCase {

    private static final String TAG = "UsbManagerTest";

    @Ignore("No way to ensure we didn't already have permission.")
    @Test
    public void registerDevice_1_PermissionDenied() {
        Log.d(TAG, "Executing permission denied registration test.");
        Context context = InstrumentationRegistry.getTargetContext();
        android.hardware.usb.UsbManager androidManager = (android.hardware.usb.UsbManager) context.getSystemService
                (Context.USB_SERVICE);
        final UsbManager _manager = new UsbManager(context);
        android.hardware.usb.UsbDevice device = findDevice(androidManager);
        requestPermissions(context, androidManager, device, new DeviceAvailable() {
            @Override public void onDeviceAvailable(@NonNull android.hardware.usb.UsbDevice device) {
                _manager.destroy();
                assertNull("onDeviceAvailable() should have never been called.", device);
            }

            @Override public void onDeviceDenied(@NonNull android.hardware.usb.UsbDevice device) {
                boolean thrown = false;
                try {
                    _manager.registerDevice(device);
                } catch (IllegalAccessException e) {
                    thrown = true;
                }
                _manager.destroy();
                assertTrue("Exception was not thrown as expected from attempting to register.", thrown);
            }
        });
        denyPermissions();
    }

    @Test
    public void registerDevice_2_PermissionGranted() {
        Log.d(TAG, "Executing permission granted registration test.");
        Context context = InstrumentationRegistry.getTargetContext();
        android.hardware.usb.UsbManager androidManager = (android.hardware.usb.UsbManager) context.getSystemService
                (Context.USB_SERVICE);
        final UsbManager _manager = new UsbManager(context);
        android.hardware.usb.UsbDevice device = findDevice(androidManager);
        requestPermissions(context, androidManager, device, new DeviceAvailable() {
            @Override public void onDeviceAvailable(@NonNull android.hardware.usb.UsbDevice device) {
                try {
                    UsbDeviceConnection deviceConnection = _manager.registerDevice(device);
                    _manager.destroy();
                    assertNotNull("Failed to register USB device.", deviceConnection);
                } catch (IllegalAccessException e) {
                    _manager.destroy();
                    assertNull("Registration threw exception.", e);
                }
            }

            @Override public void onDeviceDenied(@NonNull android.hardware.usb.UsbDevice device) {
                _manager.destroy();
                assertNull("Permission for device was denied.", device);
            }
        });
        allowPermissions();
    }

    @Test
    public void registerDevice_3_Double() {
        Log.d(TAG, "Executing double registration test.");
        Context context = InstrumentationRegistry.getTargetContext();
        android.hardware.usb.UsbManager androidManager = (android.hardware.usb.UsbManager) context.getSystemService
                (Context.USB_SERVICE);
        final UsbManager _manager = new UsbManager(context);
        android.hardware.usb.UsbDevice device = findDevice(androidManager);
        requestPermissions(context, androidManager, device, new DeviceAvailable() {
            @Override public void onDeviceAvailable(@NonNull android.hardware.usb.UsbDevice device) {
                try {
                    UsbDeviceConnection deviceConnection = _manager.registerDevice(device);
                    assertNotNull("Failed to register USB device.", deviceConnection);
                    UsbDeviceConnection deviceConnection2 = _manager.registerDevice(device);
                    assertEquals("Registering the same device should have returned the same device.",
                                 deviceConnection, deviceConnection2);
                    assertSame("Registering the same device should have returned identical references.",
                               deviceConnection, deviceConnection2);
                } catch (IllegalAccessException e) {
                    _manager.destroy();
                    assertNull("Registration threw exception.", e);
                }
            }

            @Override public void onDeviceDenied(@NonNull android.hardware.usb.UsbDevice device) {
                _manager.destroy();
                assertNull("Permission for device was denied.", device);
            }
        });
        allowPermissions();
    }
}