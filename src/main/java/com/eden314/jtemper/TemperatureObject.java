package com.eden314.jtemper;

/**
 * Object for holding all data from a single request
 * to a TEMPer2 device.
 */
class TemperatureObject
{
    protected double insideTemperature;

    protected double outsideTemperature;

    public double getInsideTemperature()
    {
        return this.insideTemperature;
    }

    public double getOutsideTemperature()
    {
        return this.outsideTemperature;
    }

    public TemperatureObject(double insideTemperature, double outsideTemperature)
    {
        this.insideTemperature = insideTemperature;
        this.outsideTemperature = outsideTemperature;
    }
}
