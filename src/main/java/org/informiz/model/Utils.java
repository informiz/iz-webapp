package org.informiz.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

// TODO: ************************ REMOVE THIS ONCE ENTITY ID IS PROVIDED BY CHAINCODE ************************

@Service
public class Utils {

    public static String channelName;

    @Value("${iz.channel.name}")
    public void setDatabase(String channelName) {
        channelName = channelName;
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
                entityType, channelName, UUID.randomUUID().toString().substring(0, 16));
    }
}
