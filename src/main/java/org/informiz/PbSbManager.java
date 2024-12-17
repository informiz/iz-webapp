package org.informiz;

import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import org.informiz.model.CitationBase;
import org.informiz.repo.citation.CitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class PbSbManager {

    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(PATTERN);
    private static final String PROJECT_ID = "key-master-283113"; // Replace with your project ID
    private static final String SUBSCRIPTION_ID = "LastReceivedTs-sub"; // Replace with your subscription name
    private static final String LAST_RECEIVED_TOPIC = "LastReceivedTs";
    @Autowired
    private PubSubPublisher pubSubPublisher;
    private Long lastCreatedTs = 1L;



    @Autowired
    private CitationRepository citationRepository;

    public PbSbManager(PubSubPublisher pubSubPublisher) {
    }

//    public void printTop5Citations() {
//        List<CitationBase> topCitations = citationRepository.findTop5ByOrderByUpdatedTsDesc();
//        System.out.println("Top 5 Citations:");
//        topCitations.forEach(System.out::println);
//    }
//    public void printTop5Citations() {
//    Pageable top5 = PageRequest.of(0, 5, Sort.by("updatedTs").descending());
//    List<CitationBase> topCitations = citationRepository.findTop5ByOrderByUpdatedTsDesc(top5);
//
//    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
//
//    System.out.println("Top 5 Citations:");
//    topCitations.forEach(citation -> {
//        Instant instant = Instant.ofEpochMilli(citation.getUpdatedTs());
//        String readableTimestamp = formatter.format(instant);
//        System.out.println("Timestamp: " + citation.getUpdatedTs() + " | Readable: " + readableTimestamp);
//    });
//}

    public void printTop5Citations() {
        // Create a Pageable object to fetch the top 5 sorted by updatedTs in descending order


        // Fetch the top 5 citations
        Pageable pageable = PageRequest.of(0, 5, Sort.by("updatedTs").descending());
        List<CitationBase> topCitations = citationRepository.findTop5ByOrderByUpdatedTsDesc(pageable);

        // Print the citations
        System.out.println("Top 5 Citations:");
        topCitations.forEach(citation -> {
            Instant instant = Instant.ofEpochMilli(citation.getUpdatedTs());
            LocalDateTime localDateTime =
                    LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));  // TODO Handle time zones
            String readableTimestamp = FORMATTER.format(localDateTime);
            System.out.println("Content: " + citation.getText() + ", UpdatedTs: " + readableTimestamp);
        });
    }




//    @Scheduled(fixedRate = 300000) // Runs every 5 minutes
//    public void processActivityLog() {
//        // Pull the last received message
//        String lastCreatedTs = pullLastReceivedTs();
//        System.out.println("Last received createdTs: " + (lastCreatedTs != null ? lastCreatedTs : "null"));
//
//        // If this is the first pull (lastCreatedTs is null), print the top 5 citations
//        if (lastCreatedTs == null) {
//            System.out.println("First pull detected. Fetching and printing top 5 citations...");
//            printTop5Citations();
//        }
//
//        // Publish the current timestamp
//        pubSubPublisher.publishTsMessage(LAST_RECEIVED_TOPIC, "Test Application Activity Logger", System.currentTimeMillis());
//    }

