package com.pi4j.component.potentiometer.microchip;

import com.pi4j.component.potentiometer.microchip.impl.MicrochipPotentiometerBase;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;
import java.util.Random;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Device Abstractions
 * FILENAME      :  MCP4651.java  
 * 
 * This file is part of the Pi4J project. More information about 
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2015 Pi4J
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

/**
 * Pi4J-device for MCP4651.
 * 
 * @author <a href="http://raspelikan.blogspot.co.at">Raspelikan</a>
 */
public class MCP4651 extends MicrochipPotentiometerBase implements MicrochipPotentiometer {

	private static final MicrochipPotentiometerChannel[] supportedChannels = new MicrochipPotentiometerChannel[] {
		MicrochipPotentiometerChannel.A, MicrochipPotentiometerChannel.B
	};
	
	/**
	 * Builds an instance which is ready to use.
	 * 
	 * @param i2cBus The Pi4J-I2CBus to which the device is connected to
	 * @param pinA0 Whether the device's address pin A0 is high (true) or low (false)
	 * @param pinA1 Whether the device's address pin A1 (if available) is high (true) or low (false)
	 * @param pinA2 Whether the device's address pin A2 (if available) is high (true) or low (false)
	 * @param channel Which of the potentiometers provided by the device to control
	 * @param initialValue Initial value of wiper
	 * @throws IOException Thrown if communication fails or device returned a malformed result
	 */
	public MCP4651(final I2CBus i2cBus, final boolean pinA0,
                   final boolean pinA1, final boolean pinA2,
                   final MicrochipPotentiometerChannel channel, final int initialValue)  throws IOException {
		
		super(i2cBus, pinA0, pinA1, pinA2,
				channel, MicrochipPotentiometerNonVolatileMode.VOLATILE_ONLY, initialValue);
		
	}

	/**
	 * @return Whether device is capable of non volatile wipers (false for MCP4651)
	 */
	@Override
	public boolean isCapableOfNonVolatileWiper() {
		
		return false;
		
	}
	
	/**
	 * @return The maximal value at which a wiper can be (256 for MCP4651)
	 */
	@Override
	public int getMaxValue() {
		
		return maxValue();
		
	}

	/**
	 * @return The maximal value at which a wiper can be (256 for MCP4651)
	 */
	public static int maxValue() {
		
		return 256;
		
	}

	/**
	 * @return Whether this device is a potentiometer or a rheostat (false for MCP4651)
	 */
	@Override
	public boolean isRheostat() {
		
		return false;
		
	}
	
	/**
	 * @return All channels supported by the underlying device (A, B for MCP4651)
	 */
	@Override
	public MicrochipPotentiometerChannel[] getSupportedChannelsByDevice() {
		
		return supportedChannels;
		
	}

	/**
	 * A MCP4651 is expected to be connected to I2C-bus 1 of Raspberry Pi.
	 * All address-pins are assumed to be low (means address 0x28).
	 * <p>
	 * Both channels of the are initialized at mid-value (the same value
	 * as hardware-preset). A is brought to max-value and B to min-value
	 * in about 3 seconds. After that A and B are going up and down (26
	 * steps per second for 5 seconds). At the end A and B are set at random
	 * (2 times per second for 5 seconds).
	 *
	 * @param args no parameters expected
	 * @throws IOException If anything goes wrong
	 */
	public static void main(String[] args) throws IOException {

		// initialize bus
		final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
		try {

			final MicrochipPotentiometer a = new MCP4651(
					bus, false, false, false, MicrochipPotentiometerChannel.A, MCP4651.maxValue() / 2);
			final MicrochipPotentiometer b = new MCP4651(
					bus, false, false, false, MicrochipPotentiometerChannel.B, MCP4651.maxValue() / 2);

			// Check device-status
			final MicrochipPotentiometerDeviceStatus aStatus = a.getDeviceStatus();
			System.out.println("WiperLock for A active: " + aStatus.isWiperLockActive());
			final MicrochipPotentiometerDeviceStatus bStatus = b.getDeviceStatus();
			System.out.println("WiperLock for B active: " + bStatus.isWiperLockActive());

			// print current values
			System.out.println("A: " + a.getCurrentValue()
					+ "/" + a.updateCacheFromDevice());
			System.out.println("B: " + b.getCurrentValue()
					+ "/" + b.updateCacheFromDevice());

			// for about 3 seconds

			for (int i = 0; i < MCP4651.maxValue() / 2; ++i) {

				// increase a
				a.increase();

				// decrease b
				b.decrease();

				// wait a little bit
				try {
					Thread.sleep(24); // assume 1 ms for I2C-communication
				} catch (InterruptedException e) {
					// never mind
				}

			}

			// print current values
			System.out.println("A: " + a.getCurrentValue()
					+ "/" + a.updateCacheFromDevice());
			System.out.println("B: " + b.getCurrentValue()
					+ "/" + b.updateCacheFromDevice());

			// 5 seconds at 26 steps
			boolean aDirectionUp = false;
			boolean bDirectionUp = true;
			final int counter1 = 5 * 26;
			for (int i = 0; i < counter1; ++i) {

				// change wipers
				if (aDirectionUp) {
					a.increase(10);
				} else {
					a.decrease(10);
				}
				if (bDirectionUp) {
					b.increase(10);
				} else {
					b.decrease(10);
				}

				// reverse direction
				if ((aDirectionUp && (a.getCurrentValue() == a.getMaxValue()))
						|| (!aDirectionUp && (a.getCurrentValue() == 0))) {
					aDirectionUp = !aDirectionUp;
				}
				if ((bDirectionUp && (b.getCurrentValue() == b.getMaxValue()))
						|| (!bDirectionUp && (b.getCurrentValue() == 0))) {
					bDirectionUp = !bDirectionUp;
				}

				// wait a little bit
				try {
					Thread.sleep(39); // assume 1 ms for I2C-communication
				} catch (InterruptedException e) {
					// never mind
				}

			}

			// 5 seconds at 2 steps
			Random randomizer = new Random(System.currentTimeMillis());
			int counter2 = 5 * 2;
			for (int i = 0; i < counter2; ++i) {

				int nextA = randomizer.nextInt(MCP4651.maxValue() + 1);
				a.setCurrentValue(nextA);

				int nextB = randomizer.nextInt(MCP4651.maxValue() + 1);
				b.setCurrentValue(nextB);

				// wait a little bit
				try {
					Thread.sleep(499); // assume 1 ms for I2C-communication
				} catch (InterruptedException e) {
					// never mind
				}

			}

			// print current values
			System.out.println("A: " + a.getCurrentValue()
					+ "/" + a.updateCacheFromDevice());
			System.out.println("B: " + b.getCurrentValue()
					+ "/" + b.updateCacheFromDevice());

		} finally {

			try {
				bus.close();
			} catch (Throwable e) {
				e.printStackTrace();
			}

		}

	}

}
