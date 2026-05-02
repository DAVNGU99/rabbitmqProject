package com.example.rabbitmq;

import com.rabbitmq.client.amqp.Connection;
import com.rabbitmq.client.amqp.Environment;
import com.rabbitmq.client.amqp.Publisher;
import com.rabbitmq.client.amqp.impl.AmqpEnvironmentBuilder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class Send {

    private static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws Exception{


        //Build the environment and connection
        Environment environment = new AmqpEnvironmentBuilder()
                .connectionSettings()
                .uri("amqp://admin:admin@localhost:5672/%2f")
                .environmentBuilder()
                .build();
        Connection connection = environment.connectionBuilder().build();


        // Declare the queue

        connection.management()
                .queue(QUEUE_NAME)
                .quorum().queue()
                .declare();

        // Create a publisher and send message

        try (Publisher publisher = connection.publisherBuilder()
                .queue(QUEUE_NAME).build()){

            String message = "Hello world!";
            CountDownLatch latch = new CountDownLatch(1);

            publisher.publish(
                    publisher.message(message.getBytes(StandardCharsets.UTF_8)),
                    context -> {
                        if(context.status() == Publisher.Status.ACCEPTED){
                            System.out.println(" [x] Sent '" + message + "'");
                        }
                        latch.countDown();
                    }
            );

            if(!latch.await(5, TimeUnit.SECONDS)){
                throw new IllegalStateException("Timed out waiting for publish outcome");
            }
        }






    }

    

}
