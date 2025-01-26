package org.informiz.auth;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import org.informiz.model.ChainCodeEntity;
import org.informiz.repo.checker.FactCheckerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.informiz.auth.InformizGrantedAuthority.*;
import static org.informiz.ctrl.checker.CheckerRestController.CHECKER_API_PREFIX;
import static org.informiz.model.Utils.channelFromEntityId;

@Service
public class AuthUtils {
    @Value("${iz.channel.name}")
    private String channelName;

    private final FactCheckerRepository factCheckerRepo;

    private final RestClient.Builder restClientBuilder = RestClient.builder();

    public AuthUtils(FactCheckerRepository factCheckerRepo) {
        this.factCheckerRepo = factCheckerRepo;
    }

    public boolean verifyMemberInChannel(String channel, String entityId)  {
        try {
            RestClient restClient = restClientBuilder.baseUrl(String.format("https://%s.informiz.org", channel)).build();

            return restClient.get().uri("{prefix}/{eid}", CHECKER_API_PREFIX.substring(1), entityId)
                    .retrieve().body(Boolean.class);
        } catch (RuntimeException e) {
            // TODO: log reason for failing
            return false;
        }
    }

    public static List<GrantedAuthority> anonymousAuthorities() {
        return List.of(
                new InformizGrantedAuthority(ROLE_VIEWER, "anonymous"));
    }

    /**
     * If local user - get member/admin creds
     * If checker - verify channel membership and give Checker access
     * Otherwise - anonymous user
     * @param email email address used for log-in
     * @return granted authorities
     */
    public Collection<? extends GrantedAuthority> getUserAuthorities(String email) {
        // TODO: if not found locally - get entity-id(s) from PubSub topic. Return all ids if multiple channels?
        // TODO: create dummy PubSub consumer to return some email->eid mappings?
        String entityId = factCheckerRepo.findByEmail(email).map(ChainCodeEntity::getEntityId).orElse(null);
        // Not a member in any channel - anonymous user
        if (entityId == null) return anonymousAuthorities();

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        String userChannel = channelFromEntityId(entityId);

        if (entityId != null) { // TODO: update test-channel ids and replace with (channelName.equals(userChannel))
            authorities.add(new InformizGrantedAuthority(ROLE_MEMBER, entityId));
            // TODO: check if also admin
        }
        else if(verifyMemberInChannel(userChannel, entityId)) {
            authorities.add(new InformizGrantedAuthority(ROLE_CHECKER, entityId));
        } else {
            // Not local member, could not verify membership in other channel
            return anonymousAuthorities();
        }

        return authorities;
    }

    public static String getUserEntityId(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .filter(auth -> auth instanceof InformizGrantedAuthority)
                .findFirst()
                .map(auth -> ((InformizGrantedAuthority)auth).getEntityId())
                .orElse(null);
    }

    // TODO: uploading media to channels - move to informi-controller, use config
    private static final String projectId = "key-master-283113";

    private static String CHANNEL_MEDIA_FOLDER;

    @Value("${iz.channel.media.folder}")
    public void setChannelMediaFolder(String folder){
        // workaround for assigning property-value to static field
        AuthUtils.CHANNEL_MEDIA_FOLDER = folder;
    }

    private static final String mediaBucket = "iz-public";
    private static final String mediaFolder = "media";
    private static final String mediaPrefix = "https://storage.googleapis.com/iz-public/";

    private static final Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();

    public static String uploadMedia(InputStream inputStream, String filename) throws IOException {
        String path = String.format("%s/%s/%s", mediaFolder, CHANNEL_MEDIA_FOLDER, filename);
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
}
