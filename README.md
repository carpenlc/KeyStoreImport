# KeyStoreImport
Prior to JDK 1.6 there was no tool available that would allow you to insert a private key into a Java Keystore file (JKS).  The code contained in this repository was written to provide that functionality.
## Download and Build the Source
* Minimum requirements:
    * Java Development Kit (v1.5.0 or higher)
    * GIT (v1.7 or higher)
    * Maven (v3.3 or higher)
* Download source
```
# cd /var/local
# git clone https://github.com/carpenlc/KeyStoreImport.git
```
* Build the source
```
# cd KeyStoreImport
# mvn clean package install
```
## Usage
* If you want to include the entire certificate chain, concatenate the certificates in a single PEM-formatted file ordered from root to leaf certificate.  This is not required.  You can simply insert the certificate file.
```
# cat root.pem intermediate.pem certificate.pem > chain.pem
```
* The KeyStoreImport tool expects the private key top be in DER-formatted PKCS#8.   To convert a PEM-formatted private key to PKCS#8 format use the following command:
```
# openssl pkcs8 -topk8 -nocrypt -in private.key -inform PEM -out private.p8 -outform DER
```
* To insert the certificate chain, and private key into a new keystore execute the following
```
# keytool -genkey -alias foo -keystore keystore.jks
# keytool -delete -alias foo -keystore keystore.jks
# /var/local/KeyStoreImport/bin/KeyStoreImport.bash \
    -keystore=keystore.jks \
    -certs=chain.pem  \
    -key=private.p8 \
    -alias=certificate.name
```

### Help
```
./bin/KeyStoreImport.bash -h

This application will import a private key and it's associated X509 certificate chain into the target keystore file in Java JKS format.  The following are the required options:

-keystore=<path-to-key-store> The full path to the target JKS file
-certs=<path-to-cert-file>    The full path to the file containing the certificate chain (PEM encoded)
-key=<path-to-key-File>       The full path to the file containing the RSA private key (DER encoded PKCS#8 format)
-alias=<cert-alias>           The certificate alias



Usage: java KeyStoreImport -keystore=<path-to-key-store> -certs=<path-to-cert-file> -key=<path-to-key-File> -alias=<cert-alias> [-h] [-help]
```
### Notes
* Some applications do not correctly handle the full certificate chain.  For example JBoss EAP (6.4 and below) will give clients a sec_error_pkcs11_device_error.  
