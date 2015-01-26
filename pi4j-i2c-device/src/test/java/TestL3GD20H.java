/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: I2C Device Abstractions
 * FILENAME      :  TestL3GD20H.java  
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

import com.pi4j.component.gyroscope.Gyroscope;
import com.pi4j.i2c.devices.L3GD20H;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;


public class TestL3GD20H {

    public static void main(String[] args) throws Exception {
        I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);

        L3GD20H l3gd20h = new L3GD20H(bus);

        l3gd20h.init(l3gd20h.X, Gyroscope.GET_RAW_VALUE_TRIGGER_READ);

        long now = System.currentTimeMillis();

        int measurement = 0;

        while (System.currentTimeMillis() - now < 10000) {

            l3gd20h.readGyro();

            System.out.println(String.format("            Raw: #: %3d, X: %7d, Y: %7d, Z: %7d", measurement, l3gd20h.X.getRawValue(), l3gd20h.Y.getRawValue(), l3gd20h.Z.getRawValue()));
            System.out.println(String.format("AngularVelocity: #: %3d, X: %7.2f, Y: %7.2f, Z: %7.2f", measurement, l3gd20h.X.getAngularVelocity(), l3gd20h.Y.getAngularVelocity(), l3gd20h.Z.getAngularVelocity()));

            Thread.sleep(100);

            measurement++;
        }
        System.out.println();
    }


}
