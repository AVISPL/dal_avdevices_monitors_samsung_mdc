/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc;

import static com.avispl.symphony.dal.communicator.samsung.mdc.SamsungMDCConstants.statusCodeNames;

public class SamsungMDCStatus {
    private statusCodeNames lamp;
    private statusCodeNames temperatureError;
    private statusCodeNames brightnessSensor;
    private statusCodeNames noSync;
    private statusCodeNames fan;
    private int temperature;

    public SamsungMDCStatus(statusCodeNames lamp,statusCodeNames temperatureError,statusCodeNames brightnessSensor,statusCodeNames noSync, statusCodeNames fan, int temperature){
        this.lamp = lamp;
        this.temperatureError = temperatureError;
        this.brightnessSensor = brightnessSensor;
        this.noSync = noSync;
        this.fan = fan;
        this.temperature = temperature;
    }

    public statusCodeNames getLamp() {
        return lamp;
    }

    public statusCodeNames getTemperatureError() {
        return temperatureError;
    }

    public statusCodeNames getBrightnessSensor() {
        return brightnessSensor;
    }

    public statusCodeNames getNoSync() {
        return noSync;
    }

    public statusCodeNames getFan() {
        return fan;
    }

    public int getTemperature() {
        return temperature;
    }
}
