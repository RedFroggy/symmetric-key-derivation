package fr.redfroggy.sample.derivation.security;

import lombok.Getter;

/**
 * Algorithms authorized
 */
public enum Algorithm {
    DES("DESede/CBC/NoPadding", "DESede", 8),
    TDES("DESede/CBC/NoPadding", "DESede", 8),
    TKTDES("DESede/CBC/NoPadding", "DESede", 8),
    AES("AES/CBC/NoPadding", "AES", 16),
    RSA("RSA", "RSA", 0);

    @Getter
    private String cipherAlgorithm;

    @Getter
    private String keyAlgorithm;

    @Getter
    private int blocSize;

    Algorithm(String cipherAlgorithm, String keyAlgorithm, int blocSize) {
        this.cipherAlgorithm = cipherAlgorithm;
        this.keyAlgorithm = keyAlgorithm;
        this.blocSize = blocSize;
    }
}