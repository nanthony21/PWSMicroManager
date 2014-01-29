///////////////////////////////////////////////////////////////////////////////
//FILE:          Properties.java
//PROJECT:       Micro-Manager 
//SUBSYSTEM:     ASIdiSPIM plugin
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nico Stuurman, Jon Daniels
//
// COPYRIGHT:    University of California, San Francisco, & ASI, 2013
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

package org.micromanager.asidispim.Data;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import mmcorej.CMMCore;

import org.micromanager.MMStudioMainFrame;
import org.micromanager.asidispim.Utils.UpdateFromPropertyListenerInterface;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.ReportingUtils;


/**
 * Contains data and methods related to getting and setting device properties.
 * Ideally this is the only place where properties are read and set.
 * One instance of this class exists in the top-level class.
 * @author Jon
 * @author nico
 */
public class Properties {
   private Devices devices_;
   private CMMCore core_;
   private List<UpdateFromPropertyListenerInterface> listeners_;

   /**
    * List of all device adapter properties used.  The enum value (all caps) is used in the Java code.  The corresponding
    * string value (in quotes) is the value used by the device adapter.
    */
   public static enum Keys {
      JOYSTICK_ENABLED("JoystickEnabled"),
      JOYSTICK_INPUT("JoystickInput"),
      JOYSTICK_INPUT_X("JoystickInputX"),
      JOYSTICK_INPUT_Y("JoystickInputY"),
      SPIM_NUM_SIDES("SPIMNumSides"),
      SPIM_NUM_SLICES("SPIMNumSlices"),
      SPIM_NUM_REPEATS("SPIMNumRepeats"),
      SPIM_NUM_SCANSPERSLICE("SPIMNumScansPerSlice"),
      SPIM_LINESCAN_PERIOD("SingleAxisXPeriod(ms)"),
      SPIM_DELAY_SIDE("SPIMDelayBeforeSide(ms)"),
      SPIM_DELAY_SLICE("SPIMDelayBeforeSlice(ms)"),
      SPIM_FIRSTSIDE("SPIMFirstSide"),
      SPIM_STATE("SPIMState"),
      SA_AMPLITUDE("SingleAxisAmplitude(um)"),
      SA_OFFSET("SingleAxisOffset(um)"),
      SA_AMPLITUDE_X_DEG("SingleAxisXAmplitude(deg)"),
      SA_OFFSET_X_DEG("SingleAxisXOffset(deg)"),
      SA_OFFSET_X("SingleAxisXOffset(um)"),
      SA_MODE_X("SingleAxisXMode"),
      SA_PATTERN_X("SingleAxisXPattern"),
      SA_AMPLITUDE_Y_DEG("SingleAxisYAmplitude(deg)"),
      SA_OFFSET_Y_DEG("SingleAxisYOffset(deg)"),
      SA_OFFSET_Y("SingleAxisYOffset(um)"),
      AXIS_LETTER("AxisLetter"),
      SERIAL_ONLY_ON_CHANGE("OnlySendSerialCommandOnChange"),
      SERIAL_COMMAND("SerialCommand"),
      SERIAL_COM_PORT("SerialComPort"),
      MAX_DEFLECTION_X("MaxDeflectionX(deg)"),
      MIN_DEFLECTION_X("MinDeflectionX(deg)"),
      BEAM_ENABLED("BeamEnabled"),
      SAVE_CARD_SETTINGS("SaveCardSettings"),
      TRIGGER_SOURCE("TRIGGER SOURCE"),
      ;
      private final String text;
      Keys(String text) {
         this.text = text;
      }
      @Override
      public String toString() {
         return text;
      }
   }
   
   // values for properties
   public static enum Values {
      YES("Yes"),
      NO("No"),
      JS_NONE("0 - none"),
      JS_X("2 - joystick X"),
      JS_Y("3 - joystick Y"),
      JS_RIGHT_WHEEL("22 - right wheel"),
      JS_LEFT_WHEEL("23 - left wheel"),
      SPIM_ARMED("Armed"),
      SPIM_RUNNING("Running"),
      SPIM_IDLE("Idle"),
      SAM_DISABLED("0 - Disabled"),
      SAM_ENABLED("1 - Enabled"),
      SAM_TRIANGLE("1 - Triangle"),
      DO_IT("Do it"),
      DO_SSZ("Z - save settings to card (partial)"),
      INTERNAL("INTERNAL"),
      EXTERNAL("EXTERNAL"),
      ;
      private final String text;
      Values(String text) {
         this.text = text;
      }
      @Override
      public String toString() {
         return text;
      }
   }
   
