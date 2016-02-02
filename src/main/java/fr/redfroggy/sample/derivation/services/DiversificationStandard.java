package fr.redfroggy.sample.derivation.services;

import com.google.common.primitives.Bytes;
import fr.redfroggy.sample.derivation.exception.DiversificationException;
import fr.redfroggy.sample.derivation.security.Algorithm;
import fr.redfroggy.sample.derivation.utils.BytesUtils;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * This class implement AN10922 diversification standard
 */
@Slf4j
public class DiversificationStandard extends AbstractDiversification {

    /**
     * AN10922 AES 128 Bits constant
     */
    protected static final byte AES128_DIV_CONSTANT = (byte) 0x01;

    /**
     * AN10922 AES 192 Bits constant #1
     */
    protected static final byte AES192_DIV_CONSTANT1 = (byte) 0x11;

    /**
     * AN10922 AES 192 Bits constant #2
     */
    protected static final byte AES192_DIV_CONSTANT2 = (byte) 0x12;

    /**
     * Create diversification process
     *
     * @param standard Standard to use
     * @throws DiversificationException
     */
    public DiversificationStandard(Standard standard) throws DiversificationException {
        super(standard);
    }

    /**
     * Diversify a key for a card
     *
     * @param originalKey      Key to diversify
     * @param keyIndex         Key index
     * @param uid              Card UID
     * @param aid              Application ID
     * @param systemIdentifier System Identifier
     * @return Diversified key
     * @throws DiversificationException
     */
    public byte[] diversify(byte[] originalKey, int keyIndex, byte[] uid, byte[] aid, byte[] systemIdentifier) throws DiversificationException {
        return diversify(originalKey, getDivSequence(keyIndex, uid, aid, systemIdentifier));
    }

    /**
     * Diversify a key with div sequence
     *
     * @param key Key to diversify
     * @param div Diversification sequence
     * @return Diversified key
     * @throws DiversificationException
     */
    protected byte[] diversify(byte[] key, byte[] div) throws DiversificationException {
        switch (standard) {
            case AN10922_AES128:
                return diversifyAN10922AES128(key, div);
            case AN10922_AES192:
                return diversifyAN10922AES192(key, div);
            case AN0148_3DES:
            case AN0148_DES:
            case AN0148_AES:
                return diversifyAN0148(key, div);
            default:
                throw new DiversificationException("Unknown diversification standard");
        }
    }

    /**
     * Diversify a key with div sequence (AN10922 mode)
     *
     * @param key Key to diversify
     * @param div Diversification sequence
     * @return Diversified key
     * @throws DiversificationException
     */
    protected byte[] diversifyAN10922AES128(byte[] key, byte[] div) throws DiversificationException {
        try {

            log.info("===== AN10922 DIVERSIFICATION ======");
            log.info("ALGO: {}", algorithm.toString());
            log.info("K: {}", BytesUtils.bytesToHex(key));
            byte[] divKey = diversifyAN10922(key, div);
            log.info("K': {}", BytesUtils.bytesToHex(divKey));
            log.info("====================================");

            return divKey;

        } catch (Exception e) {
            throw new DiversificationException("Cannot diversify key (AN10922)", e);
        }
    }


    /**
     * Diversify a key with div sequence (AN10922 mode) AES 192 bits
     *
     * @param key Key to diversify
     * @param div Diversification sequences (D1 || D2)
     * @return Diversified key
     * @throws DiversificationException
     */
    protected byte[] diversifyAN10922AES192(byte[] key, byte[] div) throws DiversificationException {
        try {

            if (Algorithm.AES192.equals(algorithm) && 192 > Cipher.getMaxAllowedKeyLength(algorithm.getKeyAlgorithm())) {
                throw new DiversificationException("AES 192 is not available");
            }

            byte[] divKey;
            byte[] d1 = Arrays.copyOfRange(div, 0, div.length / 2);
            byte[] d2 = Arrays.copyOfRange(div, div.length / 2, div.length);

            log.info("===== AN10922 DIVERSIFICATION ======");
            log.info("ALGO: {}", algorithm.toString());
            log.info("K: {}", BytesUtils.bytesToHex(key));

            log.info("--- D1 ---");
            byte[] divKey1 = diversifyAN10922(key, d1);
            log.info("Ka': {}", BytesUtils.bytesToHex(divKey1));
            log.info("--- D2 ---");
            byte[] divKey2 = diversifyAN10922(key, d2);
            log.info("Kb': {}", BytesUtils.bytesToHex(divKey2));
            log.info("----------");

            divKey = Bytes.concat(
                    Arrays.copyOfRange(divKey1, 0, 8),
                    BytesUtils.xor(Arrays.copyOfRange(divKey1, 8, 16), Arrays.copyOfRange(divKey2, 0, 8)),
                    Arrays.copyOfRange(divKey2, 8, 16));

            log.info("K': {}", BytesUtils.bytesToHex(divKey));
            log.info("====================================");

            return divKey;

        } catch (Exception e) {
            throw new DiversificationException("Cannot diversify key (AN10922)", e);
        }
    }

