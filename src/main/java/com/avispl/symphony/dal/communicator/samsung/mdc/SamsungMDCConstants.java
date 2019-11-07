package com.avispl.symphony.dal.communicator.samsung.mdc;

import java.util.HashMap;
import java.util.Map;

class SamsungMDCConstants {

    enum powerStatusNames {ON,OFF}
    final static Map<powerStatusNames,Character> powerStatus = new HashMap<powerStatusNames,Character>(){{
        put(powerStatusNames.ON, (char)0x01);
        put(powerStatusNames.OFF, (char)0x00);
    }};

    enum commandNames{POWER,INPUT,STATUS}
    final static Map<commandNames, Character> commands = new HashMap<commandNames,Character>(){{
        put(commandNames.POWER, (char)0x11);
        put(commandNames.INPUT, (char)0x14);
        put(commandNames.STATUS, (char)0x0D);
    }};

    enum inputNames {OFF,HDMI_1,HDMI_2,PC,DVI,DVI_VIDEO,COMPONENT,RF,DTV,DISPLAYPORT,MAGIC_INFO}
    final static Map<inputNames, Character> inputs = new HashMap<inputNames, Character>() {{
        put(inputNames.OFF, (char)0x01);
        put(inputNames.HDMI_1, (char)0x21);
        put(inputNames.HDMI_2, (char)0x23);
        put(inputNames.PC, (char)0x15);
        put(inputNames.DVI, (char)0x18);
        put(inputNames.DVI_VIDEO, (char)0x1F);
        put(inputNames.COMPONENT, (char)0x08);
        put(inputNames.RF, (char)0x30);
        put(inputNames.DTV, (char)0x40);
        put(inputNames.DISPLAYPORT, (char)0x25);
        put(inputNames.MAGIC_INFO, (char)0x20);
    }};

    enum statusCodeNames{NORMAL,ERROR,UNKNOWN}
    final static Map<Character,statusCodeNames> statusCodes = new HashMap<Character,statusCodeNames>(){{
       put((char)0x00,statusCodeNames.NORMAL);
       put((char)0x01,statusCodeNames.ERROR);
        put((char)0x02,statusCodeNames.UNKNOWN);
    }};

    enum statusNames{LAMP,TEMPERATURE_CODE,BRIGHTNESS_SENSOR,NO_SYNC,TEMPERATURE,FAN}
}
