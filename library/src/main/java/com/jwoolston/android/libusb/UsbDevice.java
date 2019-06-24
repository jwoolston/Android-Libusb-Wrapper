package com.jwoolston.android.libusb;

/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.os.Parcel;
import android.os.Parcelable;

import com.jwoolston.android.libusb.util.Preconditions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * This class represents a USB device attached to the android device with the android device acting as the USB host.
 * Each device contains one or more {@link UsbInterface}s, each of which contains a number of {@link UsbEndpoint}s
 * (the channels via which data is transmitted over USB).
 * <p>
 * <p> This class contains information (along with {@link UsbInterface} and {@link UsbEndpoint}) that describes the
 * capabilities of the USB device. To communicate with the device, you open a {@link UsbDeviceConnection} for the device
 * and use {@link UsbRequest} to send and receive data on an endpoint. {@link UsbDeviceConnection#controlTransfer} is
 * used for control requests on endpoint zero.
 * <p>
 */
public class UsbDevice implements Parcelable {

    @NotNull
    private final android.hardware.usb.UsbDevice device;

    @NotNull
    private final String name;
    @Nullable
    private final String manufacturerName;
    @Nullable
    private final String productName;
    @NotNull
    private final String version;
    @NotNull
    private final String serialNumber;
    @NotNull
    private final LibusbSpeed speed;

    private final int vendorId;
    private final int productId;
    private final int deviceClass;
    private final int subclass;
    private final int protocol;

    private final int fileDescriptor;

    @NotNull
    private final ByteBuffer nativeObject;

    /**
     * All configurations for this device, only null during creation
     */
    @Nullable
    private UsbConfiguration[] configurations;
    /**
     * All interfaces on the device. Initialized on first call to getInterfaceList
     */
    @Nullable
    private UsbInterface[]     interfaces;

    public int getFileDescriptor() {
        return fileDescriptor;
    }

    @NotNull
    public android.hardware.usb.UsbDevice getAndroidDevice() {
        return device;
    }

    /**
     * Returns the name of the device. In the standard implementation, this is the path of the device file for the
     * device in the usbfs file system.
     *
     * @return the device name
     */
    @NotNull
    public String getDeviceName() {
        return name;
    }

    /**
     * Returns the manufacturer name of the device.
     *
     * @return the manufacturer name, or {@code null} if the property could not be read
     */
    @Nullable
    public String getManufacturerName() {
        return manufacturerName;
    }

    /**
     * Returns the product name of the device.
     *
     * @return the product name, or {@code null} if the property could not be read
     */
    @Nullable
    public String getProductName() {
        return productName;
    }

    /**
     * Returns the version number of the device.
     *
     * @return the device version
     */
    @NotNull
    public String getVersion() {
        return version;
    }

    /**
     * Returns the serial number of the device.
     *
     * @return the serial number name, or {@code null} if the property could not be read
     */
    @NotNull
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Returns the connection speed of the device.
     *
     * @return the connection speed.
     */
    @NotNull
    public LibusbSpeed getDeviceSpeed() {
        return speed;
    }

    /**
     * Returns a unique integer ID for the device. This is a convenience for clients that want to use an integer to
     * represent the device, rather than the device name. IDs are not persistent across USB disconnects.
     *
     * @return the device ID
     */
    public int getDeviceId() {
        return device.getDeviceId();
    }

    /**
     * Returns a vendor ID for the device.
     *
     * @return the device vendor ID
     */
    public int getVendorId() {
        return vendorId;
    }

    /**
     * Returns a product ID for the device.
     *
     * @return the device product ID
     */
    public int getProductId() {
        return productId;
    }

    /**
     * Returns the devices's class field.
     * Some useful constants for USB device classes can be found in {@link UsbConstants}.
     *
     * @return the devices's class
     */
    public int getDeviceClass() {
        return deviceClass;
    }

    /**
     * Returns the device's subclass field.
     *
     * @return the device's subclass
     */
    public int getDeviceSubclass() {
        return subclass;
    }

    /**
     * Returns the device's protocol field.
     *
     * @return the device's protocol
     */
    public int getDeviceProtocol() {
        return protocol;
    }

    /**
     * Returns the number of {@link UsbConfiguration}s this device contains.
     *
     * @return the number of configurations
     */
    public int getConfigurationCount() {
        return configurations.length;
    }

    /**
     * Returns the {@link UsbConfiguration} at the given index.
     *
     * @return the configuration
     */
    @NotNull
    public UsbConfiguration getConfiguration(int index) {
        return (UsbConfiguration) configurations[index];
    }

    @NotNull
    private UsbInterface[] getInterfaceList() {
        if (interfaces == null) {
            int configurationCount = configurations.length;
            int interfaceCount = 0;
            for (int i = 0; i < configurationCount; i++) {
                UsbConfiguration configuration = configurations[i];
                interfaceCount += configuration.getInterfaceCount();
            }
            interfaces = new UsbInterface[interfaceCount];
            int offset = 0;
            for (int i = 0; i < configurationCount; i++) {
                UsbConfiguration configuration = configurations[i];
                interfaceCount = configuration.getInterfaceCount();
                for (int j = 0; j < interfaceCount; j++) {
                    interfaces[offset++] = configuration.getInterface(j);
                }
            }
        }
        return interfaces;
    }

    /**
     * Returns the number of {@link UsbInterface}s this device contains. For devices with multiple configurations,
     * you will probably want to use {@link UsbConfiguration#getInterfaceCount} instead.
     *
     * @return the number of interfaces
     */
    public int getInterfaceCount() {
        return getInterfaceList().length;
    }

    /**
     * Returns the {@link UsbInterface} at the given index. For devices with multiple configurations, you will
     * probably want to use {@link UsbConfiguration#getInterface} instead.
     *
     * @return the interface
     */
    public @NotNull
    UsbInterface getInterface(int index) {
        return getInterfaceList()[index];
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UsbDevice) {
            return ((UsbDevice) o).name.equals(name);
        } else {
            return (o instanceof String && ((String) o).equals(name));
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("UsbDevice[name=" + name +
                                                  ",vendorId=" + vendorId + ",productId=" + productId +
                                                  ",deviceClass=" + deviceClass + ",subclass=" + subclass
                                                  + ",protocol=" + protocol +
                                                  ",manufacturerName=" + manufacturerName + ",productName="
                                                  + productName
                                                  +
                                                  ",version=" + version + ",serialNumber=" + serialNumber
                                                  + ",configurations=[");
        if (configurations != null) {
            for (UsbConfiguration configuration : configurations) {
                builder.append("\n");
                builder.append(configuration.toString());
            }
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(nativeGetPointerFromNativeObject(getNativeObject()));
        dest.writeParcelable(device, flags);
        dest.writeString(name);
        dest.writeString(manufacturerName);
        dest.writeString(productName);
        dest.writeString(version);
        dest.writeString(serialNumber);
        dest.writeInt(speed.code);
        dest.writeInt(vendorId);
        dest.writeInt(productId);
        dest.writeInt(deviceClass);
        dest.writeInt(subclass);
        dest.writeInt(protocol);
        dest.writeInt(fileDescriptor);
        dest.writeTypedArray(configurations, flags);
        dest.writeTypedArray(interfaces, flags);
    }

    public static final Creator<UsbDevice> CREATOR = new Creator<UsbDevice>() {
        @Override
        public UsbDevice createFromParcel(Parcel in) {
            return new UsbDevice(in);
        }

        @Override
        public UsbDevice[] newArray(int size) {
            return new UsbDevice[size];
        }
    };

    @NonNull
    static UsbDevice fromAndroidDevice(@NotNull LibUsbContext context, @NotNull android.hardware.usb.UsbDevice device,
                                       @NotNull android.hardware.usb.UsbDeviceConnection connection) {
        return new UsbDevice(connection, device, wrapDevice(context.getNativeObject(), connection.getFileDescriptor()));
    }

    private UsbDevice(@NotNull android.hardware.usb.UsbDeviceConnection connection,
                      @NotNull android.hardware.usb.UsbDevice device, @Nullable ByteBuffer nativeObject) {
        Preconditions.checkNotNull(nativeObject, "UsbDevice initialization failed.");
        this.nativeObject = nativeObject;
        this.device = device;
        name = device.getDeviceName();
        vendorId = device.getVendorId();
        productId = device.getProductId();
        deviceClass = device.getDeviceClass();
        subclass = device.getDeviceSubclass();
        protocol = device.getDeviceProtocol();

        LibUsbDeviceDescriptor descriptor = LibUsbDeviceDescriptor.getDeviceDescriptor(this);
        manufacturerName = nativeGetManufacturerString(nativeObject, descriptor.getNativeObject());
        productName = nativeGetProductNameString(nativeObject, descriptor.getNativeObject());
        version = nativeGetDeviceVersion(descriptor.getNativeObject());
        serialNumber = connection.getSerial();
        speed = LibusbSpeed.fromNative(nativeGetDeviceSpeed(nativeObject, descriptor.getNativeObject()));

        fileDescriptor = connection.getFileDescriptor();
    }

    protected UsbDevice(Parcel in) {
        final ByteBuffer buffer = nativeGetNativeObjectFromPointer(in.readLong());
        if (buffer == null) {
            throw new IllegalStateException("Received a null reference for the native object. Creation from "
                                            + "parcel failed.");
        }
        nativeObject = buffer;
        device = in.readParcelable(android.hardware.usb.UsbDevice.class.getClassLoader());
        name = in.readString();
        manufacturerName = in.readString();
        productName = in.readString();
        version = in.readString();
        serialNumber = in.readString();
        speed = LibusbSpeed.fromNative(in.readInt());
        vendorId = in.readInt();
        productId = in.readInt();
        deviceClass = in.readInt();
        subclass = in.readInt();
        protocol = in.readInt();
        fileDescriptor = in.readInt();
        configurations = in.createTypedArray(UsbConfiguration.CREATOR);
        interfaces = in.createTypedArray(UsbInterface.CREATOR);
    }

    /**
     * Retrieves the {@link ByteBuffer} pointing to a {@code libusb_device_handle} instance in native.
     *
     * @return The {@link ByteBuffer} pointing to a {@code libusb_device_handle} instance in native.
     */
    @NotNull
    public ByteBuffer getNativeObject() {
        return nativeObject;
    }

    /**
     * Populates the internal data structures of this device which include the {@link UsbConfiguration}s,
     * the {@link UsbInterface}s and {@link UsbEndpoint}s.
     */
    void populate() {
        final int numConfigurations = nativeGetConfigurationCount(getNativeObject());
        final UsbConfiguration[] configurations = new UsbConfiguration[numConfigurations];
        for (int i = 0; i < numConfigurations; ++i) {
            configurations[i] = UsbConfiguration.fromNativeObject(this, i);
        }
        setConfigurations(configurations);
    }

    /**
     * Sets the available configurations for this device. Only expected to be called by the {@link UsbDevice#populate()}
     * method.
     *
     * @param configuration Array of {@link UsbConfiguration}s. Must not be {@code null} or contain {@code null}s.
     */
    void setConfigurations(@NotNull UsbConfiguration[] configuration) {
        configurations = Preconditions.checkArrayElementsNotNull(configuration, "configuration");
    }

    /**
     * Retrieves a string descriptor from the device.
     *
     * @param device {@link ByteBuffer} pointing to a {@code libusb_device_handle} instance in native. Provided by
     *               {@link UsbDevice#getNativeObject()}.
     * @param index  {@code int} The string index to retrieve. A value of 0 will  cause {@code null} to be returned.
     *
     * @return {@link String} The descriptor or null if one is not present on the device.
     */
    @Nullable
    static native String nativeGetStringDescriptor(@NotNull ByteBuffer device, int index);

    /**
     * Creates a {@code libusb_device_handle} native instance for the give file descriptor. This file descriptor must be
     * provided by {@link android.hardware.usb.UsbDeviceConnection#getFileDescriptor()} in order to have proper
     * permissions.
     *
     * @param context {@link ByteBuffer} pointing to a {@code libusb_context} instance in native.
     * @param fd      {@code int} The file descriptor for the opened device.
     *
     * @return {@link ByteBuffer} pointing to a {@code libusb_device_handle} instance in native, or {@code null} if a
     * failure occurred.
     */
    @Nullable
    private static native ByteBuffer wrapDevice(@NotNull ByteBuffer context, int fd);

    /**
     * Retrieves the manufacturer name string from the device.
     *
     * @param device     {@link ByteBuffer} pointing to a {@code libusb_device_handle} instance in native. Provided
     *                   by {@link UsbDevice#getNativeObject()}.
     * @param descriptor {@link ByteBuffer} pointing to a {@code libusb_device_descriptor} instanace in native.
     *                   Provided by {@link LibUsbDeviceDescriptor#getNativeObject()}.
     *
     * @return {@link String} The device manufacturer name.
     */
    private native String nativeGetManufacturerString(@NotNull ByteBuffer device, @NotNull ByteBuffer descriptor);

    /**
     * Retrieves the product name string from the device.
     *
     * @param device     {@link ByteBuffer} pointing to a {@code libusb_device_handle} instance in native. Provided
     *                   by {@link UsbDevice#getNativeObject()}.
     * @param descriptor {@link ByteBuffer} pointing to a {@code libusb_device_descriptor} instanace in native.
     *                   Provided by {@link LibUsbDeviceDescriptor#getNativeObject()}.
     *
     * @return {@link String} The device product name.
     */
    private native String nativeGetProductNameString(@NotNull ByteBuffer device, @NotNull ByteBuffer descriptor);

    /**
     * Retrieves the product version number for the device.
     *
     * @param descriptor {@link ByteBuffer} pointing to a {@code libusb_device_descriptor} instanace in native.
     *                   Provided by {@link LibUsbDeviceDescriptor#getNativeObject()}.
     *
     * @return {@link String} The device product version.
     */
    private native String nativeGetDeviceVersion(@NotNull ByteBuffer descriptor);

    /**
     * Retrieves the connection speed for the device.
     *
     * @param device     {@link ByteBuffer} pointing to a {@code libusb_device_handle} instance in native. Provided
     *                   by {@link UsbDevice#getNativeObject()}.
     * @param descriptor {@link ByteBuffer} pointing to a {@code libusb_device_descriptor} instanace in native.
     *                                     Provided by {@link LibUsbDeviceDescriptor#getNativeObject()}.
     *
     * @return
     */
    private native int nativeGetDeviceSpeed(@NotNull ByteBuffer device, @NotNull ByteBuffer descriptor);

    /**
     * Retrieves the number of configurations available on the device.
     *
     * @param device {@link ByteBuffer} pointing to a {@code libusb_device_handle} instance in native. Provided by
     *               {@link UsbDevice#getNativeObject()}.
     *
     * @return {@code int} The number of configurations.
     */
    private native int nativeGetConfigurationCount(@NotNull ByteBuffer device);

    /**
     * Retrieves the pointer to a {@code libusb_device_handle} instance in native. This is useful when
     * parceling/serializing the {@link UsbDevice}
     * as the {@link ByteBuffer} we normally use cannot be serialized and still point to the same instance.
     *
     * @param device {@link ByteBuffer} pointing to a {@code libusb_device_handle} instance in native. Provided by
     *               {@link UsbDevice#getNativeObject()}.
     *
     * @return {@code long} The native pointer.
     */
    private native long nativeGetPointerFromNativeObject(@NotNull ByteBuffer device);

    /**
     * Converts a native pointer into a {@link ByteBuffer} wrapping a {@code libusb_device_handle} instance in native
     * . This is
     * useful when constructing a {@link UsbDevice} from serialization or parcel as only the pointer can be stored.
     *
     * @param pointer {@code long} The native pointer.
     *
     * @return {@link ByteBuffer} pointing to a {@code libusb_device_handle} instance in native. Provided by
     * {@link UsbDevice#getNativeObject()}.
     */
    @Nullable
    private native ByteBuffer nativeGetNativeObjectFromPointer(long pointer);


}
