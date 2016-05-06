package com.eden314.jtemper;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.usb4java.BufferUtils;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

/**
 * Basic class for simplyfing communication with a USB device
 * using the usb4java library. Most steps concerning interaction
 * with the USB device have been derived from the usb4java Quick
 * Start guide.
 */
public class UsbDeviceCommunicator
{
    final static int DEFAULT_TIMEOUT = 1000; // 1 second

    protected int timeout;
    protected boolean detach;

    protected int interfaceNumber1 = 0;
    protected int interfaceNumber2 = 1;

    protected Context context;
    protected DeviceHandle handle;

    public UsbDeviceCommunicator()
    {
        this.timeout = DEFAULT_TIMEOUT;
    }

    /**
     * Finds the device, creates a handle and makes this accessable
     * by detaching the kernel driver (if needed).
     * 
     * @param short vendorId 
     * @param short productId 
     */
    public void connectToDevice(short vendorId, short productId)
    {
        // create context
        context = new Context();
        int result = LibUsb.init( context );
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to initialise LibUsb", result); 

        // connect to device
        Device device = this.findDevice(vendorId, productId);
        if (device == null) throw new LibUsbException("Could not find temper device", 1);

        // create a device handle for communication
        this.handle = new DeviceHandle();
        result = LibUsb.open(device, handle);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to open USB device", result);

        // Check if kernel driver must be detached
        detach = LibUsb.hasCapability(LibUsb.CAP_SUPPORTS_DETACH_KERNEL_DRIVER) 
            && LibUsb.kernelDriverActive(handle, interfaceNumber1) == 1;
        
        // FIXME: for some reason LibUsb returns false on checking if it supports
        // detaching the kernel driver. This is needed however, so this line sets
        // detach to true for now
        detach = true;

        // Detach the kernel driver
        if (detach) {
            result = LibUsb.detachKernelDriver(handle,  interfaceNumber1);
            if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to detach kernel driver", result);
            result = LibUsb.detachKernelDriver(handle,  interfaceNumber2);
            if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to detach kernel driver", result);
        }

        // Clain interfaces
        result = LibUsb.claimInterface(handle, interfaceNumber1);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to claim interface", result);
        result = LibUsb.claimInterface(handle, interfaceNumber2);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to claim interface", result);
    }

    /**
     * Disconnects the handle from the USB device and reattaches the kernel
     * driver if needed.
     *
     */
    public void disconnect()
    {
        // release the interfaces
        int result = LibUsb.releaseInterface(handle, interfaceNumber1);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to release interface", result);
        result = LibUsb.releaseInterface(handle, interfaceNumber2);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to release interface", result);

        // Attach the kernel driver again if needed
        if (detach) {
            result = LibUsb.attachKernelDriver(handle, interfaceNumber1);
            if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to re-attach kernel driver", result);
            result = LibUsb.attachKernelDriver(handle, interfaceNumber2);
            if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to re-attach kernel driver", result);
        }

        // close handle
        LibUsb.close(handle);
        
        // exit the context
        LibUsb.exit(context);
    }

    /**
     * Simple function to search for a device.
     * 
     * @param short vendorId 
     * @param short productId 
     * @return Device 
     */
    protected Device findDevice(short vendorId, short productId)
    {
        Device wantedDevice = null;
        DeviceList deviceList = new DeviceList();
        int result = LibUsb.getDeviceList(context, deviceList);
        if (result < 0) throw new LibUsbException("Failed to create deviceList", result);

        for (Device device : deviceList) {
            DeviceDescriptor descriptor = new DeviceDescriptor();
            result = LibUsb.getDeviceDescriptor(device, descriptor);
            if (result != LibUsb.SUCCESS) throw new LibUsbException("Failed to create deviceDescriptor", result);

            if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) {
                wantedDevice = device;
            }
        }

        LibUsb.freeDeviceList(deviceList, true);

        return wantedDevice;
    }

    /**
     * Sends the byte array to the USB device.
     *
     * @param byte[] data 
     */
    public void sendRequest(byte[] data) 
    {
        ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
        buffer.put(data);
        buffer.rewind();

        int transfered = LibUsb.controlTransfer(
                handle, 
                (byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE | LibUsb.ENDPOINT_OUT),
                (byte) 0x09, 
                (short) (2 << 8), 
                (short) interfaceNumber2, 
                buffer, 
                this.timeout);

        if (transfered < 0) throw new LibUsbException("Control transfer failed", transfered);
    } 

    /** 
     * Does a interrupt read on the USB device and reads N bytes.
     *
     * @param int numberOfBytes 
     * @return ByteBuffer
     */
    public ByteBuffer receiveData(int numberOfBytes)
    {
        ByteBuffer buffer = ByteBuffer.allocateDirect(numberOfBytes);
        IntBuffer transferedBytes = BufferUtils.allocateIntBuffer();
        
        int result = LibUsb.interruptTransfer(handle, (byte)0x82, buffer, transferedBytes, this.timeout);
        if (result < 0) throw new LibUsbException("Failed to retrieve data", result);

        return buffer;
    }
}
