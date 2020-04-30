package org.informiz;

import org.hyperledger.fabric.gateway.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.TimeoutException;

public class ToRemove {

    /**
     * The identity-label to use for testing
     */
    public static final String IDENTITY_LABEL = "Admin@org.nasa.com";
    /**
     * Relative path to the network-configuration directory
     */
    public static final Path CONF_PATH = Paths.get("..", "iz-chaincode", "tmp", "organizations", "peerOrganizations",
            "org.nasa.com");
    /**
     * Relative path to crypto material
     */
    public static final Path NETWORK_CONF_PATH = CONF_PATH.resolve(Paths.get("connection-org1.json"));
    /**
     * Relative path to crypto material
     */
    public static final Path CREDENTIAL_PATH = CONF_PATH.resolve(Paths.get("users", IDENTITY_LABEL, "msp"));
    /**
     * Relative path to the user's private-key
     */
    public static final Path PK_PATH = CREDENTIAL_PATH.resolve(Paths.get("keystore", "priv_sk"));
    /**
     * Where to store the wallet relative to the system user directory
     */
    public static final Path WALLET_PATH = Paths.get("..", "identity", "user", "admin", "wallet");
    /**
     * The algorithm used to generate the private-key
     */
    public static final String ALGORITHM = "EC";
    /**
     * The certificate's public key infrastructure
     */
    public static final String CERT_PKI = "X.509";
    /**
     * The Membership Service Provider label for organization 1
     */
    public static final String ORG_1_MSP = "Org1MSP";

    /**
     * A method for initializing a "wallet" for a user, based on crypto-material found in the local file-system.
     * This code assumes the credentials were generated during the setup of the informiz network (automated in the
     * iz-chaincode repository).
     * Note that in this case the certificate is a X.509 pem file, and the private key's algorithm is ECDSA.
     *
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IOException
     */
    public static Wallet setupWallet() throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {

        String current = new java.io.File( "." ).getCanonicalPath();
        System.out.println("Current dir:"+current);

        // A wallet stores identities for testing. Must be in a secure location for real users
        Wallet wallet = Wallets.newFileSystemWallet(WALLET_PATH);

        // Location of credentials to be stored in the wallet.
        Path certificatePem = CREDENTIAL_PATH.resolve(Paths.get("signcerts", IDENTITY_LABEL +"-cert.pem"));

        X509Certificate cert;
        try (InputStream inStream = new FileInputStream(certificatePem.toFile())) {
            CertificateFactory cf = CertificateFactory.getInstance(CERT_PKI);
            cert = (X509Certificate)cf.generateCertificate(inStream);
        }

        String privateKeyContent = new String(Files.readAllBytes(PK_PATH))
                .replaceAll("\\n", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");

        KeyFactory kf = KeyFactory.getInstance(ALGORITHM);

        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);

        // Load credentials into wallet
        String identityLabel = IDENTITY_LABEL;
        Identity identity = Identities.newX509Identity(ORG_1_MSP, cert, privKey);

        wallet.put(identityLabel, identity);
        return wallet;
    }

    public static Gateway.Builder createGatewayBuilder() throws IOException {
        String CHANNEL_NAME = "mychannel";
        String CHAINCODE_ID = "informiz";
        String CONTRACT_NAME = "SourceContract";
        // Load an existing wallet holding identities used to access the network.
        Wallet wallet = Wallets.newFileSystemWallet(WALLET_PATH);

        // Configure the gateway connection used to access the network.
        Gateway.Builder builder = Gateway.createBuilder()
                .identity(wallet, IDENTITY_LABEL)
                .networkConfig(NETWORK_CONF_PATH);

        // Test the connection
        try (Gateway gateway = builder.connect()) {

            // Obtain a smart contract deployed on the network.
            Network network = gateway.getNetwork(CHANNEL_NAME);
            Contract contract = network.getContract(CHAINCODE_ID, CONTRACT_NAME);

            byte[] submitResult = contract.submitTransaction("createSource", "Test Source", "0.91", "0.85");
            System.out.println(new String(submitResult, StandardCharsets.UTF_8));

            // Evaluate transactions that query state from the ledger.
//            byte[] queryResult = contract.evaluateTransaction(QUERY_FUNC_NAME);
//            System.out.println(new String(queryResult, StandardCharsets.UTF_8));

        } catch (ContractException | TimeoutException | InterruptedException e) {
            throw new RuntimeException("Connection/invocation failed", e);
        }
        return builder;
    }

    public static void main(String[] args) throws InvalidKeySpecException, CertificateException, NoSuchAlgorithmException, IOException {
        //Wallet wallet = setupWallet();
        Gateway.Builder builder = createGatewayBuilder();
    }

}