   /**
    * Constructor.
    * @param devices
    * @author Jon
    */
   public Properties (Devices devices) {
      core_ = MMStudioMainFrame.getInstance().getCore();
      devices_ = devices;
      listeners_ = new ArrayList<UpdateFromPropertyListenerInterface>();
   }

   /**
    * sees if property exists in given device
    * @param device enum key for device 
    * @param name enum key for property 
    * @param ignoreError false (default) will do error checking, true means ignores non-existing property
    * @return
    */
   boolean hasProperty(Devices.Keys device, Properties.Keys name, boolean ignoreError) {
      String mmDevice = null;
      try {
         if (ignoreError) {
            mmDevice = devices_.getMMDevice(device);
            return ((mmDevice!=null) &&  core_.hasProperty(mmDevice, name.toString()));
         } else {
            mmDevice = devices_.getMMDeviceException(device);
            return core_.hasProperty(mmDevice, name.toString());
         }
      } catch (Exception ex) {
         ReportingUtils.showError("Couldn't find property "+ name.toString() + " in device " + mmDevice);
      }
      return false;
   }
   
   /**
    * sees if property exists in given device, with error checking
    * @param device enum key for device 
    * @param name enum key for property 
    * @return
    */
   boolean hasProperty(Devices.Keys device, Properties.Keys name) {
      return hasProperty(device, name, false);
   }
   
   /**
    * writes string property value to the device adapter using a core call
    * @param device enum key for device 
    * @param name enum key for property 
    * @param strVal value in string form, sent to core using setProperty()
    * @param ignoreError false (default) will do error checking, true means ignores non-existing property
    */
   public void setPropValue(Devices.Keys device, Properties.Keys name, String strVal, boolean ignoreError) {
      String mmDevice = null;
      try {
         if (ignoreError) {
            mmDevice = devices_.getMMDevice(device);
            if (mmDevice != null) {
               core_.setProperty(mmDevice, name.toString(), strVal);
            }
         } else { 
            mmDevice = devices_.getMMDeviceException(device);
            core_.setProperty(mmDevice, name.toString(), strVal);
         }
      } catch (Exception ex) {
         ReportingUtils.showError("Error setting string property "+ name.toString() + " to " + strVal + " in device " + mmDevice);
      }
   }
   
   /**
    * writes string property value to the device adapter using a core call
    * @param device enum key for device 
    * @param name enum key for property 
    * @param val value in Properties.Values enum form, sent to core using setProperty() after toString() call
    * @param ignoreError false (default) will do error checking, true means ignores non-existing property
    */
   public void setPropValue(Devices.Keys device, Properties.Keys name, Properties.Values val, boolean ignoreError) {
      String mmDevice = null;
      try {
         if (ignoreError) {
            mmDevice = devices_.getMMDevice(device);
            if (mmDevice != null) {
               core_.setProperty(mmDevice, name.toString(), val.toString());
            }
         } else { 
            mmDevice = devices_.getMMDeviceException(device);
            core_.setProperty(mmDevice, name.toString(), val.toString());
         }
      } catch (Exception ex) {
         ReportingUtils.showError("Error setting string property "+ name.toString() + " to " + val.toString() + " in device " + mmDevice);
      }
   }
   
   /**
    * writes string property value to the device adapter using a core call, with error checking
    * @param device enum key for device 
    * @param name enum key for property 
    * @param strVal value in string form, sent to core using setProperty()
    */
   public void setPropValue(Devices.Keys device, Properties.Keys name, String strVal) {
      setPropValue(device, name, strVal, false);
   }
   
