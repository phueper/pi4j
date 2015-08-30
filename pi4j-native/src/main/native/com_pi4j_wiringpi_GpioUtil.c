/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: JNI Native Library
 * FILENAME      :  com_pi4j_wiringpi_GpioUtil.c  
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

#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <sys/types.h>
#include <fcntl.h>
#include "com_pi4j_wiringpi_GpioPin.h"
#include "com_pi4j_wiringpi_GpioUtil.h"


/* Source for com_pi4j_wiringpi_GpioUtil */

#define RDBUF_LEN       10

// pin directions
#define DIRECTION_IN 0
#define DIRECTION_OUT 1
#define DIRECTION_HIGH 2
#define DIRECTION_LOW 3

// edge detection conditions
#define EDGE_NONE 0
#define EDGE_BOTH 1
#define EDGE_RISING 2
#define EDGE_FALLING 3

/*
 * changeOwner:
 *	Change the ownership of the file to the real userId of the calling
 *	program so we can access it.
 *********************************************************************************
 */
static void changeOwner (char *file)
{
  uid_t uid = getuid () ;
  uid_t gid = getgid () ;

  if (chown (file, uid, gid) != 0)
  {
    if (errno == ENOENT)	// Warn that it's not there
    {
      fprintf (stderr, "Warning: File not present: %s\n", file) ;
    }
    else
    {
      fprintf (stderr, "Unable to change ownership of %s: %s\n", file, strerror (errno)) ;
    }
  }
}


