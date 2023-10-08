package client1;

import io.swagger.client.ApiException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultipleThreadsClient {
  private static final String SERVER_URL = "http://ec2-54-205-15-109.compute-1.amazonaws.com:8080/albumsServlet_war/";
  private static final int MAX_RETRIES = 5;

  public static void main(String[] args) throws InterruptedException, ApiException {
//    System.out.println(client.getAlbum(serverURL));
//    System.out.println(client.postAlbum(serverURL + "albums/"));
    MultipleThreadsClient client = new MultipleThreadsClient();
    client.StartMultipleThreads(10, 10, 1, SERVER_URL);
  }

  private void StartMultipleThreads(int numThreadGroups, int threadGroupSize, int delay, String serverURL)
      throws InterruptedException, ApiException {
    ExecutorService executorService = Executors.newFixedThreadPool(threadGroupSize);
    long startTime = System.currentTimeMillis();

    try {
      for (int group = 0; group < numThreadGroups; group++) {
        System.out.println("Starting Thread Group " + group);
        CountDownLatch latch = new CountDownLatch(threadGroupSize);

        for (int i = 0; i < threadGroupSize; i++) {
          executorService.execute(() -> {
            try {
              for (int j = 0; j < 2; j++) {
                boolean requestSuccess = false;
                int retryCount = 0;

                while (!requestSuccess && retryCount < MAX_RETRIES) {
                  try {
                    SingleThreadClient client = new SingleThreadClient();
                    System.out.println(client.getAlbum(serverURL));
                    System.out.println(client.postAlbum(serverURL + "albums/"));
                    requestSuccess = true;
                  } catch (Exception e) {
                    retryCount++;
                    System.out.println("Retrying request (Retry Count: " + retryCount + ")");
                    e.printStackTrace();
                    // Wait for 2 second before retrying
                    Thread.sleep(2 * 1000);
                  }
                }

                if (!requestSuccess) {
                  System.out.println("Request failed after " + MAX_RETRIES + " retries.");
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
            } finally {
              latch.countDown();
            }
          });
        }

        latch.await(); // Wait for all threads in the group to complete

        // "Waiting for delay seconds before starting the next Thread Group, but not delay for the last thread group
        if (group < numThreadGroups) {
          System.out.println("Waiting for " + delay + " seconds before starting the next Thread Group.");
          // Convert the delay time from seconds to mili seconds
          Thread.sleep(delay * 1000);
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      executorService.shutdown();
    }

//    for (int i = 0; i < numThreadGroups; i++) {
//      for (int j = 0; j < threadGroupSize; j++) {
//        SingleThreadClient client = new SingleThreadClient();
//        client.getAlbum(serverURL);
//        client.postAlbum(serverURL + "albums/");
//      }
//
//      // Converted the sleep time to mili seconds
//      Thread.sleep(delay * 1000);
//    }
  }
}
