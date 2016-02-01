SYMMETRIC KEY DERIVATION
==========

# Presentation
This prototype is used to demonstrate the symmetric key derivation process.

Two standard are implemented, AN0148 and AN10922.

# Run
Server : mvn exec:java

Options:
standard = AN10922_AES128 | AN0148_DES | AN0148_3DES | AN0148_AES
uid = Card ID (7 bytes)
aid = Application ID (3 bytes)
key = Key value (8 to 16 bytes)
keyIndex = Integer
systemIdentifier = Seed value (0 to 20 bytes)

Examples :

    mvn exec:java -Dstandard=AN10922_AES128 -Duid=04112233445566 -Daid=F5865D -Dkey=00112233445566778899AABBCCDDEEFF -DsystemIdentifier=010203040506070809

    mvn exec:java -Dstandard=AN0148_DES -Duid=04112233445566 -Dkey=0011223344556677 -DkeyIndex=1
    
    mvn exec:java -Dstandard=AN0148_3DES -Duid=04112233445566 -Dkey=00112233445566778899AABBCCDDEEFF -DkeyIndex=1
    
    mvn exec:java -Dstandard=AN0148_AES -Duid=04112233445566 -Dkey=00112233445566778899AABBCCDDEEFF -DkeyIndex=1
    
