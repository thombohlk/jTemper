package com.eden314.jtemper;

/**
 * Simple app for reading the inside and outside temperature
 * values of the TEMPer2 usb thermometer.
 *
 */
public class App 
{
    public static void main(String[] args )
    {
        Temper2Reader reader = new Temper2Reader();
        TemperatureObject data = reader.getTemperature();
        
        System.out.println("Inside temperature: " + data.getInsideTemperature());
        System.out.println("Outside temperature: " + data.getOutsideTemperature());
    }
}
