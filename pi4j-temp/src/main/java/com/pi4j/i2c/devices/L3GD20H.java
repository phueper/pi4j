package com.pi4j.i2c.devices;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Temporary Placeholder
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

import com.pi4j.component.xyz.XYZSensor;
import com.pi4j.component.xyz.XYZSensorScaledValue;
import com.pi4j.component.xyz.impl.XYZSensorScaledValueImpl;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class L3GD20H implements XYZSensor<XYZSensorScaledValue> {

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
    
    public static final int FIFO_THRESHOLD = 30;
    private final float fullScale;

    private boolean fifoEnabled = false;
    private boolean enabled = false;

    public L3GD20H() throws IOException {
        // default dps for L3GD20H: 245, could be changed in CTRL4
        fullScale = 245;
    }

    @Override
    public void enable(I2CBus bus, boolean enableFifo) throws IOException {
        I2CDevice device = getI2CDevice(bus);
        device = bus.getDevice(L3GD20H_ADDRESS);
        // CTRL1 PD -> Power Mode (0=Power Down, 1=Normal Mode)
        byte ctrl1 = (byte) device.read(CTRL1);
        ctrl1 |= (byte) 1 << 3;
        device.write(CTRL1, ctrl1);
        byte low_odr = (byte) device.read(LOW_ODR);
        // reset at next boot
        low_odr |= (byte) 1 << 2;
        device.write(LOW_ODR, low_odr);
        byte ctrl5 = (byte) device.read(CTRL5);
        // after power up force a boot
        ctrl5 |= (byte) 1 << 7;
        device.write(CTRL5, ctrl5);
        // need to wait a little for the boot
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // do nothing
        }
        // ... and power up again CTRL1 PD -> Power Mode (0=Power Down, 1=Normal Mode)
        ctrl1 = (byte) device.read(CTRL1);
        ctrl1 |= (byte) 1 << 3;
        device.write(CTRL1, ctrl1);
        // CTRL1 DR[1:0] and BW[1:0] -> Data Rate / Bandwidth 
        // if LOW_ODR=0,b0 DR=0b00 and BW=0b00 -> 100 Hz (default)
        // currently nothing to do
        // CTRL4 BDU -> Block Data Update (1= output registers not updated until MSB and LSB reading)
        byte ctrl4 = (byte) device.read(CTRL4);
        ctrl4 |= (byte) 1 << 7;
        device.write(CTRL4, ctrl4);
        if (enableFifo) {
            enableFifo(device);
        }
        enabled = true;
    }

    private void enableFifo(I2CDevice device) throws IOException {
        fifoEnabled = true;
        // enable dynamic stream mode
        // FIFO_CTRL(FM2:0) = 0b110
        byte fifo_ctrl = (byte) device.read(FIFO_CTRL);
        fifo_ctrl |= (byte) 0b110 << 5;
        // FIFO Threshold (max. N+1 entries)
        fifo_ctrl |= FIFO_THRESHOLD;
        device.write(FIFO_CTRL, fifo_ctrl);
        // need to wait a little before enabling FIFO
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // do nothing
        }
        // FIFO_EN to Enable
        byte ctrl5 = (byte) device.read(CTRL5);
        ctrl5 |= (byte) 1 << 6;
        // enable High Pass Filter
        ctrl5 |= (byte) 1 << 4;
        device.write(CTRL5, ctrl5);
    }

    @Override
    public void disable(I2CBus bus) throws IOException {
        I2CDevice device = getI2CDevice(bus);
        enabled = false;
        if (fifoEnabled) {
            // disable fifo
            // FIFO_EN to Disable
            byte ctrl5 = (byte) device.read(CTRL5);
            ctrl5 &= (byte) (~(1 << 6) & 0xff);
            device.write(CTRL5, ctrl5);
        }
        // CTRL1 PD -> Power Mode (0=Power Down, 1=Normal Mode)
        byte ctrl1 = (byte) device.read(CTRL1);
        ctrl1 &= (byte) ~(1 << 3);
        device.write(CTRL1, ctrl1);
    }

    @Override
    public List<XYZSensorScaledValue> readFifoData(I2CBus bus) throws IOException {
        I2CDevice device = getI2CDevice(bus);
        List<XYZSensorScaledValue> rval = new ArrayList<>();
        byte[] data = new byte[6 * (FIFO_THRESHOLD + 1)];
        // read FIFO_SRC to see how much data was available and wether overrun occurred
        byte fifo_src = (byte) device.read(FIFO_SRC);
        // read from OUT_X_L, OUT_X_H, OUT_Y_L, OUT_Y_H, OUT_Z_L, OUT_Z_H
        // in read burst mode, if more than 6 bytes are supposed to be read, 
        // the reading starts at OUT_X_L, continues to OUT_Z_H and then resets
        // to OUT_X_L until all requested bytes are read
        // according to the spec for multi-byte read bit 7 of the address must be set
        int r = device.read(OUT_X_L | (1 << 7), data, 0, data.length);
        if (r <= 0) {
            throw new IOException("Couldn't read gyro data in burst mode; r=" + r);
        }

        boolean fth = (fifo_src & (1 << 7)) != 0;
        boolean ovrn = (fifo_src & (1 << 6)) != 0;
        boolean empty = (fifo_src & (1 << 5)) != 0;
        byte fss = (byte) (fifo_src & (0x1f));

        byte ctrl1 = (byte) device.read(CTRL1);
        byte ctrl4 = (byte) device.read(CTRL4);
        byte ctrl5 = (byte) device.read(CTRL5);
        byte fifo_ctrl = (byte) device.read(FIFO_CTRL);
        byte low_odr = (byte) device.read(LOW_ODR);
        
        // ------- debug ------
//        System.out.println(String.format("CTRL1: %#x (0b%8s)", ctrl1, Integer.toBinaryString(ctrl1 & 0xff)));
//        System.out.println(String.format("CTRL4: %#x (0b%8s)", ctrl4, Integer.toBinaryString(ctrl4 & 0xff)));
//        System.out.println(String.format("CTRL5: %#x (0b%8s)", ctrl5, Integer.toBinaryString(ctrl5 & 0xff)));
//        System.out.println(String.format("FIFO_CTRL: %#x (0b%8s)", fifo_ctrl, Integer.toBinaryString(fifo_ctrl & 0xff)));
//        System.out.println(String.format("LOW_ODR: %#x (0b%8s)", low_odr, Integer.toBinaryString(low_odr & 0xff)));
//        System.out.println(String.format("FIFO_SRC: %#x (0b%8s)", fifo_src, Integer.toBinaryString(fifo_src & 0xff)));
//        System.out.println(String.format("FTH: %b, OVRN: %b, EMPTY: %b, FSS: %#x(%d)", fth, ovrn, empty, fss, fss));
//        StringBuffer sb = new StringBuffer("DATA: \n");
//        for (int i = 0; i + 1 < data.length;) {
//            byte lsb = data[i++];
//            byte msb = data[i++];
//            sb.append(String.format("%3d: %#4x, ", i - 2, XYZSensorScaledValueImpl.getShortFromMsbLsb(msb, lsb)));
//            if (i % 6 == 0) {
//                sb.append("\n");
//            }
//        }
//        System.out.println(sb.toString());
        // ------- debug ------
        
        
        for (int valueCount = 0, byteCount = 0; valueCount < fss; valueCount++) {
            XYZSensorScaledValue value = new XYZSensorScaledValueImpl();
            value.setFullScale(fullScale);
            // get X valyes
            byte lsb = data[byteCount++];
            byte msb = data[byteCount++];
            value.setX(msb, lsb);
            // get Y valyes
            lsb = data[byteCount++];
            msb = data[byteCount++];
            value.setY(msb, lsb);
            // get Z valyes
            lsb = data[byteCount++];
            msb = data[byteCount++];
            value.setZ(msb, lsb);
            rval.add(value);
        }
        return rval;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public XYZSensorScaledValue readSingleData(I2CBus bus) throws IOException {
        I2CDevice device = getI2CDevice(bus);
        XYZSensorScaledValue value = new XYZSensorScaledValueImpl();
        value.setFullScale(fullScale);
        byte[] data = new byte[6];
        // read from OUT_X_L, OUT_X_H, OUT_Y_L, OUT_Y_H, OUT_Z_L, OUT_Z_H
        // according to the spec for multi-byte read bit 7 of the address must be set
        int r = device.read(OUT_X_L | (1 << 7), data, 0, data.length);
        if (r != 6) {
            throw new IOException("Couldn't read gyro data; r=" + r);
        }

        value.setX(data[1], data[0]);
        value.setY(data[3], data[2]);
        value.setZ(data[5], data[4]);

//        System.out.println(String.format("0: %#x, 1: %#x, 2: %#x, 3: %#x, 4: %#x, 5: %#x", data[0], data[1], data[2], data[3], data[4], data[5]));
//        System.out.println(String.format("X: %#x, Y: %#x, Z: %#x", getX(), getY(), getZ()));
//        System.out.println(String.format("X: %d, Y: %d, Z: %d", getX(), getY(), getZ()));
        return value;
    }

    private I2CDevice getI2CDevice(I2CBus bus) throws IOException {
        return bus.getDevice(L3GD20H_ADDRESS);
    }

}
