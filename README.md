# KeyStoreImport
Prior to JDK 1.6 there was no tool available that would allow you to insert a private key into a Java Keystore file (JKS).  The code contained in this repository was written to provide that functionality.
## Building

## Usage
If you want to include the entire certificate chain, concatenate the certificates in a single PEM-formatted file ordered from root to leaf certificate.  This is not required.  You can simply insert the certificate file.
```
# cat root.pem intermediate.pem certificate.pem > chain.pem
```
The KeyStoreImport tool expects the private key top be in DER-formatted PKCS#8.   To convert a PEM-formatted private key to PKCS#8 format use the following command:
```
# openssl pkcs8 -topk8 -nocrypt -in private.key -inform PEM -out private.p8 -outform DER
```
To insert the certificate chain, and private key into a new keystore execute the following
```
# /var/local/KeyStoreImport/bin/KeyStoreImport.bash \
    -keystore=keystore.jks \
    -certs=chain.pem  \
    -key= private.p8 \
    -alias=certificate.name
```
