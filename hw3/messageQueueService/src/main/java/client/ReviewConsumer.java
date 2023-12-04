package client;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


public class ReviewConsumer {
  private static final String MQ_SERVER_URL = "amqps://b-8ba40c3e-1f8b-4dca-854a-14cf5df952ca.mq.us-east-1.amazonaws.com:5671";
  private static final String username = "fangxun";
  private static final String password = "yangfangxunyangxiangxiang";
  private static final String queueName = "reviewQueue";
  private static ConnectionFactory factory;
  private static Connection rbmqConnection;
  private Channel channel;



  public static void main(String[] args) throws Exception {
    int threadGroupSize = 10;
    int numThreadGroups = 10;
    int delay = 2;
    if (args.length == 3) {
      threadGroupSize = Integer.parseInt(args[0]);
      numThreadGroups = Integer.parseInt(args[1]);
      delay = Integer.parseInt(args[2]);
    }
    try {
      factory = new ConnectionFactory();
      factory.setUri(MQ_SERVER_URL);
      factory.setUsername(username);
      factory.setPassword(password);
      rbmqConnection = factory.newConnection();
    } catch (Exception e) {
      e.printStackTrace();
    }
    ThreadGroup[] threadGroups = new ThreadGroup[numThreadGroups];
    Thread[] threads = new Thread[numThreadGroups * threadGroupSize];
    for (int i = 0; i < numThreadGroups; i++) {
      threadGroups[i] = new ThreadGroup("Rabbit MQ Group" + i);
      for (int j = 0; j < threadGroupSize; j++) {
        ReceiveRunnable receiveRunnable = new ReceiveRunnable(rbmqConnection);
        threads[i * threadGroupSize + j] = new Thread(threadGroups[i], receiveRunnable);
        threads[i * threadGroupSize + j].start();
      }
      Thread.sleep(delay);
    }
    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println(e.getMessage());
      }
    }

  }
}