   /**
    * writes string property value to the device adapter using a core call, with error checking
    * @param device enum key for device 
    * @param name enum key for property 
    * @param val value in Properties.Values enum form, sent to core using setProperty() after toString() call
    */
   public void setPropValue(Devices.Keys device, Properties.Keys name, Properties.Values val) {
      setPropValue(device, name, val.toString(), false);
   }
 
   /**
    * writes integer property value to the device adapter using a core call
    * @param device enum key for device 
    * @param name enum key for property 
    * @param intVal value in integer form, sent to core using setProperty()
    * @param ignoreError false (default) will do error checking, true means ignores non-existing property
    */
   public void setPropValue(Devices.Keys device, Properties.Keys name, int intVal, boolean ignoreError) {
      String mmDevice = null;
      try {
         if (ignoreError) {
            mmDevice = devices_.getMMDevice(device);
            if (mmDevice != null) {
               core_.setProperty(mmDevice, name.toString(), intVal);
            }
         } else {
            mmDevice = devices_.getMMDeviceException(device);
            core_.setProperty(mmDevice, name.toString(), intVal);
         }
      } catch (Exception ex) {
         ReportingUtils.showError("Error setting int property " + name.toString() + " in device " + mmDevice);
      }
   }
   
   /**
    * writes integer property value to the device adapter using a core call, with error checking
    * @param device enum key for device 
    * @param name enum key for property 
    * @param intVal value in integer form, sent to core using setProperty()
    */
   public void setPropValue(Devices.Keys device, Properties.Keys name, int intVal) {
      setPropValue(device, name, intVal, false);
   }

   /**
    * writes float property value to the device adapter using a core call
    * @param device enum key for device 
    * @param name enum key for property 
    * @param intVal value in integer form, sent to core using setProperty()
    * @param ignoreError false (default) will do error checking, true means ignores non-existing property
    */
   public void setPropValue(Devices.Keys device, Properties.Keys name, float floatVal, boolean ignoreError) {
      String mmDevice = null;
      try {
         if (ignoreError) {
            mmDevice = devices_.getMMDevice(device);
            if (mmDevice != null) {
               core_.setProperty(mmDevice, name.toString(), floatVal);
            }
         } else {
            mmDevice = devices_.getMMDeviceException(device);
            core_.setProperty(mmDevice, name.toString(), floatVal);
         }
      } catch (Exception ex) {
         ReportingUtils.showError("Error setting float property " + name.toString() + " in device " + mmDevice);
      }
   }
   
   /**
    * writes float property value to the device adapter using a core call, with error checking
    * @param device enum key for device 
    * @param name enum key for property 
    * @param intVal value in integer form, sent to core using setProperty()
    */
   public void setPropValue(Devices.Keys device, Properties.Keys name, float floatVal) {
      setPropValue(device, name, floatVal, false);
   }

   /**
    * reads the property value from the device adapter using a core call
    * @param device enum key for device 
    * @param name enum key for property 
    * @return value in string form, returned from core call to getProperty()
    */
   private String getPropValue(Devices.Keys device, Properties.Keys name, boolean ignoreError) {
      String val = null;
      String mmDevice = null;
      try {
         if (ignoreError) {
            mmDevice = devices_.getMMDevice(device);
            val = "";  // set to be empty string to avoid null pointer exceptions
            if (mmDevice != null) {
               val = core_.getProperty(mmDevice, name.toString());
            }
         } else {
            mmDevice = devices_.getMMDeviceException(device);
            val = core_.getProperty(mmDevice, name.toString());
         }
         
      } catch (Exception ex) {
         ReportingUtils.showError("Could not get property " + name.toString() + " from device " + mmDevice);
      }
      return val;
   }

   /**
    * returns a string value for the specified property (assumes the caller 
    * knows the property contains an string)
    * 
    * @param device enum key for device 
    * @param name enum key for property 
    * @return
    * @throws ParseException
    */
   public String getPropValueString(Devices.Keys device, Properties.Keys name) {
      return getPropValue(device, name, true);
   }
   
   /**
    * returns a string value for the specified property (assumes the caller 
    * knows the property contains an string)
    * 
    * @param device enum key for device 
    * @param name enum key for property 
    * @param ignoreError false (default) will do error checking, true means ignores non-existing property
    * @return
    * @throws ParseException
    */
   public String getPropValueString(Devices.Keys device, Properties.Keys name, 
           boolean ignoreError) {
      return getPropValue(device, name, ignoreError);
   }
   
