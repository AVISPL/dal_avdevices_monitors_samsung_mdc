/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.samsung.mdc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SamsungMDCUtils {

    private final static byte HEADER = (byte)0xAA;

    /**
     * This method is used to calculate the checksum of a byte array
     * @param bytes This is the list of bytes against which the checksum should be calculated
     * @return byte This returns the calculated checksum.
     */
    static byte checkSum(byte bytes[]){
        int checksum = 0;

        for(int i = 0;i < bytes.length;i++){
            checksum = checksum + bytes[i]& 0xFF;
        }

        return (byte) checksum;
    }

    /**
     * This method is used to convert a String in HEX format (for printing purposes)
     * @param str This is the String to be converted in Hex format
     * @return String This returns the converted String
     */
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

    protected static final char[] hexArray = "0123456789ABCDEF".toCharArray();


    public static String getHexByteString(byte[] bytes) throws IOException {
        return getHexByteString((CharSequence)null, ",", (CharSequence)null, bytes);
    }

    public static String getHexByteString(CharSequence prefix, CharSequence separator, CharSequence suffix, byte[] bytes) throws IOException {
        byte[] data = bytes;
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < data.length; ++i) {
            if (i > 0) {
                sb.append(separator);
            }

            int v = data[i] & 255;
            if (prefix != null) {
                sb.append(prefix);
            }

            sb.append(hexArray[v >> 4]);
            sb.append(hexArray[v & 15]);
            if (suffix != null) {
                sb.append(suffix);
            }
        }

        return sb.toString();
    }

    /**
     * This method is used to build a string to be sent according to the NEC Protocol (See bellow)
     */
    static byte[] buildSendString(byte monitorID, byte command){
        return buildSendString(monitorID,command,null);
    }

    /**
     * This method is used to build a string to be sent according to the Samsung Protocol
     * @param monitorID This is byte representing the monitor ID
     * @param command This is the byte array reprensenting the command to be sent
     * @param param This is the byte array reprensenting the parameter values to be sent
     * @return byte[] This returns the string to be sent to the display
     */
    static byte[] buildSendString(byte monitorID, byte command, byte[] param) {
        List<Byte> bytes = new ArrayList<>();

        bytes.add(command);
        bytes.add((byte)monitorID);

        if(param != null) {

            bytes.add((byte)param.length);

            for(byte b:param){
                bytes.add(b);
            }
        }else{
            bytes.add((byte)0x00);
        }

        byte message[] = new byte[bytes.size()];

        for(int i = 0;i<bytes.size();i++)
        {
            message[i] = bytes.get(i);
        }

        bytes.add(checkSum(message));

        bytes.add(0,HEADER);

        bytes.add((byte)0x0D);
        bytes.add((byte)0x0A);

        byte byteArray[] = new byte[bytes.size()];

        for(int i = 0;i<bytes.size();i++)
        {
            byteArray[i] = bytes.get(i);
        }

        return byteArray ;
    }
}
