package com.project.searchengine.utils;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Utility class for compressing and decompressing strings using GZIP.
 */
public class CompressionUtil {

    /**
     * Compresses a string using GZIP and returns the compressed data as a byte
     * array.
     *
     * @param input the string to compress
     * @return the compressed data as a byte array, or null if compression fails
     */
    public static byte[] compress(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                gzipOutputStream.write(input.getBytes("UTF-8"));
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            System.err.println("Error compressing data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Decompresses a byte array using GZIP and returns the original string.
     *
     * @param compressedData the compressed data as a byte array
     * @return the decompressed string, or null if decompression fails
     */
    public static String decompress(byte[] compressedData) {
        if (compressedData == null || compressedData.length == 0) {
            return null;
        }
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192]; // 8KB buffer for faster processing
                int len;
                while ((len = gzipInputStream.read(buffer)) > 0) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                return byteArrayOutputStream.toString("UTF-8");
            }
        } catch (IOException e) {
            System.err.println("Error decompressing data: " + e.getMessage());
            return null;
        }
    }
}