//    @Scheduled(fixedRate = 300000) // Runs every 5 minutes
//    public void processActivityLog() {
//        // Pull the last received timestamp from Pub/Sub
//        String lastCreatedTsStr = pullLastReceivedTs();
////        Long lastCreatedTs = lastCreatedTsStr != null ? Long.valueOf(lastCreatedTsStr) : null;
//        Long lastCreatedTs = lastCreatedTsStr != null ? Long.valueOf(lastCreatedTsStr) : null;
//        System.out.println("Last received createdTs: " + (lastCreatedTs != null ? lastCreatedTs : "null"));
//
//        // Fetch new citations created after the last recorded timestamp
//        Pageable pageable = PageRequest.of(0, 5, Sort.by("updatedTs").descending());
//        List<CitationBase> newCitations = lastCreatedTs != null
//                ? citationRepository.findByUpdatedTsGreaterThanOrderByUpdatedTsAsc(lastCreatedTs)
//                : citationRepository.findTop5ByOrderByUpdatedTsDesc(pageable); // Default to last 5 critics on first run
//
//        if (!newCitations.isEmpty()) {
//            // Print new critics
//            System.out.println("New Citations Since Last Log:");
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            newCitations.forEach(citation -> {
//                String readableTimestamp = sdf.format(new Date(citation.getUpdatedTs()));
//                System.out.println(citation + " [Updated At: " + readableTimestamp + "]");
//            });
//
//            // Update Pub/Sub with the latest citation timestamp
//            Long latestTs = newCitations.get(newCitations.size() - 1).getUpdatedTs();
//            pubSubPublisher.publishTsMessage(LAST_RECEIVED_TOPIC, "Updated Last Created Timestamp", latestTs);
//        } else {
//            System.out.println("No new citation since last log.");
//        }
//    }

    //This version gets a LastCreatedTs is null error
    /*
        @Scheduled(fixedRate = 300000) // Runs every 5 minutes
        public void processActivityLog() {
            String lastCreatedTsStr = pullLastReceivedTs();
            Long lastCreatedTs = (lastCreatedTsStr != null && !lastCreatedTsStr.isEmpty())
                    ? Long.valueOf(lastCreatedTsStr)
                    : null;
            this.lastCreatedTs = lastCreatedTs;

            try {
                // Fetch new critics or the top 5 for the first run
                List<CitationBase> newCitations = lastCreatedTs != null
                        ? citationRepository.findByUpdatedTsGreaterThanOrderByUpdatedTsAsc(lastCreatedTs)
                        : citationRepository.findTop5ByOrderByUpdatedTsDesc(PageRequest.of(0, 5, Sort.by("updatedTs").descending()));

                if (newCitations.isEmpty()) {
                    System.out.println("No new critics found.");
                    return; // Nothing to process
                }

                // Print the retrieved critics
                System.out.println("Citations to process:");
                for (CitationBase citation : newCitations) {
                    String readableTimestamp = Instant.ofEpochMilli(citation.getUpdatedTs())
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    System.out.println("Content: " + citation.getText() + ", UpdatedTs: " + readableTimestamp);
                }

                // Update lastCreatedTs with the most recent timestamp
                lastCreatedTs = newCitations.stream()
                        .mapToLong(CitationBase::getUpdatedTs)
                        .max()
                        .orElse(lastCreatedTs);

                // Publish the updated timestamp for synchronization
                pubSubPublisher.publishTsMessage("LastReceivedTs", "", lastCreatedTs);

            } catch (Exception e) {
                System.err.println("Error processing activity log: " + e.getMessage());
            }
        }
*/
    @Scheduled(fixedRate = 120000) // Runs every 5 minutes
    public void processActivityLog() {
        System.out.println("Starting activity log processing...");

        // Retrieve the last timestamp from the subscription
        String lastCreatedTsStr = pullLastReceivedTs();
        Long lastCreatedTs = null;

        /*
        //This method works but needed the final upgrade
        if (lastCreatedTsStr != null && !lastCreatedTsStr.isBlank()) {
            try {
                lastCreatedTs = Long.valueOf(lastCreatedTsStr);
                System.out.println("Last recorded timestamp: " + lastCreatedTs + " (" +
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(lastCreatedTs)) + ")");
            } catch (NumberFormatException e) {
                System.err.println("Failed to parse last timestamp: " + lastCreatedTsStr);
            }
        }

        */
        if (lastCreatedTsStr != null && !lastCreatedTsStr.isBlank()) {
            try {
                // Parse the last created timestamp
                lastCreatedTs = Long.parseLong(lastCreatedTsStr);
                System.out.println("Last timestamp found: " + lastCreatedTs);

                // Fetch citations updated since the last timestamp
                List<CitationBase> newCitations = citationRepository.findByUpdatedTsGreaterThanOrderByUpdatedTsAsc(lastCreatedTs);

                if (newCitations != null && !newCitations.isEmpty()) {
                    System.out.println("New Citations since last timestamp:");
                    newCitations.forEach(citation -> {
                        Instant instant = Instant.ofEpochMilli(citation.getUpdatedTs());
                        LocalDateTime localDateTime =
                                LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));  // TODO Handle time zones
                        String readableTimestamp = FORMATTER.format(localDateTime);
                        System.out.println("Citation: " + citation.getEntityId() + ", UpdatedAt: " + readableTimestamp);
                    });
                    // Publish each Citation as a separate JSON message
                    pubSubPublisher.publishCitationBackupAsJson("Citations-backup", newCitations);
                } else {
                    System.out.println("No Citations activities were registered.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Failed to parse last timestamp: " + lastCreatedTsStr);
            }
        }


        else {
            System.out.println("First run. No previous messages, sir.");
        }

        // Check if it's the first run
        if (lastCreatedTs == null) {
            System.out.println("Fetching top 5 Citations...");
            List<CitationBase> topCitations = citationRepository.findTop5ByOrderByUpdatedTsDescWithRelations(PageRequest.of(0, 5));
            if (topCitations != null && !topCitations.isEmpty()) {
                System.out.println("Latest 5 Citations:");

                topCitations.forEach(citation -> {
                    Instant instant = Instant.ofEpochMilli(citation.getUpdatedTs());
                    LocalDateTime localDateTime =
                            LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));  // TODO Handle time zones
                    String readableTimestamp = FORMATTER.format(localDateTime);
                    System.out.println("Citation: " + citation.getEntityId() + " | Timestamp: " + readableTimestamp);
                });
                // Publish each critic as a JSON backup
                try {
                    pubSubPublisher.publishCitationBackupAsJson("Citations-backup", topCitations);
                    System.out.println("Published top 5 critics successfully.");
                } catch (Exception e) {
                    System.err.println("Error publishing top 5 critics: " + e.getMessage());
                }

            } else {
                System.out.println("No Citations found in the database.");
            }
        }


        // Publish the current timestamp to the LastReceivedTs topic
        pubSubPublisher.publishTsMessage("LastReceivedTs", "Activity log update.");
        System.out.println("Activity log processing completed.");
    }





    private String pullLastReceivedTs() {
        String[] lastMessage = new String[1]; // To store the pulled message

        Subscriber subscriber = null;
        try {
            subscriber = Subscriber.newBuilder(
                    ProjectSubscriptionName.of(PROJECT_ID, SUBSCRIPTION_ID),
                    (message, consumer) -> {
                        System.out.println("Received message: " + message.getData().toStringUtf8());
                        lastMessage[0] = message.getData().toStringUtf8(); // Capture the message
                        consumer.ack(); // Acknowledge the message
                    }).build();

            subscriber.startAsync().awaitRunning();
            TimeUnit.SECONDS.sleep(5); // Allow some time to pull messages
        } catch (Exception e) {
            System.err.println("Error pulling messages: " + e.getMessage());
        } finally {
            if (subscriber != null) {
                subscriber.stopAsync(); // Ensure the subscriber is stopped properly
            }
        }

        return lastMessage[0];
    }
}
