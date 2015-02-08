package com.pi4j.component.xyz;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Device Abstractions
 * FILENAME      :  XYZSensor.java  
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
import java.util.List;

public interface XYZSensor<V extends XYZSensorValue> {
    
    void enable(I2CBus bus, boolean enableFifo) throws IOException;

    void disable(I2CBus bus) throws IOException;
    V readSingleData(I2CBus bus) throws IOException;
    List<V> readFifoData(I2CBus bus) throws IOException;

    boolean isEnabled();
}
