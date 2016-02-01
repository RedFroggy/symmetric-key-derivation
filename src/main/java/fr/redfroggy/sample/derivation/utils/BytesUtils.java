package fr.redfroggy.sample.derivation.utils;

import com.google.common.primitives.Bytes;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * Tools used to manipulate bytes and bytes array
 *
 * @author Florent PERINEL
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class BytesUtils {

    /**
     * Default delimiter for hexadecimal values
     */
    public static final char DEFAULT_HEXA_DELIMITER = ' ';

    /**
     * Format bytes array to hexadecimal representation
     *
     * @param byteToFormat Byte to format
     * @return hexadecimal representation
     */
    public static String bytesToHex(byte byteToFormat) {
        return bytesToHex(new byte[]{byteToFormat});
    }

    /**
     * Format bytes array to hexadecimal representation
     *
     * @param bytes Bytes array to format
     * @return hexadecimal representation
     */
    public static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, DEFAULT_HEXA_DELIMITER, bytes.length);
    }

    /**
     * Format bytes array to hexadecimal representation
     *
     * @param bytes     Bytes array to format
     * @param separator Char used to separate hex values
     * @return hexadecimal representation
     */
    public static String bytesToHex(byte[] bytes, char separator) {
        return bytesToHex(bytes, separator, bytes.length);
    }

    /**
     * Format bytes array to hexadecimal representation
     *
     * @param bytes      Bytes array to format
     * @param separator  Char used to separate hex values
     * @param bytePerRow Number of byte per row
     * @return hexadecimal representation
     */
    public static String bytesToHex(byte[] bytes, char separator, int bytePerRow) {

        int count = 0;
        StringBuilder hexa = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            if (bytePerRow > 0 && count == bytePerRow) {
                hexa.append('\n');
                count = 0;
            } else if (i > 0) {
                hexa.append(separator);
            }

            hexa.append(String.format("%02X", bytes[i]));
            count++;
        }

        return hexa.toString();
    }

    /**
     * Format hexadecimal representation to bytes array
     *
     * @param hexadecimal hexadecimal representation
     * @return bytes array
     */
    public static byte[] hexToBytes(String hexadecimal) {
        String cleanedHexa = hexadecimal.replaceAll("[^a-fA-F0-9]", "");
        int len = cleanedHexa.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(cleanedHexa.charAt(i), 16) << 4)
                    + Character.digit(cleanedHexa.charAt(i + 1), 16));
        }

        return data;
    }

    /**
     * Compute a CRC 32 for ISO 14443a cards
     *
     * @param data Data to check
     * @return 4 bytes array crc 32
     */
    public static byte[] crc32(byte[] data) {
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(data, 0, data.length);
        long l = crc.getValue();

        byte[] ret = new byte[4];
        for (int i = 0; i < 4; i++) {
            ret[i] = (byte) (l & 0x00000000000000ff);
            ret[i] = (byte) ~ret[i];
            l >>>= 8;
        }

        return ret;
    }

    /**
     * Pad byte array to n*multiple size
     *
     * @param data     Data to pad
     * @param multiple Multiple bloc size
     * @return Data padded
     */
    public static byte[] pad(byte[] data, int multiple) {

        if (multiple <= 0) {
            // No padding
            return data;
        }

        if (data.length % multiple == 0) {
            return data;
        }

        int padding;
        if (data.length < multiple) {
            padding = multiple - data.length;
        } else {
            padding = ((data.length / multiple + 1) * multiple) - data.length;
        }

        return Bytes.concat(data, new byte[padding]);
    }

    /**
     * Trim the padding
     *
     * @param data Data to unpad
     * @return Unpaded data
     */
    public static byte[] unpad(byte[] data) {
        for (int s = data.length - 1; s >= 0; s--) {
            if (data[s] != (byte) 0x00) {
                return Arrays.copyOfRange(data, 0, s + 1);
            }
        }

        return data;
    }

    /**
     * Apply a XOR function on two bytes array an return the result
     *
     * @param data1 Data 1
     * @param data2 Data 2
     * @return Data after XOR
     */
    public static byte[] xor(byte[] data1, byte[] data2) {
        byte[] result = new byte[data1.length < data2.length ? data2.length : data1.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = ((byte) (data1[i] ^ data2[i]));
        }

        return result;
    }

    /**
     * Shift bit left
     *
     * @param data Data to shift
     * @return Shifted data
     */
    public static byte[] shiftLeft(byte[] data) {

        StringBuilder sb = new StringBuilder();

        for (byte b : data) {
            String s = Integer.toBinaryString(0x100 + b);
            sb.append(s.subSequence(s.length() - 8, s.length()));
        }

        String s = sb.toString().substring(1) + "0";

        byte[] a = new byte[s.length() / 8];

        for (int index = 0, i = 0; i < s.length(); index++, i += 8) {
            a[index] = (byte) Integer.parseInt(s.substring(i, i + 8), 2);
        }

        return a;
    }

    /**
     * Reverse byte array
     *
     * @param data Data to reverse
     * @return Reversed data
     */
    public static byte[] reverseBytes(byte[] data) {
        byte[] reversed = ArrayUtils.clone(data);
        ArrayUtils.reverse(reversed);
        return reversed;
    }
}
