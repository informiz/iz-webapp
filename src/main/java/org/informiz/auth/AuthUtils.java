package org.informiz.auth;

import com.google.api.gax.paging.Page;
import com.google.cloud.kms.v1.CryptoKeyName;
import com.google.cloud.kms.v1.DecryptResponse;
import com.google.cloud.kms.v1.EncryptResponse;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.storage.*;
import com.google.protobuf.ByteString;
import org.apache.commons.io.FileUtils;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Wallet;
import org.informiz.repo.CryptoUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;

public class AuthUtils {

    public static void generateCryptoMaterial(String userEntityId) {
        // TODO: generate using organization's CA
/*
        try {
            saveCertificates(userEntityId);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate and upload crypto material", e);
        }
*/
    }

    public static boolean isChannelMember(@NotBlank String userEntityId, @NotNull Wallet userWallet, @NotBlank String channelId) {
        Identity identity = null;
        try {
            identity = userWallet.get(String.format("%s:%s:%s", userEntityId, "member", channelId));
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected error while retrieving identity", e);
        }
        return identity != null;
    }

    public static boolean isChannelAdmin(@NotBlank String userEntityId, @NotNull Wallet userWallet, @NotBlank String channelId) {
        Identity identity = null;
        try {
            identity = userWallet.get(String.format("%s:%s:%s", userEntityId, "admin", channelId));
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected error while retrieving identity", e);
        }
        return identity != null;
    }

    public static Collection<GrantedAuthority> getUserAuthorities(String email, String entityId) {
        // TODO: get wallet from encrypted storage based on user email address
        // TODO: need to exchange user's email for entity-id
        Wallet userWallet = null;
        try {
            userWallet = getUserWallet(email);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected error while loading user wallet", e);
        }

        Collection<GrantedAuthority> authorities = new ArrayList<>();

        if (userWallet == null) {
            return authorities; // No additional authorities
        }

        // All users have fact-checker permissions
        authorities.add(new InformizGrantedAuthority("ROLE_CHECKER", entityId));

        // TODO: get current channel name
        if (isChannelMember(email, userWallet, channelId)) {
            authorities.add(new InformizGrantedAuthority("ROLE_MEMBER", entityId));
        }

        if (isChannelAdmin(email, userWallet, channelId)) {
            authorities.add(new InformizGrantedAuthority("ROLE_ADMIN", entityId));
        }

        return authorities;
    }


    public static void getChannelProxy(String email, String channelId) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession userSession =  attr.getRequest().getSession(true);

