package com.example.rabbitmq;

import com.rabbitmq.client.amqp.Connection;
import com.rabbitmq.client.amqp.Consumer;
import com.rabbitmq.client.amqp.Environment;
import com.rabbitmq.client.amqp.impl.AmqpEnvironmentBuilder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public class Recv {

    private static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws Exception{

        //Build the env and connection
        Environment environment = new AmqpEnvironmentBuilder()
                .connectionSettings()
                .uri("amqp://admin:admin@localhost:5672/%2f")
                .environmentBuilder()
                .build();

        Connection connection = environment.connectionBuilder().build();


        //Declare the queue here too. Because we might start the consumer before the publisher, we want to make sure the queue exist before we try to consume message from it

        connection.management().queue(QUEUE_NAME).quorum().queue().declare();



        System.out.println("Waiting for messages. To exit press CTRL + C");

        Consumer consumer = connection.consumerBuilder()
                .queue(QUEUE_NAME)
                .messageHandler((context, message) -> {
                    String text = new String(message.body(), StandardCharsets.UTF_8);
                    System.out.println(" [x] Received '" + text + "'");
                    context.accept();
                })
                .build();

        new CountDownLatch(1).await();
    }
}
