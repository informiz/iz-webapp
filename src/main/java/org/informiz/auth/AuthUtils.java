package org.informiz.auth;

import com.google.api.gax.paging.Page;
import com.google.cloud.WriteChannel;
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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static org.informiz.auth.InformizGrantedAuthority.*;

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

    public static List<GrantedAuthority> anonymousAuthorities() {
        return Arrays.asList(
                new InformizGrantedAuthority(ROLE_VIEWER, "anonymous"));
    }

    public static Collection<GrantedAuthority> getUserAuthorities(String email, String entityId) {
        // TODO: get wallet from secret-manager based on user entity-id
        Wallet userWallet;
        try {
            userWallet = getUserWallet(email);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected error while loading user wallet", e);
        }

        Collection<GrantedAuthority> authorities = new ArrayList<>();

        if (userWallet == null) {
            authorities.add(new InformizGrantedAuthority(ROLE_VIEWER, entityId));
            return authorities; // No additional authorities
        }

        // All users have fact-checker permissions
        authorities.add(new InformizGrantedAuthority(ROLE_CHECKER, entityId));

        // TODO: get current channel name
        if (isChannelMember(email, userWallet, channelId)) {
            authorities.add(new InformizGrantedAuthority(ROLE_MEMBER, entityId));
        }

        if (isChannelAdmin(email, userWallet, channelId)) {
            authorities.add(new InformizGrantedAuthority(ROLE_ADMIN, entityId));
        }

        return authorities;
    }

    // TODO: Very inefficient!! Implement a user-details service to keep this info
    public static String getUserEntityId(Collection<? extends GrantedAuthority> authorities) {
        String entityId =  authorities.stream()
                .filter(auth -> auth instanceof InformizGrantedAuthority)
                .findFirst()
                .map(auth -> ((InformizGrantedAuthority)auth).getEntityId())
                .orElse(null);
        return entityId;
    }


/*
    public static void getChannelProxy(String email, String channelId) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession userSession =  attr.getRequest().getSession(true);

        if (userSession.getAttribute(CryptoUtils.ChaincodeProxy.PROXY_ATTR) == null) {
            //CryptoUtils.ChaincodeProxy proxy = CryptoUtils.createChaincodeProxy(channelId, "informiz");
        }
        // TODO: update granted authorities if channel changes (trigger re-authentication?)
    }
*/

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

    public static String uploadMedia(InputStream inputStream, String filename) throws IOException {
        String path = String.format("%s/%s/%s", mediaFolder, channelId, filename);
        BlobId blobId = BlobId.of(mediaBucket, path);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build(); // TODO: content-type? MD5?
        Blob current = storage.get(blobId);
        // TODO: handle duplicate names!!
        if ( ! ((current != null) && current.exists()) ) {
            try (WriteChannel writer = storage.writer(blobInfo)) {
                byte[] buffer = new byte[1024];
                int limit;
                while ((limit = inputStream.read(buffer)) >= 0) {
                    writer.write(ByteBuffer.wrap(buffer, 0, limit));
                }
            }
        }
        return String.format("%s%s", mediaPrefix, blobInfo.getName());
    }

    public static void saveCertificates(String userEntityId) throws IOException {

        // TODO: get real credentials
        String userCertContent = FileUtils.readFileToString(
                new File(AuthUtils.class.getClassLoader().getResource("test-crypto/test-cert.pem").getPath()),
                StandardCharsets.UTF_8.toString());
        String privateKeyContent = FileUtils.readFileToString(
                new File(AuthUtils.class.getClassLoader().getResource("test-crypto/test_pk").getPath()),
                StandardCharsets.UTF_8.toString());

        uploadIdentityContent(storage, userEntityId, userCertContent, certFilename);
        uploadIdentityContent(storage, userEntityId, privateKeyContent, keyFilename);
    }

    private static void uploadIdentityContent(Storage storage, String userEntityId, String content, String filename) {
        try {
            byte[] bytes = encrypt(ByteString.copyFromUtf8(content), keyRingId, keyId).toByteArray();

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

        byte[] certBytes = decrypt(ByteString.copyFrom(certBlob.getContent()), keyRingId, keyId).toByteArray();
        X509Certificate cert = CryptoUtils.getCertificate(new ByteArrayInputStream(certBytes));

        ByteString keyContent = decrypt(ByteString.copyFrom(keyBlob.getContent()), keyRingId, keyId);
        PrivateKey key = CryptoUtils.getPKCS8Key(keyContent.toString(StandardCharsets.UTF_8), CryptoUtils.ALGORITHM);

        // TODO: same MSP for all organizations?
        return CryptoUtils.setUpWallet(String.format("%s:%s:%s", userEntityId, role, channelId),
                CryptoUtils.ORG_1_MSP, cert, key);
    }


    // TODO: use local encryption key to improve performance..?
    public static String encrypt(String content, String keyRingId, String keyId) throws IOException {
        byte[] encrypted = encrypt(ByteString.copyFromUtf8(content), keyRingId, keyId).toByteArray();
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static ByteString encrypt(ByteString content, String keyRingId, String keyId) throws IOException {
        try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {

            CryptoKeyName keyVersionName = CryptoKeyName.of(projectId, locationId, keyRingId, keyId);
            EncryptResponse response = client.encrypt(keyVersionName, content);
            return response.getCiphertext();
        }
    }

    public static String decrypt(String content, String keyRingId, String keyId) throws IOException {
        byte[] restored = Base64.getDecoder().decode(content);
        return decrypt(ByteString.copyFrom(restored), keyRingId, keyId).toStringUtf8();
    }

    public static ByteString decrypt(ByteString content, String keyRingId, String keyId) throws IOException {
        try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {

            CryptoKeyName keyVersionName = CryptoKeyName.of(projectId, locationId, keyRingId, keyId);
            DecryptResponse response = client.decrypt(keyVersionName, content);
            return response.getPlaintext();
        }
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
