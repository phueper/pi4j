package com.pi4j.component.xyz.impl;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Device Abstractions
 * FILENAME      :  XYZSensorScaledValueImplTest.java  
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

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class XYZSensorScaledValueImplTest {

    XYZSensorScaledValueImpl value = null;

    @Before
    public void setup() {
        value = new XYZSensorScaledValueImpl();
        value.setFullScale(1);
    }

    @Test
    public void testGetXYZ() throws Exception {
        byte msb = (byte) 0xaf;
        byte lsb = (byte) 0xfe;
        value.setX(msb, lsb);
        assertThat(value.getX(), is((short) 0xaffe));
        value.setY(msb, lsb);
        assertThat(value.getY(), is((short) 0xaffe));
        value.setZ(msb, lsb);
        assertThat(value.getZ(), is((short) 0xaffe));
    }

    @Test
    public void testGetScaledXYZ() throws Exception {
        float fullScale = 1;
        value.setFullScale(fullScale);
        value.setX(Short.MIN_VALUE);
        assertThat(value.getScaledX(), is(fullScale * -1));
        value.setX(Short.MAX_VALUE);
        assertThat(value.getScaledX(), is(fullScale));
        value.setX((short) 0);
        assertThat(value.getScaledX(), is((float) 0));
        value.setY(Short.MIN_VALUE);
        assertThat(value.getScaledY(), is(fullScale * -1));
        value.setY(Short.MAX_VALUE);
        assertThat(value.getScaledY(), is(fullScale));
        value.setY((short) 0);
        assertThat(value.getScaledY(), is((float) 0));
        value.setZ(Short.MIN_VALUE);
        assertThat(value.getScaledZ(), is(fullScale * -1));
        value.setZ(Short.MAX_VALUE);
        assertThat(value.getScaledZ(), is(fullScale));
        value.setZ((short) 0);
        assertThat(value.getScaledZ(), is((float) 0));

        fullScale = (float) Math.PI;
        value.setFullScale(fullScale);
        value.setX(Short.MIN_VALUE);
        assertThat(value.getScaledX(), is(fullScale * -1));
        value.setX(Short.MAX_VALUE);
        assertThat(value.getScaledX(), is(fullScale));
        value.setX((short) 0);
        assertThat(value.getScaledX(), is((float) 0));
        value.setY(Short.MIN_VALUE);
        assertThat(value.getScaledY(), is(fullScale * -1));
        value.setY(Short.MAX_VALUE);
        assertThat(value.getScaledY(), is(fullScale));
        value.setY((short) 0);
        assertThat(value.getScaledY(), is((float) 0));
        value.setZ(Short.MIN_VALUE);
        assertThat(value.getScaledZ(), is(fullScale * -1));
        value.setZ(Short.MAX_VALUE);
        assertThat(value.getScaledZ(), is(fullScale));
        value.setZ((short) 0);
        assertThat(value.getScaledZ(), is((float) 0));

    }

    @Test
    public void testGetShortFromMsbLsb() throws Exception {
        byte msb = (byte) 0xaf;
        byte lsb = (byte) 0xfe;
        assertThat(XYZSensorScaledValueImpl.getShortFromMsbLsb(msb, lsb), is((short) 0xaffe));
    }
}