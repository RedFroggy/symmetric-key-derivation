SYMMETRIC KEY DERIVATION
==========

# Presentation
This prototype is used to demonstrate the symmetric key derivation process.

Two standards are implemented: AN0148 and AN10922.

# Run
    mvn exec:java

## Options:
 - standard = AN10922_AES128 | AN0148_DES | AN0148_3DES | AN0148_AES
 - uid = Card ID (7 bytes)
 - aid = Application ID (3 bytes)
 - key = Key value (8 to 16 bytes)
 - keyIndex = Integer
 - systemIdentifier = Seed value (0 to 20 bytes)

Examples :

    mvn exec:java -Dstandard=AN10922_AES128 -Duid=04112233445566 -Daid=F5865D -Dkey=00112233445566778899AABBCCDDEEFF -DsystemIdentifier=010203040506070809

    mvn exec:java -Dstandard=AN0148_DES -Duid=04112233445566 -Dkey=0011223344556677 -DkeyIndex=1
    
    mvn exec:java -Dstandard=AN0148_3DES -Duid=04112233445566 -Dkey=00112233445566778899AABBCCDDEEFF -DkeyIndex=1
    
    mvn exec:java -Dstandard=AN0148_AES -Duid=04112233445566 -Dkey=00112233445566778899AABBCCDDEEFF -DkeyIndex=1
    
## Output:

With AN10922 Standard:

    ===== AN10922 DIVERSIFICATION ======
    K: 00 11 22 33 44 55 66 77 88 99 AA BB CC DD EE FF
    K0: FD E4 FB AE 4A 09 E0 20 EF F7 22 96 9F 83 83 2B
    K1: FB C9 F7 5C 94 13 C0 41 DF EE 45 2D 3F 07 06 D1
    K2: F7 93 EE B9 28 27 80 83 BF DC 8A 5A 7E 0E 0D 25
    M: 01 04 11 22 33 44 55 66 5D 86 F5 01 02 03 04 05 06 07 08 09
    K': 37 13 1F 0F 6B EF 7B 93 2A 6E 8B 25 AE 04 9C 36
    ====================================
    Diversified key: 37 13 1F 0F 6B EF 7B 93 2A 6E 8B 25 AE 04 9C 36
  
  - K: Original key value
  - K0: Subkey 0
  - K1: Subkey 1
  - K2: Subkey 2
  - M: Diversification seed
  - K': Diversified key


With AN0148 Standard:

    ====== AN0148 DIVERSIFICATION ======
    K: 00 11 22 33 44 55 66 77 88 99 AA BB CC DD EE FF
    ALGO: AES
    M: 01 04 11 22 33 44 55 66 01 04 11 22 33 44 55 66
    K': B9 B6 1B 27 99 75 03 C8 C5 20 3E ED 28 1D 8C 0B
    ====================================
    Diversified key: B9 B6 1B 27 99 75 03 C8 C5 20 3E ED 28 1D 8C 0B

  - K: Original key value
  - M: Diversification seed
  - K': Diversified key


# CI
Develop: [![Build Status](https://api.travis-ci.org/RedFroggy/symmetric-key-derivation.svg?branch=develop)](https://travis-ci.org/RedFroggy/symmetric-key-derivation)
Master: [![Build Status](https://api.travis-ci.org/RedFroggy/symmetric-key-derivation.svg?branch=master)](https://travis-ci.org/RedFroggy/symmetric-key-derivation)