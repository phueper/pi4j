package com.pi4j.component.xyz.impl;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Device Abstractions
 * FILENAME      :  XYZSensorScaledValueImpl.java  
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

import com.pi4j.component.xyz.XYZSensorScaledValue;

/**
 * implementation for XYZ16bitSignedScaledSensor
 */
public class XYZSensorScaledValueImpl implements XYZSensorScaledValue {
    // to make it easier to port this to other bit sizes, we store positive / negative MAX as absolute values already as float
    private static final float POSITIVE_MAX = Short.MAX_VALUE;
    private static final float NEGATIVE_MAX = Math.abs(Short.MIN_VALUE);

    private short x;
    private short y;
    private short z;
    // default fullScale = 1
    private float fullScale = 1;

    @Override
    public short getX() {
        return x;
    }

    @Override
    public void setX(short x) {
        this.x = x;
    }

    @Override
    public void setX(byte msb, byte lsb) {
        this.x = getShortFromMsbLsb(msb, lsb);
    }

    @Override
    public short getY() {
        return y;
    }

    @Override
    public void setY(short y) {
        this.y = y;
    }

    @Override
    public void setY(byte msb, byte lsb) {
        this.y = getShortFromMsbLsb(msb, lsb);
    }

    @Override
    public short getZ() {
        return z;
    }

    @Override
    public void setZ(short z) {
        this.z = z;
    }

    @Override
    public void setZ(byte msb, byte lsb) {
        this.z = getShortFromMsbLsb(msb, lsb);
    }

    @Override
    public float getFullScale() {
        return fullScale;
    }

    @Override
    public void setFullScale(float fullScale) {
        this.fullScale = fullScale;
    }

    @Override
    public float getScaledX() {
        short rawX = getX();
        return getScaledValue(rawX);
    }

    @Override
    public float getScaledY() {
        short rawY = getY();
        return getScaledValue(rawY);
    }

    @Override
    public float getScaledZ() {
        short rawZ = getZ();
        return getScaledValue(rawZ);
    }
    
    private float getScaledValue(short rawValue) {
        float rval = 0;
        if (rawValue >= 0) {
            rval = (rawValue / POSITIVE_MAX) * fullScale;
        } else {
            rval = (rawValue / NEGATIVE_MAX) * fullScale;
        }
        return rval;
    }

    public static short getShortFromMsbLsb(byte msb, byte lsb) {
        return (short) (((msb & 0xff) << 8) | (lsb & 0xff));
    }
}
