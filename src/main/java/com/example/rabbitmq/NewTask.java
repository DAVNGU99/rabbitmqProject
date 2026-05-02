package com.example.rabbitmq;

import com.rabbitmq.client.amqp.Connection;
import com.rabbitmq.client.amqp.Environment;
import com.rabbitmq.client.amqp.Publisher;
import com.rabbitmq.client.amqp.impl.AmqpEnvironmentBuilder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NewTask {

//    mvn -q compile exec:java -Dexec.mainClass=com.example.rabbitmq.NewTask -Dexec.args="First task..."
//    mvn -q compile exec:java -Dexec.mainClass=com.example.rabbitmq.NewTask -Dexec.args="Second task......"
//    mvn -q compile exec:java -Dexec.mainClass=com.example.rabbitmq.NewTask -Dexec.args="Third task."
//    mvn -q compile exec:java -Dexec.mainClass=com.example.rabbitmq.NewTask -Dexec.args="Fourth task...."

    private static final String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] args) throws Exception {

        try (Environment environment = new AmqpEnvironmentBuilder()
                .connectionSettings()
                .uri("amqp://admin:admin@localhost:5672/%2f")
                .environmentBuilder()
                .build();

             Connection connection = environment.connectionBuilder().build()){

            connection.management().queue(TASK_QUEUE_NAME).quorum().queue().declare();

            String message = String.join(" ", args);

            try (Publisher publisher = connection.publisherBuilder().queue(TASK_QUEUE_NAME).build()) {
                CountDownLatch latch = new CountDownLatch(1);
                publisher.publish(
                        publisher.message(message.getBytes(StandardCharsets.UTF_8)).durable(true),
                        context -> {
                            if (context.status() == Publisher.Status.ACCEPTED) {
                                System.out.println(" [x] Sent '" + message + "'");
                            }
                            latch.countDown();
                        });
                if (!latch.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Timed out waiting for publish outcome");
                }
            }
        }
    }
}