/*
 * Class:     com_pi4j_wiringpi_GpioUtil
 * Method:    export
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_pi4j_wiringpi_GpioUtil_export
  (JNIEnv *env, jclass class, jint pin, jint direction)
{
	FILE *fd ;
	char fName [128] ;

	// validate the pin number
	if(isPinValid(pin) <= 0)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Invalid GPIO pin [%d]\n", pin) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return;
	}

	// validate the pin direction
	if(direction != DIRECTION_IN &&
	   direction != DIRECTION_OUT &&
	   direction != DIRECTION_HIGH &&
	   direction != DIRECTION_LOW)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Invalid GPIO pin [%d] direction [%d]\n", pin, direction) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return;
	}

	// get the edge pin number
	int edgePin = getEdgePin(pin);

	// validate that the export file can be accessed
	if ((fd = fopen ("/sys/class/gpio/export", "w")) == NULL)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Unable to open GPIO export interface: %s\n", strerror (errno)) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return;
	}

	// add the pin to the export file; then close the file
	fprintf (fd, "%d\n", edgePin) ;
	fclose (fd) ;

	// get the pin direction
	int existing_direction = getExistingPinDirection(edgePin);

	// set direction if its not already configured with the same direction value
	if(direction != existing_direction)
	{
        // attempt to access the gpio pin's direction file
        sprintf (fName, "/sys/class/gpio/gpio%d/direction", edgePin) ;
        if ((fd = fopen (fName, "w")) == NULL)
        {
            // throw exception
            char errstr[255];
            sprintf (errstr, "Unable to open GPIO direction interface for pin [%d]: %s\n", pin, strerror (errno)) ;
            (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
            return;
        }

        // write the IN/OUT direction to the direction file
        if (direction == DIRECTION_IN)
        {
          fprintf (fd, "in\n") ;
        }
        else if (direction == DIRECTION_OUT)
        {
          fprintf (fd, "out\n") ;
        }
        else if (direction == DIRECTION_HIGH)
        {
          fprintf (fd, "high\n") ;
        }
        else if (direction == DIRECTION_LOW)
        {
          fprintf (fd, "low\n") ;
        }
        else
        {
            // close the direction file
            fclose (fd) ;

            // throw exception
            char errstr[255];
            sprintf (errstr, "Unsupported DIRECTION [%d] for GPIO pin [%d]\n", direction, pin) ;
            (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
            return;
        }

	    // close the direction file
	    fclose (fd) ;
    }

	// change ownership so the current user can actually use it!
	sprintf (fName, "/sys/class/gpio/gpio%d/value", edgePin) ;
	changeOwner (fName) ;

	sprintf (fName, "/sys/class/gpio/gpio%d/edge", edgePin) ;
	changeOwner (fName) ;
}


/*
 * Class:     com_pi4j_wiringpi_GpioUtil
 * Method:    unexport
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_pi4j_wiringpi_GpioUtil_unexport
(JNIEnv *env, jclass class, jint pin)
{
	FILE *fd ;

	// validate the pin number
	if(isPinValid(pin) <= 0)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Invalid GPIO pin [%d]\n", pin) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return;
	}

	// get the edge pin number
	int edgePin = getEdgePin(pin);

	if ((fd = fopen ("/sys/class/gpio/unexport", "w")) == NULL)
	{
		// throw exception
		char errstr[255];
		sprintf (errstr, "Unable to open GPIO unexport interface for pin [%d]: %s\n", pin, strerror (errno)) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return;
	}

	fprintf (fd, "%d\n", edgePin) ;
	fclose (fd) ;
}


/*
 * Class:     com_pi4j_wiringpi_GpioUtil
 * Method:    isExported
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_pi4j_wiringpi_GpioUtil_isExported
  (JNIEnv *env, jclass class, jint pin)
{
	int result;
	char fName [128] ;

	// validate the pin number
	if(isPinValid(pin) <= 0)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Invalid GPIO pin [%d]\n", pin) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return (jboolean)0;
	}

	// get the edge pin number
	int edgePin = getEdgePin(pin);

	// construct directory path for gpio pin
	sprintf (fName, "/sys/class/gpio/gpio%d", edgePin) ;

	// check for exported gpio directory
	result = access(fName, F_OK);

	if (result == 0)
	{
		// is exported
		return (jboolean)1;
	}

	// not exported
	return (jboolean)0;
}


/*
 * Class:     com_pi4j_wiringpi_GpioUtil
 * Method:    setDirection
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_pi4j_wiringpi_GpioUtil_setDirection
(JNIEnv *env, jclass class, jint pin, jint direction)
{
	FILE *fd ;
	char fName [128] ;

	// validate the pin number
	if(isPinValid(pin) <= 0)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Invalid GPIO pin [%d]\n", pin) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return (jboolean)0;
	}

	// validate the pin direction
	if(direction != DIRECTION_IN &&
	   direction != DIRECTION_OUT &&
	   direction != DIRECTION_HIGH &&
	   direction != DIRECTION_LOW)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Invalid GPIO pin [%d] direction [%d]\n", pin, direction) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return (jboolean)0;
	}

	// get the edge pin number
	int edgePin = getEdgePin(pin);

	// get the pin direction
	int existing_direction = getExistingPinDirection(edgePin);

	// no need to set direction if its already configured with the same direction value
	if(direction == existing_direction)
	{
        // success
        return (jboolean)1;
	}

	// attempt to access the gpio pin's direction file
	sprintf (fName, "/sys/class/gpio/gpio%d/direction", edgePin) ;
	if ((fd = fopen (fName, "w")) == NULL)
	{
		// throw exception
		char errstr[255];
		sprintf (errstr, "Unable to open GPIO direction interface for pin [%d]: %s\n", pin, strerror (errno)) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return (jboolean)0;
	}

	// write the IN/OUT direction to the direction file
	if (direction == DIRECTION_IN)
	{
	  fprintf (fd, "in\n") ;
	}
	else if (direction == DIRECTION_OUT)
	{
	  fprintf (fd, "out\n") ;
	}
	else if (direction == DIRECTION_HIGH)
	{
	  fprintf (fd, "high\n") ;
	}
	else if (direction == DIRECTION_LOW)
	{
	  fprintf (fd, "low\n") ;
	}
	else
	{
        // close the direction file
        fclose (fd) ;

		// throw exception
		char errstr[255];
		sprintf (errstr, "Unsupported DIRECTION [%d] for GPIO pin [%d]\n", direction, pin) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return;
	}

	// close the direction file
	fclose (fd) ;

	// success
	return (jboolean)1;
}


/*
 * Class:     com_pi4j_wiringpi_GpioUtil
 * Method:    getDirection
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_pi4j_wiringpi_GpioUtil_getDirection
(JNIEnv *env, jclass class, jint pin)
{
	// validate the pin number
	if(isPinValid(pin) <= 0)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Invalid GPIO pin [%d]\n", pin) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return -1;
	}

	// get the edge pin number
	int edgePin = getEdgePin(pin);

	// get the pin direction
	int direction = getExistingPinDirection(edgePin);

	// check for errors
	if(direction < 0)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Unable to open GPIO direction interface for pin [%d]: %s\n", pin, strerror (errno)) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return -1;
	}

	return direction;
}

int getExistingPinDirection(int edgePin)
{
	FILE *fd ;
	char fName [128] ;
	char data[RDBUF_LEN];

	// construct the gpio direction file path
	sprintf (fName, "/sys/class/gpio/gpio%d/direction", edgePin) ;

	// open the gpio direction file
	if ((fd = fopen (fName, "r")) == NULL)
	{
		return -1;
	}

	// read the data from the file into the data buffer
	if(fgets(data, RDBUF_LEN, fd) == NULL)
	{
		return -2;
	}

	// close the gpio direction file
	fclose (fd) ;

	// determine direction mode
	if (strncasecmp(data, "in", 2) == 0)
	{
		return DIRECTION_IN;
	}
	else if (strncasecmp(data, "out", 3) == 0)
	{
		return DIRECTION_OUT;
	}
	else
	{
		return -3;
	}
}

/*
 * Class:     com_pi4j_wiringpi_GpioUtil
 * Method:    setEdgeDetection
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_pi4j_wiringpi_GpioUtil_setEdgeDetection
(JNIEnv *env, jclass class, jint pin, jint edge)
{
	FILE *fd ;
	char fName [128] ;

	// validate the pin number
	if(isPinValid(pin) <= 0)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Invalid GPIO pin [%d]\n", pin) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return (jboolean)0;
	}

	// validate the pin edge detection option
	if(edge != EDGE_NONE &&
	   edge != EDGE_BOTH &&
	   edge != EDGE_RISING &&
	   edge != EDGE_FALLING)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Invalid GPIO pin [%d] edge condition [%d]\n", pin, edge) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return (jboolean)0;
	}

	// get the edge pin number
	int edgePin = getEdgePin(pin);

	// Export the pin and set direction to input
	if ((fd = fopen ("/sys/class/gpio/export", "w")) == NULL)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Unable to open GPIO export interface: %s\n", strerror (errno)) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return (jboolean)0;
	}

	// add the pin to the export file
	fprintf (fd, "%d\n", edgePin) ;

	// close the export file
	fclose (fd) ;

	// access the pin direction file and force the pin direction to IN
	sprintf (fName, "/sys/class/gpio/gpio%d/direction", edgePin) ;
	if ((fd = fopen (fName, "w")) == NULL)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Unable to open GPIO direction interface for pin %d: %s\n", pin, strerror (errno)) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return (jboolean)0;
	}

	// set the pin direction to IN
	fprintf (fd, "in\n") ;

	// close the gpio direction file
	fclose (fd) ;

	// construct the gpio edge file path
	sprintf (fName, "/sys/class/gpio/gpio%d/edge", edgePin) ;

	// open the gpio edge file
	if ((fd = fopen (fName, "w")) == NULL)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Unable to open GPIO edge interface for pin %d: %s\n", pin, strerror (errno));
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return (jboolean)0;
	}

	// determine edge mode and write to edge file
	if (edge == EDGE_NONE) fprintf (fd, "none\n") ;
	else if (edge == EDGE_BOTH) fprintf (fd, "both\n") ;
	else if (edge == EDGE_RISING) fprintf (fd, "rising\n") ;
	else if (edge == EDGE_FALLING) fprintf (fd, "falling\n") ;
	else
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Invalid edge mode [%d]. Should be none (0), rising (2), falling (3) or both (1)\n", edge);
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return (jboolean)0;
	}

	// Change ownership of the value and edge files, so the current user can actually use it!
	sprintf (fName, "/sys/class/gpio/gpio%d/value", edgePin);
	changeOwner(fName);

	sprintf (fName, "/sys/class/gpio/gpio%d/edge", edgePin);
	changeOwner(fName);

	// close the gpio edge file
	fclose (fd);

	// success
	return (jboolean)1;
}

/*
 * Class:     com_pi4j_wiringpi_GpioUtil
 * Method:    getEdgeDetection
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_pi4j_wiringpi_GpioUtil_getEdgeDetection
(JNIEnv *env, jclass class, jint pin)
{
	FILE *fd ;
	char fName [128] ;
	char data[RDBUF_LEN];

	// validate the pin number
	if(isPinValid(pin) <= 0)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Invalid GPIO pin: %d\n", pin) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return -1;
	}

	// get the edge pin number
	int edgePin = getEdgePin(pin);

	// construct the gpio edge file path
	sprintf (fName, "/sys/class/gpio/gpio%d/edge", edgePin) ;

	// open the gpio edge file
	if ((fd = fopen (fName, "r")) == NULL)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Unable to open GPIO edge interface for pin %d: %s\n", pin, strerror (errno)) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return -1;
	}

	// read the data from the file into the data buffer
	if(fgets(data, RDBUF_LEN, fd) == NULL)
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Unable to open GPIO edge interface for pin %d: %s\n", pin, strerror (errno)) ;
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return -1;
	}

	// close the gpio edge file
	fclose (fd) ;

	// determine edge mode
	if (strncasecmp(data, "none", 4) == 0) return EDGE_NONE;
	else if (strncasecmp(data, "both", 4) == 0) return EDGE_BOTH;
	else if (strncasecmp(data, "rising", 6) == 0) return EDGE_RISING;
	else if (strncasecmp(data, "falling", 7) == 0) return EDGE_FALLING;
	else
	{
		// throw exception
		char errstr[255];
		sprintf(errstr, "Unrecognized mode: %s. Should be 'none', 'rising', 'falling' or 'both'.\n",data);
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), errstr);
		return -1;
	}
}

/*
 * Class:     com_pi4j_wiringpi_GpioUtil
 * Method:    isPinSupported
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_pi4j_wiringpi_GpioUtil_isPinSupported
(JNIEnv *env, jclass class, jint pin)
{
	// validate the pin number
	return isPinValid(pin);
}

