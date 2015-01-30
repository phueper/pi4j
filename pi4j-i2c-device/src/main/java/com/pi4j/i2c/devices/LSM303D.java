package com.pi4j.i2c.devices;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: I2C Device Abstractions
 * FILENAME      :  LSM303D.java  
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

/**
 * the LSM303D has an Accelerometer and a Magnetometer,
 * both have 3 axis (X,Y,Z) and a scale factor on each axis dependening 
 * on register settings
 *
 * This class is the abstract superclass for both Accelerometer and Magnetometer 
 * for common properties and methods
 */
public abstract class LSM303D extends XYZ16bitSignedScaledSensorImpl {
    public final static int LSM303D_ADDRESS = 0x1d;
    /* register addresses */
    public static final int TEMP_OUT_L = 0x05;
    public static final int TEMP_OUT_H = 0x06;
    public static final int STATUS_M = 0x07;
    public static final int OUT_X_L_M = 0x08;
    public static final int OUT_X_H_M = 0x09;
    public static final int OUT_Y_L_M = 0x0a;
    public static final int OUT_Y_H_M = 0x0b;
    public static final int OUT_Z_L_M = 0x0c;
    public static final int OUT_Z_H_M = 0x0d;
    public static final int WHO_AM_I = 0x0F;
    public static final int INT_CTRL_M = 0x12;
    public static final int INT_SRC_M = 0x13;
    public static final int INT_THS_L_M = 0x14;
    public static final int INT_THS_H_M = 0x15;
    public static final int OFFSET_X_L_M = 0x16;
    public static final int OFFSET_X_H_M = 0x17;
    public static final int OFFSET_Y_L_M = 0x18;
    public static final int OFFSET_Y_H_M = 0x19;
    public static final int OFFSET_Z_L_M = 0x1a;
    public static final int OFFSET_Z_H_M = 0x1b;
    public static final int REFERENCE_X = 0x1c;
    public static final int REFERENCE_Y = 0x1d;
    public static final int REFERENCE_Z = 0x1e;
    public static final int CTRL0 = 0x1f;
    public static final int CTRL1 = 0x20;
    public static final int CTRL2 = 0x21;
    public static final int CTRL3 = 0x22;
    public static final int CTRL4 = 0x23;
    public static final int CTRL5 = 0x24;
    public static final int CTRL6 = 0x25;
    public static final int CTRL7 = 0x26;
    public static final int STATUS_A = 0x27;
    public static final int OUT_X_L_A = 0x28;
    public static final int OUT_X_H_A = 0x29;
    public static final int OUT_Y_L_A = 0x2A;
    public static final int OUT_Y_H_A = 0x2B;
    public static final int OUT_Z_L_A = 0x2C;
    public static final int OUT_Z_H_A = 0x2D;
    public static final int FIFO_CTRL = 0x2E;
    public static final int FIFO_SRC = 0x2F;
    public static final int IG_CFG1 = 0x30;
    public static final int IG_SRC1 = 0x31;
    public static final int IG_THS1 = 0x32;
    public static final int IG_DUR1 = 0x33;
    public static final int IG_CFG2 = 0x34;
    public static final int IG_SRC2 = 0x35;
    public static final int IG_THS2 = 0x36;
    public static final int IG_DUR2 = 0x37;
    public static final int CLICK_CFG = 0x38;
    public static final int CLICK_SRC = 0x39;
    public static final int CLICK_THS = 0x3a;
    public static final int TIME_LIMIT = 0x3b;
    public static final int TIME_LATENCY = 0x3c;
    public static final int TIME_WINDOWS = 0x3d;
    public static final int ACT_THS = 0x3e;
    public static final int ACT_DUR = 0x3f;
    private static final int CALIBRATION_READS = 50;
    private static final int CALIBRATION_SKIPS = 5;
    protected I2CDevice device;
    
    /* where to read the data from */
    protected int dataBaseRegAddress;

    public LSM303D(I2CBus bus) throws IOException {
        device = bus.getDevice(LSM303D_ADDRESS);
    }

    public void enable() throws IOException {
        // CTRL1 AODR[3:0] -> Power Mode (0x7 = 200 Hz) 
        byte ctrl1 = (byte) device.read(CTRL1);
        ctrl1 |= (byte) 0x7 << 4;
        // CTRL1 BDU -> Block Data Update (1= output registers not updated until MSB and LSB reading)
        ctrl1 |= (byte) 1 << 3;
        device.write(CTRL1, ctrl1);
    }

    public void disable() throws IOException {
        // CTRL1 AODR -> Power Mode (0 = Power Down)
        byte ctrl1 = (byte) device.read(CTRL1);
        ctrl1 &= (byte) ~(0xF << 4);
        device.write(CTRL1, ctrl1);
    }

    public void readData() throws IOException {
        byte[] data = new byte[6];
        // read from OUT_X_L_A, OUT_X_H_A, OUT_Y_L_A, OUT_Y_H_A, OUT_Z_L_A, OUT_Z_H_A
        // according to the spec for multi-byte read bit 7 of the address must be set
        int r = device.read(dataBaseRegAddress | (1 << 7), data, 0, 6);
        
        if (r != 6) {
            throw new IOException("Couldn't read data; r=" + r);
        }

        setX(data[1], data[0]);
        setY(data[3], data[2]);
        setZ(data[5], data[4]);

//        System.out.println(String.format("0: %#x, 1: %#x, 2: %#x, 3: %#x, 4: %#x, 5: %#x", data[0], data[1], data[2], data[3], data[4], data[5]));
//        System.out.println(String.format("X: %#x, Y: %#x, Z: %#x", getX(), getY(), getZ()));
//        System.out.println(String.format("X: %d, Y: %d, Z: %d", getX(), getY(), getZ()));

    }
}
