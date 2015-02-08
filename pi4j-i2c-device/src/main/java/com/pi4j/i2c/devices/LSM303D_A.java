package com.pi4j.i2c.devices;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: I2C Device Abstractions
 * FILENAME      :  LSM303D_A.java  
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
 * This class represents the LSM303D Accelerometer
 */
public class LSM303D_A extends LSM303D {

    private boolean enabled = false;

    public LSM303D_A() throws IOException {
        // default scale for LSM303D_A: +/- 2g, could be changed in CTRL2
        fullScale = 2;
        dataBaseRegAddress = OUT_X_L_A;
    }

    @Override
    public void enable(I2CBus bus, boolean enableFifo) throws IOException {
        I2CDevice device = bus.getDevice(LSM303D_ADDRESS);
        // CTRL1 AODR[3:0] -> Power Mode (0x5 = 50 Hz) 
        byte ctrl1 = (byte) device.read(CTRL1);
        ctrl1 |= (byte) (0x5 & 0xf) << 4;
        // CTRL1 BDU -> Block Data Update (1= output registers not updated until MSB and LSB reading)
        ctrl1 |= (byte) 1 << 3;
        device.write(CTRL1, ctrl1);
        enabled = true;
    }

    @Override
    public void disable(I2CBus bus) throws IOException {
        I2CDevice device = bus.getDevice(LSM303D_ADDRESS);
        enabled = false;
        // CTRL1 AODR -> Power Mode (0 = Power Down)
        byte ctrl1 = (byte) device.read(CTRL1);
        ctrl1 &= (byte) ~(0xF << 4);
        device.write(CTRL1, ctrl1);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
