package com.pi4j.i2c.devices;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: I2C Device Abstractions
 * FILENAME      :  L3GD20H.java  
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

import com.pi4j.component.xyz.impl.XYZ16bitSignedScaledSensorImpl;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;

public class L3GD20H extends XYZ16bitSignedScaledSensorImpl {

    public final static int L3GD20H_ADDRESS = 0x6b;
    /* register addresses */
    public static final int WHO_AM_I = 0x0F;
    public static final int CTRL1 = 0x20;
    public static final int CTRL2 = 0x21;
    public static final int CTRL3 = 0x22;
    public static final int CTRL4 = 0x23;
    public static final int CTRL5 = 0x24;
    public static final int REFERENCE = 0x25;
    public static final int OUT_TEMP = 0x26;
    public static final int STATUS = 0x27;
    public static final int OUT_X_L = 0x28;
    public static final int OUT_X_H = 0x29;
    public static final int OUT_Y_L = 0x2A;
    public static final int OUT_Y_H = 0x2B;
    public static final int OUT_Z_L = 0x2C;
    public static final int OUT_Z_H = 0x2D;
    public static final int FIFO_CTRL = 0x2E;
    public static final int FIFO_SRC = 0x2F;
    public static final int IG_CFG = 0x30;
    public static final int IG_SRC = 0x31;
    public static final int IG_THS_XH = 0x32;
    public static final int IG_THS_XL = 0x33;
    public static final int IG_THS_YH = 0x34;
    public static final int IG_THS_YL = 0x35;
    public static final int IG_THS_ZH = 0x36;
    public static final int IG_THS_ZL = 0x37;
    public static final int IG_DURATION = 0x38;
    public static final int LOW_ODR = 0x39;

    private I2CDevice device;

    public L3GD20H(I2CBus bus) throws IOException {
        device = bus.getDevice(L3GD20H_ADDRESS);
        // default dps for L3GD20H: 245, could be changed in CTRL4
        setFullScale(245);
    }

    public void enable() throws IOException {
        // CTRL1 PD -> Power Mode (0=Power Down, 1=Normal Mode)
        byte ctrl1 = (byte) device.read(CTRL1);
        ctrl1 |= (byte) 1 << 3;
        device.write(CTRL1, ctrl1);
        // CTRL4 BDU -> Block Data Update (1= output registers not updated until MSB and LSB reading)
        byte ctrl4 = (byte) device.read(CTRL4);
        ctrl4 |= (byte) 1 << 7;
        device.write(CTRL4, ctrl4);
    }

    public void disable() throws IOException {
        // CTRL1 PD -> Power Mode (0=Power Down, 1=Normal Mode)
        byte ctrl1 = (byte) device.read(CTRL1);
        ctrl1 &= (byte) ~(1 << 3);
        device.write(CTRL1, ctrl1);
    }


    public void readData() throws IOException {
        byte[] data = new byte[6];
        // read from OUT_X_L, OUT_X_H, OUT_Y_L, OUT_Y_H, OUT_Z_L, OUT_Z_H
        // according to the spec for multi-byte read bit 7 of the address must be set
        int r = device.read(OUT_X_L | (1 << 7), data, 0, 6);
        if (r != 6) {
            throw new IOException("Couldn't read gyro data; r=" + r);
        }

        setX(data[1], data[0]);
        setY(data[3], data[2]);
        setZ(data[5], data[4]);

//        System.out.println(String.format("0: %#x, 1: %#x, 2: %#x, 3: %#x, 4: %#x, 5: %#x", data[0], data[1], data[2], data[3], data[4], data[5]));
//        System.out.println(String.format("X: %#x, Y: %#x, Z: %#x", getX(), getY(), getZ()));
//        System.out.println(String.format("X: %d, Y: %d, Z: %d", getX(), getY(), getZ()));

    }

}
