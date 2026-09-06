package org.informiz.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class Utils {

    public static final String ID_PATTERN = "%s_%s_%s";
    private static String CHANNEL_NAME;

    @Value("${iz.channel.name}")
    public void setChannelName(String name){
        // workaround for assigning property-value to static field
        Utils.CHANNEL_NAME = name;
    }

    public enum EntityType {
        FACT_CHECKER("Fact Checker"),
        SOURCE("Source"),
        CLAIM("Claim"),
        CITATION("Citation"),
        INFORMI("Informi");

        private final String displayValue;

        EntityType(String displayValue) {
            this.displayValue = displayValue;
        }

        public String getDisplayValue() {
            return displayValue;
        }

        public static String entityTypeOptionsPattern() {
            return EnumSet.allOf(EntityType.class).stream().map(Enum::toString).collect(Collectors.joining("|"));
        }
    }

    public static Pattern EID_PREFIX_PATTERN = Pattern.compile(String.format("^(%s)_([^_]+)_(.*)",
            EntityType.entityTypeOptionsPattern()));
    public static String createEntityId(ChainCodeEntity entity) {
        EntityType entityType;

        if (entity instanceof FactCheckerBase) {
            entityType = EntityType.FACT_CHECKER;
        } else if (entity instanceof SourceBase) {
            entityType = EntityType.SOURCE;
        } else if (entity instanceof HypothesisBase) {
            entityType = EntityType.CLAIM;
        } else if (entity instanceof CitationBase) {
            entityType = EntityType.CITATION;
        } else if (entity instanceof InformiBase) {
            entityType = EntityType.INFORMI;
        } else {
            throw new IllegalStateException("Unexpected entity type: " + entity.toString());
        }

        // TODO: check uniqueness
        return String.format(ID_PATTERN,
                entityType, CHANNEL_NAME, UUID.randomUUID().toString().substring(0, 16));
    }

    public static String channelFromEntityId(String entityId) {
        try {
            Matcher m = EID_PREFIX_PATTERN.matcher(entityId);
            if (m.find())
                return m.group(2);
            return null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static class Views {
        public static class EntityDefaultView {
        }

        public static class EntityData extends EntityDefaultView {
        }
    }

}
