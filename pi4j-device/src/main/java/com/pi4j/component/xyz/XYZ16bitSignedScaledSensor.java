package com.pi4j.component.xyz;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Device Abstractions
 * FILENAME      :  XYZ16bitSignedScaledSensor.java  
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

/**
 * Interface for a 3D (X, Y, Z) Sensor storing 16bit signed (short) values that are scaled
 * the full scale represents the +/- range of the complete 16bit signed (-32768 ... 32767) range
 * getting the scaled values calculates the values according to scale, i.e. if the raw axis value is
 * 32767, the result is +[fullScale] if the raw axis value is -32768 the result is -[fullScale]
 *
 * This could be any sensor providing scaled 3D axis data (e.g. gyro, accelerometer, magnetometer, ...)
 *
 */
public interface XYZ16bitSignedScaledSensor extends XYZ16bitSignedSensor {
    float getFullScale();
    void setFullScale(float fullScale);
    float getScaledX();
    float getScaledY();
    float getScaledZ();
}
