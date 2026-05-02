package com.example.rabbitmq;

import com.rabbitmq.client.amqp.Connection;
import com.rabbitmq.client.amqp.Consumer;
import com.rabbitmq.client.amqp.Environment;
import com.rabbitmq.client.amqp.impl.AmqpEnvironmentBuilder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public class Worker {

    // The queue both workers and publishers share - same name = same queue
    private static final String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] argv) throws Exception {

        // Same environment/connection setup as before

        Environment environment = new AmqpEnvironmentBuilder()
                .connectionSettings()
                .uri("amqp://admin:admin@localhost:5672/%2f")
                .environmentBuilder()
                .build();
        Connection connection = environment.connectionBuilder().build();

        // Declare the queue here too, because the worker might start
        // before NewTask has a chance to create it
        connection.management().queue(TASK_QUEUE_NAME).quorum().queue().declare();

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = connection.consumerBuilder()
                .queue(TASK_QUEUE_NAME)
                // initialCredits(1) means "only give me 1 message at a time"
                // Worker won't receive message 2 until it finishes message 1
                // This is called "fair dispatch" - without this, RabbitMQ blindly
                // round-robins even if one worker is already busy
                .initialCredits(1)
                .messageHandler((context, message) -> {
                    // Convert raw bytes back to a String
                    String text = new String(message.body(), StandardCharsets.UTF_8);
                    System.out.println(" [x] Received '" + text + "'");
                    try {
                        // Simulate doing actual work based on the message
                        doWork(text);
                    } finally {
                        // finally block ensures accept() is ALWAYS called
                        // even if doWork() throws an exception
                        System.out.println(" [x] Done");
                        // Tell RabbitMQ "I finished this task, remove it from the queue"
                        // If we crash before this, RabbitMQ redelivers the message to another worker
                        context.accept();
                    }
                })
                .build();

        // Keep the program alive forever, waiting for messages
        // The latch is never counted down so it waits until Ctrl+C
        new CountDownLatch(1).await();
    }

    // Simulates work by sleeping 1 second per dot in the message
    // "Hello..."  = 3 seconds of work
    // "Hi."       = 1 second of work
    // "Hey"       = 0 seconds (no dots)
    private static void doWork(String task) {
        for (char ch : task.toCharArray()) {
            if (ch == '.') {
                try {
                    Thread.sleep(1000); // sleep 1 second per dot
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}