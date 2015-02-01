package com.pi4j.component.xyz.impl;

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