        // TODO: if no proxy or channel changed - create proxy
        if (userSession.getAttribute(CryptoUtils.ChaincodeProxy.PROXY_ATTR) == null) {
            //CryptoUtils.ChaincodeProxy proxy = CryptoUtils.createChaincodeProxy(channelId, "informiz");
        }
        // TODO: update granted authorities if channel changes (trigger re-authentication?)
    }

    // TODO: use config
    private static final String projectId = "key-master-283113";
    private static final String locationId = "global";
    private static final String keyRingId = "informiz";
    private static final String keyId = "beta-channel";
    private static final String izBucket = "informiz";
    private static final String idsFolder = "identities";
    private static final String channelId = "beta-channel.informiz.org";
    private static final String checkersChannelId = "checkers.informiz.org";

    private static final String certFilename = "cert.pem";
    private static final String keyFilename = "key.pk";

    private static final String mediaBucket = "iz-public";
    private static final String mediaFolder = "media";
    private static final String mediaPrefix = "https://storage.googleapis.com/iz-public/";

    private static final Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();

    public static String uploadMedia(byte[] media, String filename) {
        String path = String.format("%s/%s/%s", mediaFolder, channelId, filename);
        BlobId certBlobId = BlobId.of(mediaBucket, path);

        BlobInfo blobInfo = BlobInfo.newBuilder(certBlobId).build();
        storage.create(blobInfo, media);

        return String.format("%s%s", mediaPrefix, blobInfo.getName());
    }

    public static void saveCertificates(String userEntityId) throws IOException {

        // TODO: get real credentials
        String userCertContent = FileUtils.readFileToString(
                new File(AuthUtils.class.getClassLoader().getResource("test-crypto/test-cert.pem").getPath()),
                StandardCharsets.UTF_8);
        String privateKeyContent = FileUtils.readFileToString(
                new File(AuthUtils.class.getClassLoader().getResource("test-crypto/test_pk").getPath()),
                StandardCharsets.UTF_8);

        uploadIdentityContent(storage, userEntityId, userCertContent, certFilename);
        uploadIdentityContent(storage, userEntityId, privateKeyContent, keyFilename);
    }

    private static void uploadIdentityContent(Storage storage, String userEntityId, String content, String filename) {
        try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {

            CryptoKeyName keyVersionName = CryptoKeyName.of(projectId, locationId, keyRingId, keyId);
            EncryptResponse response = client.encrypt(keyVersionName, ByteString.copyFromUtf8(content));
            byte[] bytes = response.getCiphertext().toByteArray();

            BlobId certBlobId = BlobId.of(izBucket,
                    String.format("%s/%s/member:%s/%s", idsFolder, userEntityId, channelId, filename));
            BlobInfo blobInfo = BlobInfo.newBuilder(certBlobId).build();
            storage.create(blobInfo, bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload crypto material", e);
        }
    }

    public static  Wallet getUserWallet(String userEntityId) throws IOException, CertificateException, InvalidKeySpecException, NoSuchAlgorithmException {
        Page<Blob> blobs = storage.list(izBucket,
                Storage.BlobListOption.prefix(String.format("%s/%s/", idsFolder, userEntityId)),
                Storage.BlobListOption.pageSize(1));
        if ( ! blobs.getValues().iterator().hasNext()) return null;

        // TODO: check for admin identity
        blobs = storage.list(izBucket,
                Storage.BlobListOption.prefix(String.format("%s/%s/member:%s/", idsFolder, userEntityId, channelId)),
                Storage.BlobListOption.pageSize(1));

        if (blobs.getValues().iterator().hasNext()) {
            return getWalletForIdentity(userEntityId, "member", channelId);
        } else {
            // All fact-checkers should have crypto-material for checker identity
            return getWalletForIdentity(userEntityId, "member", checkersChannelId);
        }

    }

    private static Wallet getWalletForIdentity(String userEntityId, String role, String channel) throws IOException, CertificateException, InvalidKeySpecException, NoSuchAlgorithmException {
        Blob certBlob = storage.get(BlobId.of(izBucket, String.format("%s/%s/%s:%s/%s",
                idsFolder, userEntityId, role, channel, certFilename)));
        Blob keyBlob = storage.get(BlobId.of(izBucket, String.format("%s/%s/%s:%s/%s",
                idsFolder, userEntityId, role, channel, keyFilename)));

        X509Certificate cert;
        PrivateKey key;
        try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {

            CryptoKeyName keyVersionName = CryptoKeyName.of(projectId, locationId, keyRingId, keyId);

            DecryptResponse response = client.decrypt(keyVersionName, ByteString.copyFrom(certBlob.getContent()));
            byte[] certBytes = response.getPlaintext().toByteArray();
            cert = CryptoUtils.getCertificate(new ByteArrayInputStream(certBytes));

            response = client.decrypt(keyVersionName, ByteString.copyFrom(keyBlob.getContent()));
            ByteString keyContent = response.getPlaintext();
            key = CryptoUtils.getPKCS8Key(keyContent.toString(StandardCharsets.UTF_8), CryptoUtils.ALGORITHM);
        }

        // TODO: same MSP for all organizations?
        return CryptoUtils.setUpWallet(String.format("%s:%s:%s", userEntityId, role, channelId),
                CryptoUtils.ORG_1_MSP, cert, key);
    }

 // TODO: REMOVE THIS
/*
    public static void main(String[] args) {
        try {
            //saveCertificates("email@domain.com");
            //Wallet wallet = getUserWallet("email@domain.com");
            //System.out.println("Got it");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
}
