package com.superacm.demo.player.util;

import java.security.*;

public class DigestUtil {

    public static String getMD5(String input) {
        try {
            // Create an instance of the MD5 MessageDigest
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Update the digest using the byte array of the input string
            md.update(input.getBytes());

            // Perform the hash computation and get the bytes of the digest
            byte[] digest = md.digest();

            // Convert the byte array into a hexadecimal string
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }

            // Return the MD5 hash as a string
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // Handle the exception if MD5 is not supported
            throw new RuntimeException(e);
        }
    }
}
