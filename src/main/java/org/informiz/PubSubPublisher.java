package org.informiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import org.informiz.model.CitationBase;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class PubSubPublisher {

    private static final String PROJECT_ID = "key-master-283113"; // Replace with your actual project ID
    private Long lastCreatedTs = null;


    /**
     * Publishes a message to a specific Pub/Sub topic.
     *
     * @param topicId    the Pub/Sub topic ID
     * @param content    the message content
     */
//    public void publishTsMessage(String topicId, String content, Long createdTs) {
//        ProjectTopicName topicName = ProjectTopicName.of(PROJECT_ID, topicId);
//
//        Publisher publisher = null;
//        try {
//            publisher = Publisher.newBuilder(topicName).build();
//
//            // Construct the Pub/Sub message
//            String messageData = String.format("Content: %s, CreatedTs: %d", content, createdTs);
//            PubsubMessage message = PubsubMessage.newBuilder()
//                    .setData(ByteString.copyFromUtf8(messageData))
//                    .build();
//
//            // Publish the message
//            ApiFuture<String> messageIdFuture = publisher.publish(message);
//            String messageId = messageIdFuture.get(); // Wait for the publish operation
//            System.out.println("Published message ID: " + messageId);
//        } catch (Exception e) {
//            System.err.println("Error publishing message: " + e.getMessage());
//        } finally {
//            if (publisher != null) {
//                try {
//                    publisher.shutdown(); // Ensure the publisher is properly shut down
//                } catch (Exception e) {
//                    System.err.println("Error shutting down publisher: " + e.getMessage());
//                }
//            }
//        }
//    }

    /*
    public void publishTsMessage(String topicId, String content, Long createdTs) {
        ProjectTopicName topicName = ProjectTopicName.of(PROJECT_ID, topicId);
        Publisher publisher = null;

        try {
            publisher = Publisher.newBuilder(topicName).build();

            // Convert createdTs to readable format
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //String readableTimestamp = createdTs != null ? sdf.format(new Date(createdTs)) : "null";
            String readableTimestamp = lastCreatedTs != null ? sdf.format(new Date(lastCreatedTs)) : "First Run (no prior timestamp)";


            // Construct the Pub/Sub message
//            String messageData = String.format("Content: %s, CreatedTs: %d (%s)", content, createdTs, readableTimestamp);
            String messageData = String.valueOf(createdTs);
            PubsubMessage message = PubsubMessage.newBuilder()
                    .setData(ByteString.copyFromUtf8(messageData))
                    .build();

            // Publish the message
            ApiFuture<String> messageIdFuture = publisher.publish(message);
            String messageId = messageIdFuture.get(); // Wait for the publish operation
            System.out.println("Published message ID: " + messageId);
            System.out.println("Published message with readable timestamp: " + readableTimestamp);

        } catch (Exception e) {
            System.err.println("Error publishing message: " + e.getMessage());
        } finally {
            if (publisher != null) {
                try {
                    publisher.shutdown(); // Ensure the publisher is properly shut down
                } catch (Exception e) {
                    System.err.println("Error shutting down publisher: " + e.getMessage());
                }
            }
        }
    }
    */
//This method creates its own Ts:
    public void publishTsMessage(String topicId, String content) {
        ProjectTopicName topicName = ProjectTopicName.of(PROJECT_ID, topicId);
        Publisher publisher = null;

        try {
            publisher = Publisher.newBuilder(topicName).build();

            // Generate the current timestamp
            long currentTs = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String readableTimestamp = sdf.format(new Date(currentTs));

            // Set the raw timestamp as the message
            String messageData = String.valueOf(currentTs);
            PubsubMessage message = PubsubMessage.newBuilder()
                    .setData(ByteString.copyFromUtf8(messageData))
                    .build();

            // Publish the message
            ApiFuture<String> messageIdFuture = publisher.publish(message);
            String messageId = messageIdFuture.get(); // Wait for the publish operation
            System.out.println("Published message ID: " + messageId);
            System.out.println("Published timestamp: " + currentTs + " (" + readableTimestamp + ")");

        } catch (Exception e) {
            System.err.println("Error publishing message: " + e.getMessage());
        } finally {
            if (publisher != null) {
                try {
                    publisher.shutdown(); // Ensure the publisher is properly shut down
                } catch (Exception e) {
                    System.err.println("Error shutting down publisher: " + e.getMessage());
                }
            }
        }
    }

    public void publishCitationBackupAsJson(String topicId, List<CitationBase> citations) {
        ProjectTopicName topicName = ProjectTopicName.of(PROJECT_ID, topicId);
        Publisher publisher = null;

        try {
            publisher = Publisher.newBuilder(topicName).build();

            ObjectMapper objectMapper = new ObjectMapper(); // JSON serializer

            for (CitationBase citatinon : citations) {
                try {
                    // Convert the individual Citation to JSON
                    String jsonData = objectMapper.writeValueAsString(citatinon);

                    // Construct and publish the Pub/Sub message
                    PubsubMessage message = PubsubMessage.newBuilder()
                            .setData(ByteString.copyFromUtf8(jsonData))
                            .build();

                    ApiFuture<String> messageIdFuture = publisher.publish(message);
                    String messageId = messageIdFuture.get(); // Wait for publish operation
                    System.out.println("Published message ID: " + messageId);

                } catch (Exception e) {
                    System.err.println("Error publishing Citation ID " + citatinon.getId() + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error initializing publisher: " + e.getMessage());
        } finally {
            if (publisher != null) {
                try {
                    publisher.shutdown();
                } catch (Exception e) {
                    System.err.println("Error shutting down publisher: " + e.getMessage());
                }
            }
        }
    }


}
