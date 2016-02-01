package fr.redfroggy.sample.derivation.services;

import fr.redfroggy.sample.derivation.exception.DiversificationException;
import fr.redfroggy.sample.derivation.security.Algorithm;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;

/**
 * This class implement bases of a diversification system
 */
@Slf4j
public abstract class AbstractDiversification {

    /**
     * Diversification standards
     */
    public enum Standard {
        AN10922_AES128(Algorithm.AES),
        AN0148_DES(Algorithm.DES),
        AN0148_3DES(Algorithm.TDES),
        AN0148_AES(Algorithm.AES);

        private Algorithm algorithm;

        /**
         * Create diversificaton standard
         *
         * @param algorithm Algorithm supported
         */
        Standard(Algorithm algorithm) {
            this.algorithm = algorithm;
        }

        /**
         * Return algorithm of standard
         *
         * @return algorithm
         */
        public Algorithm getAlgorithm() {
            return algorithm;
        }
    }

    /**
     * Diversification standard
     */
    @Getter
    protected Standard standard;

    /**
     * Cipher process
     */
    protected Cipher cipher;

    /**
     * Algorithm used
     */
    protected Algorithm algorithm;

    /**
     * Create diversification process
     *
     * @param standard Standard to use
     * @throws DiversificationException
     */
    public AbstractDiversification(Standard standard) throws DiversificationException {
        try {
            this.standard = standard;
            this.algorithm = standard.getAlgorithm();
            this.cipher = Cipher.getInstance(algorithm.getCipherAlgorithm());
        } catch (Exception e) {
            throw new DiversificationException("Cannot create instance of diversification process", e);
        }
    }
}
