package org.informiz.conf;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class PubSubPublisher {

    private final String projectId = "your-project-id"; // Replace with your GCP project ID

    // Method to publish a message to any given topic
    public void publishMessage(String topicId, String message) {
        TopicName topicName = TopicName.of(projectId, topicId);
        Publisher publisher = null;

        try {
            // Create a publisher instance with the topic name
            publisher = Publisher.newBuilder(topicName).build();  // Might throw IOException

            // Convert the message to bytes and create a PubsubMessage
            ByteString data = ByteString.copyFromUtf8(message);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

            // Publish the message to the topic
            publisher.publish(pubsubMessage).get();

            System.out.println("Message published to topic '" + topicId + "': " + message);

        } catch (IOException e) {
            // Handle IOException thrown when building the Publisher
            System.out.println("Error creating Publisher: " + e.getMessage());
        } catch (InterruptedException | ExecutionException e) {
            // Handle InterruptedException and ExecutionException from publishing
            System.out.println("Error publishing message: " + e.getMessage());
        } finally {
            // Ensure the publisher is shutdown to free resources
            if (publisher != null) {
                publisher.shutdown();
            }
        }
    }
}

