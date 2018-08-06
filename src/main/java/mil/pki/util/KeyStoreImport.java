package mil.pki.util;

/* Java Imports */
import java.security.*;
import java.security.spec.*;
import java.security.cert.*;
import java.io.*;
import java.util.*;

/* Custom Imports */
import mil.pki.tools.Options;
import mil.pki.tools.Options.Multiplicity;
import mil.pki.tools.Options.Separator;

/**
 * This application will import a private key and it's associated X509 
 * certificate chain into the target keystore file in Java JKS format.
 * This utility was written because there exists no capabilities to import
 * a previously generated public/private key pair into a JKS file.
 * The following are the required options:
 * 
 * -keystore=<path-to-key-store> The full path to the target JKS file
 * -certs=<path-to-cert-file>    The full path to the file containing the 
 *                               certificate chain (PEM encoded)
 * -key=<path-to-key-File>       The full path to the file containing the 
 *                               RSA private key (DER encoded PKCS#8 format)
 * -alias=<cert-alias>           The certificate alias
 * 
 * @author L. Craig Carpenter
 */
public class KeyStoreImport {
	
	/**
	 * Usage String printed when incorrect arguments are supplied.
	 */
	private static final String USAGE_STRING = 
		new String("Usage: java KeyStoreImport " + 
				"-keystore=<path-to-key-store> " +
				"-certs=<path-to-cert-file> " + 
				"-key=<path-to-key-File> " +
				"-alias=<cert-alias> " +
				"[-h] [-help]" );
	
	/**
	 * Help string printed when -h or -help appear on the command line.
	 */
	private static final String HELP_STRING = new String(
			"This application will import a private key and it's associated " + 
			"X509 certificate chain into the target keystore file in Java " + 
			"JKS format.  The following are the required options: \n\n" +
			"-keystore=<path-to-key-store> The full path to the target JKS file\n" +
			"-certs=<path-to-cert-file>    The full path to the file " +
			"containing the certificate chain (PEM encoded)\n" + 
			"-key=<path-to-key-File>       The full path to the file " +
			"containing the RSA private key (DER encoded PKCS#8 format)\n" +
			"-alias=<cert-alias>           The certificate alias\n\n");
	
	/**
	 * Used to output the command line arguments for debugging purposes
	 */
	private static boolean DEBUG = true;
	
	/**
	 * Method constructed originally for debugging purposes used to convert
	 * a Collection of Certificates to an array of certificates for importing
	 * into the target JKS file.
	 * 
	 * @param c A collection of Certificate objects retrieved from a 
	 * certificate chain file 
	 * @return An array containing the certificate chain associated with the
	 * certificate to import
	 */
	private static java.security.cert.Certificate[] toArray(Collection c) {
		java.security.cert.Certificate certs[] = 
				new java.security.cert.Certificate[c.size()];
		int index = 0;
		Iterator it = c.iterator();
		while (it.hasNext()) {
			certs[index++] = (java.security.cert.Certificate)it.next();
		}
		return certs;
	}
	
