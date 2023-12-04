package client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

public class ReceiveRunnable implements Runnable{
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private static final String QUEUE_NAME = "reviewQueue";
  private final Connection connection;
  private final HikariDataSource connectionPool;

  public ReceiveRunnable(Connection connection, HikariDataSource connectionPool) {
    this.connection = connection;
    this.connectionPool = connectionPool;
  }

  @Override
  public void run() {
    try {
      final Channel channel = connection.createChannel();
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
      channel.basicQos(1);

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), true);
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        JsonObject reviewJsonBody = gson.fromJson(message, JsonObject.class);

        String albumId = reviewJsonBody.get("albumId").getAsString();
        String likeOrDislike = reviewJsonBody.get("LikeOrDislike").getAsString();
        boolean like = "Like".equals(likeOrDislike);
        reviewLikes(albumId, like);
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

  private void reviewLikes(String albumId, boolean like) {
    try (java.sql.Connection connection = this.connectionPool.getConnection()) {
      String updateLikes =
          "UPDATE AlbumsWithLikes "
              + "SET Likes = Likes + 1"
              + "WHERE AlbumId = ?;";
      String updateDislikes =
          "UPDATE AlbumsWithLikes "
              + "SET Dislikes = Dislikes + 1"
              + "WHERE AlbumId = ?;";

      PreparedStatement preparedStatement;
      if (like) {
        preparedStatement = connection.prepareStatement(updateLikes);
      } else {
        preparedStatement = connection.prepareStatement(updateDislikes);
      }
      preparedStatement.setString(1, albumId);
      preparedStatement.executeUpdate();
      ResultSet resultKey = preparedStatement.getGeneratedKeys();
      int imageId = -1;
      if(!resultKey.next()) {
        throw new SQLException("Unable to retrieve auto-generated key.");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
