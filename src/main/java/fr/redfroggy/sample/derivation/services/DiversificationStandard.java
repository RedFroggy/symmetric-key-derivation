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
                return diversifyAN10922(key, div);
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
    protected byte[] diversifyAN10922(byte[] key, byte[] div) throws DiversificationException {
        try {

            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(new byte[key.length]));

            byte[] finalDiv = div;
            boolean padded = false;
            if (div.length != 32) {
                finalDiv = BytesUtils.pad(Bytes.concat(div, new byte[]{(byte) 0x80}), 32);
                padded = true;
            }

            // Generate SubKeys
            // Let L = CIPHK(0b)
            byte[] lKey = cipher.doFinal(new byte[key.length]);
            byte[] subKey1 = generateSubKey(lKey);
            byte[] subKey2 = generateSubKey(subKey1);

            // CMAC
            byte[] cmac = cmac(finalDiv, subKey1, subKey2, padded);

            // Last 16-byte block. (CMAC)
            byte[] divKey = Arrays.copyOfRange(cmac, 16, 32);

            log.info("===== AN10922 DIVERSIFICATION ======");
            log.info("K: {}", BytesUtils.bytesToHex(key));
            log.info("K0: {}", BytesUtils.bytesToHex(lKey));
            log.info("K1: {}", BytesUtils.bytesToHex(subKey1));
            log.info("K2: {}", BytesUtils.bytesToHex(subKey2));
            log.info("M: {}", BytesUtils.bytesToHex(div));
            log.info("K': {}", BytesUtils.bytesToHex(divKey));
            log.info("====================================");

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
            log.info("K: {}", BytesUtils.bytesToHex(key));
            log.info("ALGO: {}", algorithm.toString());
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
            return Bytes.concat(new byte[]{(byte) 0x01}, csn, BytesUtils.reverseBytes(aid), sysId);
        } else if (standard.equals(Standard.AN0148_AES)) {
            byte[] divSequence = BytesUtils.pad(Bytes.concat(new byte[]{(byte) keyIndex}, csn), 8);
            return Bytes.concat(divSequence, divSequence);
        } else {
            return BytesUtils.pad(Bytes.concat(new byte[]{(byte) keyIndex}, csn), 8);
        }
    }
}
