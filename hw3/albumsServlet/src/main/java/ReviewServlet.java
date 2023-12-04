import com.zaxxer.hikari.HikariDataSource;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import com.google.gson.Gson;
import model.AlbumInfo;
import model.ErrorMsg;
import model.ImageMetaData;

@WebServlet(name = "ReviewServlet", value = "/review/*")
@MultipartConfig(fileSizeThreshold = 10485760,    // 10 MB
    maxFileSize = 10485760,        // 10 MB
    maxRequestSize = 209715200)    // 200 MB
public class ReviewServlet extends HttpServlet {
  private Gson gson = new Gson();
  private HikariDataSource connectionPool = SQLConnector.createDataSource();
  private ConnectionFactory factory;
  private Connection rbmqConnection;
  private Channel channel;
  ConcurrentLinkedDeque<Channel> channelPool;

  private static final String MQ_SERVER_URL = "amqps://b-8ba40c3e-1f8b-4dca-854a-14cf5df952ca.mq.us-east-1.amazonaws.com:5671";
  private static final String username = "fangxun";
  private static final String password = "yangfangxunyangxiangxiang";
  private static final String QUEUE_NAME = "reviewQueue";
  private final static int CHANNEL_SIZE = 100;
  private final static int EXECUTOR_SIZE = 100;

  private ExecutorService executorService;

  @Override
  public void init() throws ServletException {
    connectionPool =  SQLConnector.createDataSource();
    channelPool = new ConcurrentLinkedDeque<>();
    executorService = Executors.newFixedThreadPool(EXECUTOR_SIZE);
    try {
      factory = new ConnectionFactory();
      factory.setUri(MQ_SERVER_URL);
      factory.setUsername(username);
      factory.setPassword(password);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ServletException("Failed to initialize connection factory" + e.getMessage());
    }

    try {
      this.rbmqConnection = factory.newConnection();
      for (int i = 0; i < CHANNEL_SIZE; i++) {
        Channel channel = rbmqConnection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channelPool.add(channel);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new ServletException("Failed to initialize connection or channel" + e.getMessage());
    }
  }

  public void close() {
    connectionPool.close();
    closeChannelPool(channelPool);
  }

  public void closeChannelPool(ConcurrentLinkedDeque<Channel> channelPool) {
    for (Channel channel : channelPool) {
      try {
        channel.close();
      } catch (IOException | TimeoutException e) {
        throw new RuntimeException(e);
      }
    }
  }


  private boolean isPostUrlValid(HttpServletRequest req) {
    // We want to check if we get a valid post request url
    String urlPath = req.getPathInfo();
    if (urlPath == null) {
      return false;
    }
    return req.getPathInfo().split("/").length == 1;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    if (!isPostUrlValid(req)) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      ErrorMsg error = new ErrorMsg().msg("Invalid empty review post request");
      String result = this.gson.toJson(error);
      res.getWriter().write(result);
      return;
    }

    String urlPath = req.getPathInfo();
    String[] urlParts = urlPath.split("/");
    String reviewType = urlParts[1];
    String albumId = urlParts[2];
    HashMap<String, String> messageData = new HashMap<>();
    messageData.put("albumId", albumId);
    messageData.put("LikeOrDislike", reviewType);
    String messageToSend = new Gson().toJson(messageData);

    String messageToClient = null;
    boolean like = true;
    if (reviewType.equals("like")) {
      messageToClient = "AlbumID " + albumId + " +1 " + "like";
    }

    if (reviewType.equals("dislike")) {
      like = false;
      messageToClient = "AlbumID " + albumId + " +1 " + "dislike";
    }
    if (messageToClient != null && !messageToClient.isEmpty()) {
      String finalMessage = messageToClient;
      executorService.submit(() -> {
        try {
          sendToQueue(messageToSend);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    }

//    try (java.sql.Connection connection = this.connectionPool.getConnection()) {
//      String updateLikes =
//          "UPDATE AlbumsWithLikes "
//              + "SET Likes = Likes + 1"
//              + "WHERE AlbumId = ?;";
//      String updateDislikes =
//          "UPDATE AlbumsWithLikes "
//              + "SET Dislikes = Dislikes + 1"
//              + "WHERE AlbumId = ?;";
//
//      PreparedStatement preparedStatement;
//      if (like) {
//        preparedStatement = connection.prepareStatement(updateLikes);
//      } else {
//        preparedStatement = connection.prepareStatement(updateDislikes);
//      }
//      preparedStatement.setString(1, albumId);
//      res.setStatus(HttpServletResponse.SC_OK);
//      preparedStatement.executeUpdate();
//      ResultSet resultKey = preparedStatement.getGeneratedKeys();
//      int imageId = -1;
//      if(!resultKey.next()) {
//        throw new SQLException("Unable to retrieve auto-generated key.");
//      }
//      res.getWriter().write(messageToClient);
//    } catch (SQLException e) {
//      throw new RuntimeException(e);
//    }
  }

  private void sendToQueue(String msg) throws Exception {
    try {
      channel = this.channelPool.removeFirst();
      if (channel != null) {
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.basicPublish("", QUEUE_NAME, null, msg.getBytes("UTF-8"));
      } else {
        System.out.println("No channel available");
        Thread.sleep(1 * 1000);
        return;
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new Exception("Failed to send to the queue");
    } finally {
      if (channel != null) {
        channelPool.offer(channel);
      }
    }
  }
}
