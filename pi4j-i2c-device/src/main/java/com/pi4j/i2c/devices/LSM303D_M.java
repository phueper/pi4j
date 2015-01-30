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

import java.io.IOException;

/**
 * the LSM303D has an Accelerometer and a Magnetometer,
 * both have 3 axis (X,Y,Z) and a scale factor on each axis dependening 
 * on register settings
 * 
 * This class represents the LSM303D Magnetometer
 */
public class LSM303D_M extends LSM303D {

    public LSM303D_M(I2CBus bus) throws IOException {
        super(bus);
        // default scale for LSM303D_A: +/- 2 gauss, could be changed in CTRL6
        setFullScale(2);
        dataBaseRegAddress = OUT_X_L_M;
    }


}
