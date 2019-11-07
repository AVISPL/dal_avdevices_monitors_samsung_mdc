package com.avispl.symphony.dal.communicator.samsung.mdc;

public class SamsungMDCUtils {

    private final static char HEADER = 0xAA;

    //Calculate an xor checksum of a byte[], return a byte
    static byte checkSum(byte bytes[]){
        byte checkSum = 0;

        for( byte s : bytes){
            checkSum = (byte) (checkSum + s);
        }

        return checkSum;
    }

    //print a String in HEX format
    protected static String getHexString(String str){
        StringBuilder sBld = new StringBuilder();

        int j =0;
        sBld.append("[");
        for(byte b: str.getBytes()) {
            sBld.append(String.format("%02x",b));
            j++;
            if(j<str.length())
                sBld.append(", ");
        }
        sBld.append("]");

        return sBld.toString();
    }

    static String buildSendString(char monitorID, char command){
        return buildSendString(monitorID,command,null);
    }

    static String buildSendString(char monitorID, char command, String param) {

        //build message first as it's size is required in header
        String message = null;

        if(param != null) {
            message = Character.toString(command) + Character.toString(monitorID) + (char)param.length() + param;
        }else{
            message = Character.toString(command) + Character.toString(monitorID) + (char) 0x00;
        }

        message += (char) checkSum(message.getBytes());

        return HEADER + message ;
    }
}
