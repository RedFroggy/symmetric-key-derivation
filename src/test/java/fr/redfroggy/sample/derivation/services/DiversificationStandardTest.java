package fr.redfroggy.sample.derivation.services;

import fr.redfroggy.sample.derivation.exception.DiversificationException;
import fr.redfroggy.sample.derivation.utils.BytesUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test of AN10922 and AN0148 implementation
 * See document AN10922 of NXP
 * See document "iCLASS HF Migration Reader Key Diversification – AN0148, A.0"
 */
@RunWith(MockitoJUnitRunner.class)
public class DiversificationStandardTest {

    @Before
    public void setUp() throws Exception {
    }

    /**
     * NXP AN10922 / §2.2.1 : AES-128 key diversification example
     */
    @Test
    public void AN10922_AES128_Nominal() throws Exception {

        byte[] key = BytesUtils.hexToBytes("00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF");
        byte[] uid = BytesUtils.hexToBytes("04:78:2E:21:80:1D:80");
        byte[] aid = BytesUtils.hexToBytes("F5:42:30");
        byte[] seed = BytesUtils.hexToBytes("4E:58:50:20:41:62:75");
        // Not necessary in AN10922
        int keyIndex = 0;

        DiversificationStandard div = new DiversificationStandard(AbstractDiversification.Standard.AN10922_AES128);
        byte[] divKey = div.diversify(key, keyIndex, uid, aid, seed);

        Assert.assertArrayEquals(BytesUtils.hexToBytes("A8:DD:63:A3:B8:9D:54:B3:7C:A8:02:47:3F:DA:91:75"), divKey);
    }

    /**
     * NXP AN10922 / §2.3.1 : AES-192 key diversification example
     */
    @Test
    public void AN10922_AES192_Nominal() throws Exception {

        byte[] key = BytesUtils.hexToBytes("00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:01:02:03:04:05:06:07:08");
        byte[] uid = BytesUtils.hexToBytes("04:78:2E:21:80:1D:80");
        byte[] aid = BytesUtils.hexToBytes("F5:42:30");
        byte[] seed = BytesUtils.hexToBytes("4E:58:50:20:41:62:75");
        // Not necessary in AN10922
        int keyIndex = 0;

        DiversificationStandard div = new DiversificationStandard(AbstractDiversification.Standard.AN10922_AES192);
        byte[] divKey = div.diversify(key, keyIndex, uid, aid, seed);

        Assert.assertArrayEquals(BytesUtils.hexToBytes("CE39C8E1CD82D9A7BEDBE9D74AF59B23176755EE7586E12C"), divKey);
    }

    /**
     * NXP AN10922 / §2.2.1 : AES-128 key diversification example
     */
    @Test
    public void AN10922_AES128_DiversificationByKeyIDAnd20Padding() throws Exception {

        byte[] key = BytesUtils.hexToBytes("00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF");
        byte[] uid = BytesUtils.hexToBytes("11:22:33:44:55:66:77");
        byte[] aid = BytesUtils.hexToBytes("AA:BB:CC");
        byte[] seed = BytesUtils.hexToBytes("01:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00");
        // Not necessary in AN10922
        int keyIndex = 0;

        DiversificationStandard div = new DiversificationStandard(AbstractDiversification.Standard.AN10922_AES128);
        byte[] divKey = div.diversify(key, keyIndex, uid, aid, seed);

        Assert.assertArrayEquals(BytesUtils.hexToBytes("DB:29:A5:E1:7D:94:14:DE:4B:E5:C0:B1:0B:49:A1:D2"), divKey);
    }

    /**
     * NXP AN10922 / §2.2.1 : AES-128 key diversification example
     */
    @Test(expected = DiversificationException.class)
    public void AN10922_AES128_BadKeyLength() throws Exception {

        byte[] key = BytesUtils.hexToBytes("FF");
        byte[] uid = BytesUtils.hexToBytes("04:78:2E:21:80:1D:80");
        byte[] aid = BytesUtils.hexToBytes("F5:42:30");
        byte[] seed = BytesUtils.hexToBytes("4E:58:50:20:41:62:75");
        // Not necessary in AN10922
        int keyIndex = 0;

        DiversificationStandard div = new DiversificationStandard(AbstractDiversification.Standard.AN10922_AES128);
        div.diversify(key, keyIndex, uid, aid, seed);
    }

    /**
     * HID AN1048 / §5.1. Example: Single DES
     */
    @Test
    public void AN0148_DES_Nominal() throws Exception {

        byte[] key = BytesUtils.hexToBytes("01:23:45:67:89:AB:CD:EF");
        int keyIndex = 1;
        byte[] uid = BytesUtils.hexToBytes("04:5E:75:A9:C1:25:80");
        // Not necessary in AN0148
        byte[] aid = new byte[0];
        byte[] seed = new byte[0];

        DiversificationStandard div = new DiversificationStandard(AbstractDiversification.Standard.AN0148_DES);
        byte[] divKey = div.diversify(key, keyIndex, uid, aid, seed);

        Assert.assertArrayEquals(BytesUtils.hexToBytes("1A:94:52:25:B3:FA:A4:45"), divKey);
    }

    /**
     * HID AN1048 / 5.2. Example: Two-key 3DES
     */
    @Test
    public void AN0148_3DES_Nominal() throws Exception {

        byte[] key = BytesUtils.hexToBytes("01:23:45:67:89:AB:CD:EF:FE:DC:BA:98:76:54:32:10");
        int keyIndex = 1;
        byte[] uid = BytesUtils.hexToBytes("04:5E:75:A9:C1:25:80");
        // Not necessary in AN0148
        byte[] aid = new byte[0];
        byte[] seed = new byte[0];

        DiversificationStandard div = new DiversificationStandard(AbstractDiversification.Standard.AN0148_3DES);
        byte[] divKey = div.diversify(key, keyIndex, uid, aid, seed);

        Assert.assertArrayEquals(BytesUtils.hexToBytes("7B:AA:6C:97:BD:A3:6B:FF:24:44:5A:FF:A0:B5:8C:F6"), divKey);
    }

    /**
     * HID AN1048 / 5.3. Example: AES
     */
    @Test
    public void AN0148_AES_Nominal() throws Exception {

        byte[] key = BytesUtils.hexToBytes("01:23:45:67:89:AB:CD:EF:FE:DC:BA:98:76:54:32:10");
        int keyIndex = 1;
        byte[] uid = BytesUtils.hexToBytes("04:6C:75:A9:C1:25:80");
        // Not necessary in AN0148
        byte[] aid = new byte[0];
        byte[] seed = new byte[0];

        DiversificationStandard div = new DiversificationStandard(AbstractDiversification.Standard.AN0148_AES);
        byte[] divKey = div.diversify(key, keyIndex, uid, aid, seed);

        Assert.assertArrayEquals(BytesUtils.hexToBytes("51:9A:50:2E:2F:69:CE:7B:17:C3:BF:B1:2B:30:4D:28"), divKey);
    }

    /**
     * HID AN1048 / 5.3. Example: AES
     */
    @Test(expected = DiversificationException.class)
    public void AN0148_AES_BadKeyLength() throws Exception {

        byte[] key = BytesUtils.hexToBytes("10");
        int keyIndex = 0;
        byte[] uid = BytesUtils.hexToBytes("04:5E:75:A9:C1:25:80");
        // Not necessary in AN0148
        byte[] aid = new byte[0];
        byte[] seed = new byte[0];

        DiversificationStandard div = new DiversificationStandard(AbstractDiversification.Standard.AN0148_AES);
        div.diversify(key, keyIndex, uid, aid, seed);
    }
}