   /**
    * returns an integer value for the specified property (assumes the caller knows the property contains an integer)
    * @param device enum key for device 
    * @param name enum key for property 
    * @return
    * @throws ParseException
    */
   public int getPropValueInteger(Devices.Keys device, Properties.Keys name) {
      int val = 0;
      String strVal = null;
      try {
         strVal = getPropValue(device, name, true);
         if (!strVal.equals("")) {
            val = NumberUtils.coreStringToInt(strVal);
         }
      } catch (ParseException ex) {
         ReportingUtils.showError("Could not parse int value of " + strVal + " for " + name.toString() + " in device " + device.toString());
      } catch (NullPointerException ex) {
         ReportingUtils.showError("Null Pointer error in function getPropValueInteger");
      }
      return val;
   }
   

   /**
    * returns an integer value for the specified property (assumes the caller knows the property contains an integer)
    * @param device enum key for device 
    * @param name enum key for property 
    * @param ignoreError false (default) will do error checking, true means ignores non-existing property
    * @return
    * @throws ParseException
    */
   public int getPropValueInteger(Devices.Keys device, Properties.Keys name, boolean ignoreError) {
      int val = 0;
      String strVal = null;
      try {
         strVal = getPropValue(device, name, ignoreError);
         if (!ignoreError || !strVal.equals("")) {
            val = NumberUtils.coreStringToInt(strVal);
         }
      } catch (ParseException ex) {
         ReportingUtils.showError("Could not parse int value of " + strVal + " for " + name.toString() + " in device " + device.toString());
      } catch (NullPointerException ex) {
         ReportingUtils.showError("Null Pointer error in function getPropValueInteger");
      }
      return val;
   }

   /**
    * returns an float value for the specified property (assumes the caller knows the property contains a float)
    * @param device enum key for device 
    * @param name enum key for property 
    * @return
    * @throws ParseException
    */
   public float getPropValueFloat(Devices.Keys device, Properties.Keys name) {
      float val = 0;
      String strVal = null;
      try {
         strVal = getPropValue(device, name, true);
         if (!strVal.equals("")) {
            val = (float)NumberUtils.coreStringToDouble(strVal);
         }
      } catch (ParseException ex) {
         ReportingUtils.showError("Could not parse int value of " + strVal + " for " + name.toString() + " in device " + device.toString());
      } catch (NullPointerException ex) {
         ReportingUtils.showError("Null Pointer error in function getPropValueFLoat");
      }
      return val;
   }
   
   /**
   * returns an float value for the specified property (assumes the caller knows the property contains a float)
   * @param device enum key for device 
   * @param name enum key for property 
   * @return
   * @throws ParseException
   */
  public float getPropValueFloat(Devices.Keys device, Properties.Keys name, boolean ignoreError) {
     float val = 0;
     String strVal = null;
     try {
        strVal = getPropValue(device, name, ignoreError);
        if (!ignoreError || !strVal.equals("")) {
           val = (float)NumberUtils.coreStringToDouble(strVal);
        }
     } catch (ParseException ex) {
        ReportingUtils.showError("Could not parse int value of " + strVal + " for " + name.toString() + " in device " + device.toString());
     } catch (NullPointerException ex) {
        ReportingUtils.showError("Null Pointer error in function getPropValueFLoat");
     }
     
     return val;
  }
  
  
  /**
   * Used to add classes implementing DeviceListenerInterface as listeners
   */
  public void addListener(UpdateFromPropertyListenerInterface listener) {
     listeners_.add(listener);
  }

  /**
   * Remove classes implementing the DeviceListener interface from the listers
   *
   * @param listener
   */
  public void removeListener(UpdateFromPropertyListenerInterface listener) {
     listeners_.remove(listener);
  }
  
  /**
   * Call each listener in succession to alert them that something changed
   */
  public void callListeners() {
     for (UpdateFromPropertyListenerInterface listener : listeners_) {
        listener.updateFromProperty();
     }
  }
   

}