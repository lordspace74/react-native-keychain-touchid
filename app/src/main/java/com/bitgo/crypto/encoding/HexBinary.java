package com.bitgo.crypto.encoding;

/**
 * Created by arik on 12/14/15.
 */
public class HexBinary {

    public static String binaryToHex(byte[] binary) {
        String hexString = "";
        for (byte b : binary) {
            hexString += String.format("%02x", b);
        }
        return hexString;
    }

    public static byte[] hexToBinary(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
