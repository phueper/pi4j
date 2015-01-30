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

import com.pi4j.component.gyroscope.AxisGyroscope;
import com.pi4j.component.gyroscope.Gyroscope;
import com.pi4j.component.gyroscope.MultiAxisGyro;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;

public class L3GD20H implements MultiAxisGyro {

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

    // default dps for L3GD20H: 245, could be changed in CTRL4
    public final Gyroscope X = new AxisGyroscope(this, 245f);
    public final Gyroscope Y = new AxisGyroscope(this, 245f);
    public final Gyroscope Z = new AxisGyroscope(this, 245f);

    protected final AxisGyroscope aX = (AxisGyroscope) X;
    protected final AxisGyroscope aY = (AxisGyroscope) Y;
    protected final AxisGyroscope aZ = (AxisGyroscope) Z;

    private int timeDelta;
    private long lastRead;

    private static final int CALIBRATION_READS = 50;
    private static final int CALIBRATION_SKIPS = 5;


    public L3GD20H(I2CBus bus) throws IOException {
        device = bus.getDevice(L3GD20H_ADDRESS);
    }

    public Gyroscope init(Gyroscope triggeringAxis, int triggeringMode) throws IOException {
        enable();

        if (triggeringAxis == aX) {
            aX.setReadTrigger(triggeringMode);
        } else {
            aX.setReadTrigger(Gyroscope.READ_NOT_TRIGGERED);
        }
        if (triggeringAxis == aY) {
            aY.setReadTrigger(triggeringMode);
        } else {
            aY.setReadTrigger(Gyroscope.READ_NOT_TRIGGERED);
        }
        if (triggeringAxis == aZ) {
            aZ.setReadTrigger(triggeringMode);
        } else {
            aZ.setReadTrigger(Gyroscope.READ_NOT_TRIGGERED);
        }
        return triggeringAxis;
    }

    @Override
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


    @Override
    public void disable() throws IOException {
        // CTRL1 PD -> Power Mode (0=Power Down, 1=Normal Mode)
        byte ctrl1 = (byte) device.read(CTRL1);
        ctrl1 &= (byte) ~(1 << 3);
        device.write(CTRL1, ctrl1);
    }


    @Override
    public void readGyro() throws IOException {
        long now = System.currentTimeMillis();
        timeDelta = (int) (now - lastRead);
        lastRead = now;

        byte[] data = new byte[6];

        try {
            Thread.sleep(10);
        } catch (InterruptedException ignore) {
        }

        // read from OUT_X_L, OUT_X_H, OUT_Y_L, OUT_Y_H, OUT_Z_L, OUT_Z_H
        // according to the spec for multi-byte read bit 7 of the address must be set
        int r = device.read(OUT_X_L | (1 << 7), data, 0, 6);
        if (r != 6) {
            throw new IOException("Couldn't read gyro data; r=" + r);
        }

        short x = (short) (((data[1] & 0xff) << 8) | (data[0] & 0xff));
        short y = (short) (((data[3] & 0xff) << 8) | (data[2] & 0xff));
        short z = (short) (((data[5] & 0xff) << 8) | (data[4] & 0xff));

//        System.out.println(String.format("0: %#x, 1: %#x, 2: %#x, 3: %#x, 4: %#x, 5: %#x", data[0], data[1], data[2], data[3], data[4], data[5]));
//        System.out.println(String.format("X: %d, Y: %d, Z: %d", x, y, z));

        aX.setRawValue(x);
        aY.setRawValue(y);
        aZ.setRawValue(z);

    }


    @Override
    public int getTimeDelta() {
        return timeDelta;
    }


    @Override
    public void recalibrateOffset() throws IOException {
        long totalX = 0;
        long totalY = 0;
        long totalZ = 0;

        int minX = 10000;
        int minY = 10000;
        int minZ = 10000;

        int maxX = -10000;
        int maxY = -10000;
        int maxZ = -10000;

        for (int i = 0; i < CALIBRATION_SKIPS; i++) {
            readGyro();
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignore) {
            }
        }

        for (int i = 0; i < CALIBRATION_READS; i++) {
            readGyro();

            int x = aX.getRawValue();
            int y = aY.getRawValue();
            int z = aZ.getRawValue();

            totalX = totalX + x;
            totalY = totalY + y;
            totalZ = totalZ + z;
            if (x < minX) {
                minX = x;
            }
            if (y < minY) {
                minY = y;
            }
            if (z < minZ) {
                minZ = z;
            }

            if (x > maxX) {
                maxX = x;
            }
            if (y > maxY) {
                maxY = y;
            }
            if (z > maxZ) {
                maxZ = z;
            }
        }

        aX.setOffset((int) (totalX / CALIBRATION_READS));
        aY.setOffset((int) (totalY / CALIBRATION_READS));
        aZ.setOffset((int) (totalZ / CALIBRATION_READS));

    }


}
