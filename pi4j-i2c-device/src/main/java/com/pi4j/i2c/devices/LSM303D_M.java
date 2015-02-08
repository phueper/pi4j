package com.pi4j.i2c.devices;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: I2C Device Abstractions
 * FILENAME      :  LSM303D_M.java  
 * 
 * This file is part of the Pi4J project. More information about 
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2015 Pi4J
 * %%
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
 * #L%
 */

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;

/**
 * the LSM303D has an Accelerometer and a Magnetometer,
 * both have 3 axis (X,Y,Z) and a scale factor on each axis dependening 
 * on register settings
 * 
 * This class represents the LSM303D Magnetometer
 */
public class LSM303D_M extends LSM303D {

    private boolean enabled = false;

    public LSM303D_M(I2CBus bus) throws IOException {
        // default scale for LSM303D_A: +/- 4 gauss, could be changed in CTRL6
        fullScale = 4;
        dataBaseRegAddress = OUT_X_L_M;
    }

    @Override
    public void enable(I2CBus bus, boolean enableFifo) throws IOException {
        I2CDevice device = bus.getDevice(LSM303D_ADDRESS);
        // CTRL1 BDU -> Block Data Update (1= output registers not updated until MSB and LSB reading)
        // CTRL5 MODR[2:0] -> Data Rate selection (0x4 = 50 Hz) (Attention: 100 Hz mode only works if accelerometer is 100Hz or disabled!!) 
        // CTRL7 MD[1:0] -> Power Mode (0x0 = Continuous conversion mode) 
        byte ctrl1 = (byte) device.read(CTRL1);
        byte ctrl5 = (byte) device.read(CTRL5);
        byte ctrl7 = (byte) device.read(CTRL7);
        ctrl1 |= (byte) 1 << 3;
        ctrl5 |= (byte) (0x4 & 0x7) << 2;
        ctrl7 &= (byte) ~(0x3);
        device.write(CTRL1, ctrl1);
        device.write(CTRL5, ctrl5);
        device.write(CTRL7, ctrl7);
        enabled = true;
    }

    @Override
    public void disable(I2CBus bus) throws IOException {
        I2CDevice device = bus.getDevice(LSM303D_ADDRESS);
        enabled = false;
        // CTRL7 MD[1:0] -> Power Mode (0x3 = Power-down)
        byte ctrl7 = (byte) device.read(CTRL7);
        ctrl7 |= (byte) 0x3;
        device.write(CTRL7, ctrl7);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
