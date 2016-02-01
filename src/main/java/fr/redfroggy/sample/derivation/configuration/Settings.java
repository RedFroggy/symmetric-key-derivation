package fr.redfroggy.sample.derivation.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Settings
 */
@Component
@ConfigurationProperties
@Data
public class Settings {

    /**
     * Derivation standard to use
     * default: AN10922_AES128
     */
    @NotNull
    protected String standard = "AN10922_AES128";

    /**
     * Key to diversify
     * default: 00000000000000000000000000000000
     */
    @NotNull
    protected String key = "00000000000000000000000000000000";

    /**
     * Key to diversify
     * default: 00000000000000000000000000000000
     */
    @NotNull
    protected String uid = "00000000000000";

    /**
     * Key to diversify (AN10922 only)
     * default: 000000
     */
    protected String aid = "000000";

    /**
     * Key to diversify (AN10922 only)
     * default: 00
     */
    protected String systemIdentifier = "00";

    /**
     * Index of key to diversify (AN0148 only)
     * default: 0
     */
    protected int keyIndex = 0;

}
