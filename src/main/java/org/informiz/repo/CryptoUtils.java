package org.informiz.repo;

import org.hyperledger.fabric.gateway.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.xml.ws.WebServiceException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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

public class CryptoUtils {

    // TODO: ********************************** FOR TESTING, REPLACE WITH CONFIG!! **********************************

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

    // TODO: ***************************************** END TESTING ******************************************

    /**
     * Utility method for initializing a "wallet" for a user, based on the provided crypto-material.
     * Note that the certificate MUST be a X.509 pem file.
     *
     * @param idLabel label associated with the identity of the user
     *                TODO: different ids for different organizations, e.g me@org1.edu.nl, me@org2.ac.uk
     * @param mspLabel the Membership Service Provider label
     *                 TODO: use one MSP per organization
     * @param userCert user's certificate
     *                 TODO: produce user certificates with organizational-units for possible fine-grained access
     * @param userKey user's private key
     * @return
     * @throws IOException
     */
    public static Wallet setUpWallet(@NotBlank String idLabel, @NotBlank String mspLabel,
                                     @NotNull X509Certificate userCert, @NotNull PrivateKey userKey)
            throws IOException {

        Wallet wallet = Wallets.newInMemoryWallet();

        // Load credentials into wallet
        Identity identity = Identities.newX509Identity(mspLabel, userCert, userKey);

        wallet.put(idLabel, identity);
        return wallet;
    }

    public static boolean isInformizMember(@NotBlank String userEmail) {
        // TODO: Check if the user has a wallet
        return true;
    }


    public static boolean isChannelMember(@NotNull Wallet userWallet, @NotBlank String channelId) {
        // TODO: Check if the user has an identity associated with the specific channel
        return true;
    }


    /**
     * Retrieve a user's wallet from encrypted storage
     * @param userEmail the email used for the user-login
     * @return a wallet associated with the user, or null if the user doesn't have a wallet
     */
    public static Wallet getUserWallet(@NotBlank String userEmail) {
        // TODO: load wallet from encrypted storage, path based on email used for login.
        return setupWallet();
    }

    /**
     * Save a user's wallet to encrypted storage
     * @param userEmail the email used for the user-login
     * @param wallet the user's wallet
     * @return a wallet associated with the user, or null if the user doesn't have a wallet
     */
    public static void saveUserWallet(@NotBlank String userEmail, @NotNull Wallet wallet) {
        // TODO: save wallet to encrypted storage, path based on email used for login.
    }

    private static X509Certificate getCertificate(Path userCertPath) throws IOException, CertificateException {
        X509Certificate cert;
        try (InputStream inStream = new FileInputStream(userCertPath.toFile())) {
            CertificateFactory cf = CertificateFactory.getInstance(CERT_PKI);
            cert = (X509Certificate)cf.generateCertificate(inStream);
        }
        return cert;
    }

    /**
     *
     * @param pkPath Path to a private-key file
     * @param algo The algorithm used to generate the private-key, e.g 'EC' for ECDSA
     * @return A {@link PrivateKey} object
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private static PrivateKey getPKCS8Key(Path pkPath, String algo) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKeyContent = new String(Files.readAllBytes(pkPath))
                .replaceAll("\\n", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");

        KeyFactory kf = KeyFactory.getInstance(algo);

        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        return kf.generatePrivate(keySpecPKCS8);
    }


    // TODO: ********************************** FOR TESTING, REMOVE THIS!! **********************************
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
    public static Wallet setupWallet() {
        try {
            // A wallet with identities for testing. Must be in a secure location for real users
            Wallet wallet = Wallets.newInMemoryWallet();

            // Location of credentials to be stored in the wallet.
            //Path certificatePem = CREDENTIAL_PATH.resolve(Paths.get("signcerts", IDENTITY_LABEL + "-cert.pem"));
            Path certificatePem = Paths.get(
                    CryptoUtils.class.getClassLoader().getResource("test-crypto/test-cert.pem").toURI());

            X509Certificate cert = getCertificate(certificatePem);
            //PrivateKey privKey = getPKCS8Key(PK_PATH, ALGORITHM);
            PrivateKey privKey = getPKCS8Key(Paths.get(
                    CryptoUtils.class.getClassLoader().getResource("test-crypto/test_pk").toURI()),
                    ALGORITHM);

            // Load credentials into wallet
            Identity identity = Identities.newX509Identity(ORG_1_MSP, cert, privKey);
            wallet.put(IDENTITY_LABEL, identity);
            return wallet;
        } catch (IOException | CertificateException | NoSuchAlgorithmException |
                InvalidKeySpecException | URISyntaxException e) {
            throw new WebServiceException("Failed to create test-wallet", e);
        }
    }

    /**
     * @param channelName
     * @param ccId
     * @return
     * @throws IOException
     */
    public static ChaincodeProxy createChaincodeProxy(String channelName, String ccId) throws IOException {
        // Load an existing wallet holding identities used to access the network.
        //Wallet wallet = Wallets.newFileSystemWallet(WALLET_PATH);
        Wallet wallet = setupWallet();
        return createChaincodeProxy(wallet, IDENTITY_LABEL, channelName, ccId, "config/connection.json");
    }