    /**
     * Diversify a key with div sequence (AN10922 mode)
     *
     * @param key Key to diversify
     * @param div Diversification sequence
     * @return Diversified key
     * @throws DiversificationException
     */
    protected byte[] diversifyAN10922(byte[] key, byte[] div) throws DiversificationException {
        try {

            SecretKeySpec secretKey = new SecretKeySpec(key, algorithm.getKeyAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(new byte[algorithm.getBlocSize()]));

            byte[] finalDiv = div;
            boolean padded = false;
            if (div.length != 32) {
                finalDiv = BytesUtils.pad(Bytes.concat(div, new byte[]{(byte) 0x80}), 32);
                padded = true;
            }

            // Generate SubKeys
            // Let L = CIPHK(0b)
            byte[] lKey = cipher.doFinal(new byte[algorithm.getBlocSize()]);
            byte[] subKey1 = generateSubKey(lKey);
            byte[] subKey2 = generateSubKey(subKey1);

            // CMAC
            byte[] cmac = cmac(finalDiv, subKey1, subKey2, padded);

            // Last 16-byte block. (CMAC)
            byte[] divKey = Arrays.copyOfRange(cmac, 16, 32);

            log.info("K0: {}", BytesUtils.bytesToHex(lKey));
            log.info("K1: {}", BytesUtils.bytesToHex(subKey1));
            log.info("K2: {}", BytesUtils.bytesToHex(subKey2));
            log.info("M: {}", BytesUtils.bytesToHex(div));

            return divKey;

        } catch (Exception e) {
            throw new DiversificationException("Cannot diversify key (AN10922)", e);
        }
    }

    /**
     * Diversify a key with div sequence (AN0148 mode)
     *
     * @param key Key to diversify
     * @param div Diversification sequence
     * @return Diversified key
     * @throws DiversificationException
     */
    protected byte[] diversifyAN0148(byte[] key, byte[] div) throws DiversificationException {

        try {
            byte[] tmpKey;
            byte[] toDivKey = key;

            int keySize = key.length;

            if (!algorithm.equals(Algorithm.AES)) {
                if (keySize == 8) {
                    toDivKey = Bytes.concat(key, key, key);
                } else if (keySize == 16) {
                    toDivKey = Bytes.concat(key, Arrays.copyOfRange(key, 0, 8));
                }
            }

            SecretKeySpec secretKey = new SecretKeySpec(toDivKey, algorithm.getKeyAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(new byte[div.length]));

            if (algorithm.equals(Algorithm.AES)) {
                toDivKey = BytesUtils.xor(toDivKey, div);
                tmpKey = cipher.doFinal(toDivKey);

            } else {
                byte[] key1 = Arrays.copyOfRange(toDivKey, 0, 8);
                key1 = BytesUtils.xor(key1, div);
                byte[] divKey1 = cipher.doFinal(key1);

                byte[] key2 = Arrays.copyOfRange(toDivKey, 8, 16);
                key2 = BytesUtils.xor(key2, divKey1);
                byte[] divKey2 = cipher.doFinal(key2);

                tmpKey = Bytes.concat(divKey1, divKey2);
            }

            byte[] divKey = Arrays.copyOf(tmpKey, keySize);


            log.info("====== AN0148 DIVERSIFICATION ======");
            log.info("ALGO: {}", algorithm.toString());
            log.info("K: {}", BytesUtils.bytesToHex(key));
            log.info("M: {}", BytesUtils.bytesToHex(div));
            log.info("K': {}", BytesUtils.bytesToHex(divKey));
            log.info("====================================");

            return divKey;

        } catch (Exception e) {
            throw new DiversificationException("Cannot diversify key (AN0148)", e);
        }
    }

    /**
     * Generate Subkey
     *
     * @return Sub key
     * @throws GeneralSecurityException
     */
    protected byte[] generateSubKey(byte[] key) throws GeneralSecurityException {

        byte[] subKey = BytesUtils.shiftLeft(key);
        int msbL = (key[0] & 0xff) >> 7;
        if (msbL == 1) {
            subKey[15] = (byte) (subKey[15] ^ (byte) 0x87);
        }

        return subKey;
    }

    /**
     * Compute a CMAC string
     *
     * @param d      D value
     * @param k1     Sub key 1
     * @param k2     Sub key 2
     * @param padded Is the div sequence is padded ?
     * @return cmac value
     * @throws GeneralSecurityException
     */
    protected byte[] cmac(byte[] d, byte[] k1, byte[] k2, boolean padded) throws GeneralSecurityException {

        /**
         * Last 16-byte is XORed with K2 if padding is added, otherwise XORed with K1
         */
        byte[] inputMSB = Arrays.copyOfRange(d, 0, 16);
        byte[] inputLSB = Arrays.copyOfRange(d, 16, 32);

        if (padded) {
            inputLSB = BytesUtils.xor(inputLSB, k2);
        } else {
            inputLSB = BytesUtils.xor(inputLSB, k1);
        }

        return cipher.doFinal(Bytes.concat(inputMSB, inputLSB));
    }

    /**
     * Create diversification string
     *
     * @param keyIndex Key index
     * @param csn      Csn
     * @param aid      Application ID
     * @param sysId    System Identifier
     * @return Diversification string
     */
    protected byte[] getDivSequence(int keyIndex, byte[] csn, byte[] aid, byte[] sysId) {
        if (standard.equals(Standard.AN10922_AES128)) {
            return Bytes.concat(new byte[]{AES128_DIV_CONSTANT}, csn, BytesUtils.reverseBytes(aid), sysId);
        } else if (standard.equals(Standard.AN10922_AES192)) {
            byte[] divSequence1 = Bytes.concat(new byte[]{AES192_DIV_CONSTANT1}, csn, BytesUtils.reverseBytes(aid), sysId);
            byte[] divSequence2 = Bytes.concat(new byte[]{AES192_DIV_CONSTANT2}, csn, BytesUtils.reverseBytes(aid), sysId);
            return Bytes.concat(divSequence1, divSequence2);
        } else if (standard.equals(Standard.AN0148_AES)) {
            byte[] divSequence = BytesUtils.pad(Bytes.concat(new byte[]{(byte) keyIndex}, csn), 8);
            return Bytes.concat(divSequence, divSequence);
        } else {
            return BytesUtils.pad(Bytes.concat(new byte[]{(byte) keyIndex}, csn), 8);
        }
    }
}