	/**
	 * Main method containing all of the program logic
	 * 
	 * @param args Command line arguments to process
	 */
	public static void main(String args[]) {
	
		String keyStoreFileName = null;
		String certificateChainFileName = null;
		String privateKeyFileName = null; 
		String entryAlias = null;
		String newLine = System.getProperty("line.separator", "\n");
		
		// Set up the command line options
		Options opt = new Options(args, 0);
		opt.getSet().addOption("keystore", Separator.EQUALS, 
				Multiplicity.ONCE);
		opt.getSet().addOption("certs", Separator.EQUALS, 
				Multiplicity.ONCE);
		opt.getSet().addOption("key", Separator.EQUALS, 
				Multiplicity.ONCE);
		opt.getSet().addOption("alias", Separator.EQUALS, 
				Multiplicity.ONCE);
		opt.getSet().addOption("h", Separator.EQUALS, 
				Multiplicity.ZERO_OR_MORE);
		opt.getSet().addOption("help", Separator.EQUALS, 
				Multiplicity.ZERO_OR_MORE);
		
		// Make sure the options make sense
		if (!opt.check(true, false)) {
			System.out.println(KeyStoreImport.USAGE_STRING);
			System.exit(1);
		}
		
		// See if the user asked for the help information to print
		if (opt.getSet().isSet("h") || opt.getSet().isSet("help")) {
			System.out.println(KeyStoreImport.HELP_STRING);
			System.out.println("");
			System.out.println(KeyStoreImport.USAGE_STRING);
			System.exit(0);
		}
	
		// Retrieve the command line parameters
		if (opt.getSet().isSet("keystore") && opt.getSet().isSet("certs") && 
				opt.getSet().isSet("key") && opt.getSet().isSet("alias")) {
			keyStoreFileName = 
				opt.getSet().getOption("keystore").getResultValue(0);
			certificateChainFileName = 
				opt.getSet().getOption("certs").getResultValue(0);
			privateKeyFileName = 
				opt.getSet().getOption("key").getResultValue(0);
			entryAlias = opt.getSet().getOption("alias").getResultValue(0);
		}
		else {
			System.out.println(KeyStoreImport.HELP_STRING);
			System.out.println("");
			System.out.println(KeyStoreImport.USAGE_STRING);
			System.exit(1);
		}
		
		try {
			
			// Output the command line parameters for debugging purposes
			if (KeyStoreImport.DEBUG) {
				System.out.println("===== DEBUG INFO =====");
				System.out.println("Key Store    : " + 
						keyStoreFileName);
				System.out.println("Certificates : " + 
						certificateChainFileName);
				System.out.println("Private Key  : " + 
						privateKeyFileName);
				System.out.println("Alias        : " + 
						entryAlias);
				System.out.println("===== DEBUG INFO =====");
			}
			
			// Prompt the user for the password for the keystore.
			System.out.print("Keystore password>  ");
			String keyStorePassword = (new BufferedReader(
					new InputStreamReader(System.in))).readLine();
	
			// Load the keystore
			System.out.println(newLine + "Loading the keystore file: " + 
					keyStoreFileName);
			KeyStore keyStore = KeyStore.getInstance("jks");
			FileInputStream keyStoreInputStream =
				new FileInputStream(keyStoreFileName);
			keyStore.load(keyStoreInputStream, 
					keyStorePassword.toCharArray());
			keyStoreInputStream.close();
	
			//Load the certificate chain (in X.509 DER encoding).
			System.out.println(newLine + "Loading the certificate chain: " + 
					certificateChainFileName);
			FileInputStream certificateStream =
				new FileInputStream(certificateChainFileName);
			CertificateFactory certificateFactory =
				CertificateFactory.getInstance("X.509");
			Collection c = certificateFactory.generateCertificates(
					certificateStream);
			java.security.cert.Certificate[] chain = 
				KeyStoreImport.toArray(c);
			certificateStream.close();
			
			if (KeyStoreImport.DEBUG) {
				System.out.println("===== DEBUG: Collection Size: " + 
						c.size());
				System.out.println("===== DEBUG: Array Size     : " + 
						chain.length);
			}
			
			// Load the private key (in PKCS#8 DER encoding).
			System.out.println(newLine + "Loading the private key: " + 
					privateKeyFileName);
			File keyFile = new File(privateKeyFileName);
			byte[] encodedKey = new byte[(int)keyFile.length()];
			FileInputStream keyInputStream = new FileInputStream(keyFile);
			keyInputStream.read(encodedKey);
			keyInputStream.close();
	
			// Create the concrete key object
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
			KeyFactory rSAKeyFactory = KeyFactory.getInstance("RSA");
			PrivateKey privateKey = rSAKeyFactory.generatePrivate(
					keySpec);
	
			// Add the private key to the keystore with a password set to 
			// the same value as the key store password.
			keyStore.setEntry(entryAlias,
					new KeyStore.PrivateKeyEntry(privateKey, chain),
					new KeyStore.PasswordProtection(
							keyStorePassword.toCharArray()));
	
			// Output the updated keystore
			System.out.println("Writing new keystore...");
			FileOutputStream keyStoreOutputStream =
				new FileOutputStream(keyStoreFileName);
			keyStore.store(keyStoreOutputStream, 
					keyStorePassword.toCharArray());
			keyStoreOutputStream.close();
			
			System.out.println("Keystore update complete!");
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
