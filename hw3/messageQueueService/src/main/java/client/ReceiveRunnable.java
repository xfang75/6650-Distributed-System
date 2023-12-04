package client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class ReceiveRunnable implements Runnable{
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private static final String QUEUE_NAME = "reviewQueue";
  private final Connection connection;

  public ReceiveRunnable(Connection connection) {
    this.connection = connection;
  }

  @Override
  public void run() {
    try {
      final Channel channel = connection.createChannel();
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
      channel.basicQos(1);

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), true);
        System.out.println( "Thread ID: " + Thread.currentThread().getId() + " Pull '" + message + "'");
      };
      boolean autoAck = false;
      channel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, consumerTag -> {
      });
    } catch (Exception e) {
      System.out.println("Getting error while pulling from RabbitMQ " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
