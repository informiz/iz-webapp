package org.informiz.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class Utils {

    @Value("${iz.channel.name}")
    private String channelName;

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
    }
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
        return String.format("%s_%s_%s",
                entityType, CHANNEL_NAME, UUID.randomUUID().toString().substring(0, 16));
    }

    public static class Views {
        public static class EntityDefaultView {
        }

        public static class EntityData extends EntityDefaultView {
        }
    }

}
