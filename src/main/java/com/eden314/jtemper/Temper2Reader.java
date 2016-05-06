package com.eden314.jtemper;

import java.nio.ByteBuffer;

/**
 * Class for reading values from the TEMPer2 usb thermometer.
 *
 */
public class Temper2Reader extends UsbDeviceCommunictor
{
    final static byte[] TEMP_REQUEST = { (byte)0x01, (byte)0x80, (byte)0x33, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 };

    final static short TEMPER2_VENDOR_ID  = 0x0c45;
    final static short TEMPER2_PRODUCT_ID = 0x7401;


    /** 
     * Finds TEMPer2 device and returns current temperatures. 
     * @return TemperatureObject
     */
    public TemperatureObject getTemperature()
    {
        this.connectToDevice(TEMPER2_VENDOR_ID, TEMPER2_PRODUCT_ID);
        ByteBuffer rawData = this.getRawTemperatureData();
        TemperatureObject result = this.convertRawDataToTemperatureObject(rawData);
        this.disconnect();

        return result;
    }

    /** 
     * Sends request for temperature data to USB device and
     * returns response raw byte data.
     * @return ByteBuffer
     */
    protected ByteBuffer getRawTemperatureData()
    {
        // get the raw data
        this.sendRequest(TEMP_REQUEST);
        return this.receiveData(8);
    }

    /** 
     * Converts the raw data from a response from the TEMPer2 device
     * to actual temperatues in Celcius.
     * @param ByteBuffer data 
     * @return TemperatureObject
     */
    protected TemperatureObject convertRawDataToTemperatureObject(ByteBuffer data)
    {
        // convert to temperatures
        double insideTemperature = (data.get(3) & (byte)0xFF) + ((char)data.get(2) << 8);
        insideTemperature *= (125.0 / 32000.0);

        double outsideTemperature = (data.get(5) & (byte)0xFF) + ((char)data.get(4) << 8);
        outsideTemperature *= (125.0 / 32000.0);

        return new TemperatureObject(insideTemperature, outsideTemperature);
    }
}