    // TODO: ***************************************** END TESTING ******************************************

    /**
     * Create a proxy to the chaincode for the given identity. The wallet should contain crypto-material authorizing
     * the given identity to interact with the given chaincode
     * @param wallet A wallet containing identities
     * @param idLabel The label associated with the identity accessing the chaincode
     * @param channelName The name of the channel to connect to
     * @param ccId The chaincode's id
     * @param networkConfig A name of a file containing the network configuration TODO: where are network configs stored?
     * @return A proxy that can be used to communicate with the chaincode on the user's behalf
     * @throws IOException
     */
    public static ChaincodeProxy createChaincodeProxy(Wallet wallet, String idLabel, String channelName, String ccId, String networkConfig)
            throws IOException {
        // Configure the gateway connection used to access the network.
        Gateway.Builder builder = Gateway.createBuilder()
                .identity(wallet, idLabel)
                .networkConfig(getNetworkConfig(networkConfig));

        return new ChaincodeProxy(builder, channelName, ccId);
    }

    private static InputStream getNetworkConfig(String config) throws IOException {
        return CryptoUtils.class.getClassLoader().getResource(config).openStream();
    }


    /**
     * A proxy class for connecting to a Hyperledger Fabric network
     */
    public static class ChaincodeProxy {

        public static final String PROXY_ATTR = "proxy";

        private Gateway.Builder builder;
        private String channelName;
        private String chaincodeId;

        /**
         *
         * @param builder a HLF gateway builder
         * @param channelName the channel to use, e.g "mychannel"
         * @param ccId the chain-code id, e.g "informiz"
         * @throws IOException
         */
        protected ChaincodeProxy(Gateway.Builder builder, String channelName, String ccId) {
            this.builder = builder;
            this.channelName = channelName;
            this.chaincodeId = ccId;
        }

        /**
         * Submit a transaction to the configured Hyperledger Fabric network
         * @param contractName the name of the contract, e.g "SourceContract"
         * @param func e.g "createSource"
         * @param args e.g "Test Source", "0.91", "0.85"
         * @return The transaction result returned by the chaincode, as string
         */
        public String submitTransaction(String contractName, String func, String[] args) {
            try (Gateway gateway = builder.connect()) {

                // Obtain a smart contract deployed on the network.
                Network network = gateway.getNetwork(channelName);
                Contract contract = network.getContract(chaincodeId, contractName);

                byte[] submitResult = contract.submitTransaction(func, args);
                return new String(submitResult, StandardCharsets.UTF_8);

            } catch (ContractException | TimeoutException | InterruptedException e) {
                throw new WebServiceException("Failed to submit transaction", e);
            }
        }
        /**
         * Submit a query to the configured Hyperledger Fabric network
         * @param contractName the name of the contract, e.g "SourceContract"
         * @param func e.g "querySource"
         * @param args e.g "some_source_id"
         * @return The query result returned by the chaincode, as string
         */
        public String evaluateTransaction(String contractName, String func, String[] args) {
            try (Gateway gateway = builder.connect()) {

                // Obtain a smart contract deployed on the network.
                Network network = gateway.getNetwork(channelName);
                Contract contract = network.getContract(chaincodeId, contractName);

                byte[] queryResult = contract.evaluateTransaction(func, args);
                return new String(queryResult, StandardCharsets.UTF_8);

            } catch (ContractException e) {
                throw new WebServiceException("Failed to submit query", e);
            }
        }

    }

}


