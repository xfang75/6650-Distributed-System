package client2;

import io.swagger.client.ApiException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MultipleThreadsClient {
  private static final int MAX_RETRIES = 5;

  private static final int START_THREAD = 10;

  private static final int START_LOOP_TIMES = 100;

  private static final int OFFICIAL_LOOP_TIMES = 1000;

  // Use a thread safe Integer to count and calculate how many requests failed
  private AtomicInteger failedRequests = new AtomicInteger();

  private static final String SERVER_URL = "http://ec2-3-87-217-142.compute-1.amazonaws.com:8080/albumsServlet_war/";

  // Replace the following url for go server
//  private static final String SERVER_URL = "http://ec2-3-87-217-142.compute-1.amazonaws.com:8080/AlbumStore/1.0.0/";



  public static void main(String[] args) throws InterruptedException, ApiException {
    MultipleThreadsClient client = new MultipleThreadsClient();
    client.AlbumClient(10, 10, 2, SERVER_URL);
    client.AlbumClient(20, 10, 2, SERVER_URL);
    client.AlbumClient(30, 10, 2, SERVER_URL);
  }

  public void AlbumClient(int numThreadGroups, int threadGroupSize, int delay, String serverURL) throws InterruptedException, ApiException {
    // Starting the 10 threads each calling 100 times API on startup
    this.StartMultipleThreads(1, START_THREAD, delay, serverURL, START_LOOP_TIMES);
    long startTime = System.currentTimeMillis();
    // Starting the official loop threads each calling 1000 times API on startup
    this.StartMultipleThreads(numThreadGroups, threadGroupSize, delay, serverURL, OFFICIAL_LOOP_TIMES);
    long endTime = System.currentTimeMillis();
    double wallTimeInSeconds = ((double)endTime - startTime) / 1000;
    System.out.println("The wall time in second is " + wallTimeInSeconds);
    long totalRequests = numThreadGroups * threadGroupSize * OFFICIAL_LOOP_TIMES * 2;
    long successRequests = totalRequests - failedRequests.get();
    double throughput = (double)successRequests / wallTimeInSeconds;
    System.out.println("The average throughput per second is " + throughput);
  }

  private void StartMultipleThreads(int numThreadGroups, int threadGroupSize, int delay, String serverURL, int callAPIKTime)
      throws InterruptedException, ApiException {
    int totalThreadCount = numThreadGroups * threadGroupSize;
    ExecutorService executorService = Executors.newFixedThreadPool(totalThreadCount);

    try {
      for (int group = 0; group < numThreadGroups; group++) {
        System.out.println("Starting Thread Group " + group);
        CountDownLatch latch = new CountDownLatch(threadGroupSize);
        for (int i = 0; i < threadGroupSize; i++) {
          executorService.execute(() -> {
            try {
              for (int j = 0; j < callAPIKTime; j++) {
                boolean requestSuccess = false;
                int retryCount = 0;

                while (!requestSuccess && retryCount < MAX_RETRIES) {
                  try {
                    SingleThreadClient client = new SingleThreadClient();
                    client.getAlbum(serverURL);
                    client.postAlbum(serverURL + "albums/");
                    requestSuccess = true;
                  } catch (Exception e) {
                    retryCount++;
                    // Uncomment the following two lines for debug purposes
//                    System.out.println("Retrying request (Retry Count: " + retryCount + ")");
//                    e.printStackTrace();

                    // Wait for 1 second before retrying
                    Thread.sleep(1 * 1000);
                  }
                }

                if (!requestSuccess) {
                  // When the request failed, we want to count this as a failed requests
                  failedRequests.incrementAndGet();
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
  }
}
