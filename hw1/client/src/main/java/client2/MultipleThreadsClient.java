package client2;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
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

  static CopyOnWriteArrayList<Long> getList = new CopyOnWriteArrayList<>();
  static CopyOnWriteArrayList<Long> postList = new CopyOnWriteArrayList<>();

  private static final String SERVER_URL = "http://ec2-3-87-217-142.compute-1.amazonaws.com:8080/albumsServlet_war/";

  // Replace the following url for go server
//  private static final String SERVER_URL = "http://ec2-3-87-217-142.compute-1.amazonaws.com:8080/AlbumStore/1.0.0/";



  public static void main(String[] args) throws InterruptedException, ApiException {
    MultipleThreadsClient client = new MultipleThreadsClient();
    client.AlbumClient(10, 10, 2, SERVER_URL);
    //    client.AlbumClient(20, 10, 2, SERVER_URL);
    //    client.AlbumClient(30, 10, 2, SERVER_URL);
    ArrayList<Long> getResponseTimeArrayList = new ArrayList<>(getList);
    ArrayList<Long> postResponseTimeArrayList = new ArrayList<>(postList);
    System.out.println("=== Below are data for get responses: ");
    client.calculateResponseTime(getResponseTimeArrayList);
    System.out.println("=== Below are data for post responses: ");
    client.calculateResponseTime(postResponseTimeArrayList);
  }

  private void calculateResponseTime(ArrayList<Long> responseTimeArrayList) {
    Collections.sort(responseTimeArrayList);
    long responseTotal = 0;
    for (long responseTime : responseTimeArrayList) {
      responseTotal += responseTime;
    }
    double averateResponse = (double)responseTotal / responseTimeArrayList.size();
    System.out.println("The average of the response time is: " + averateResponse);
    int middle = responseTimeArrayList.size() / 2;
    double medianResponseTime;
    if (responseTimeArrayList.size() % 2 == 0) {
      medianResponseTime = (responseTimeArrayList.get(middle - 1) + responseTimeArrayList.get(middle)) / 2.0;
    } else {
      medianResponseTime = responseTimeArrayList.get(middle);
    }
    System.out.println("The median of the response time is: " + medianResponseTime);
    int p99Index = (int) Math.ceil(responseTimeArrayList.size() * 0.99);
    long p99ResponseTime = responseTimeArrayList.get(p99Index - 1); // -1 because of 0-based index
    System.out.println("The P99 of the response time is: " + p99ResponseTime);
    System.out.println("The min of the response time is: " + responseTimeArrayList.get(0));
    System.out.println("The max of the response time is: " + responseTimeArrayList.get(responseTimeArrayList.size() - 1));
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
    System.out.println("Finish total " + successRequests + " success requests with " +
        failedRequests.get() + " failed requests");
  }

  private void StartMultipleThreads(int numThreadGroups, int threadGroupSize, int delay, String serverURL, int callAPIKTime)
      throws InterruptedException, ApiException {
    int totalThreadCount = numThreadGroups * threadGroupSize;
    ExecutorService executorService = Executors.newFixedThreadPool(totalThreadCount);

    try {
      FileWriter fileWriter = new FileWriter("result.csv");
      CSVWritter csvWritter = new CSVWritter(fileWriter);
      for (int group = 0; group < numThreadGroups; group++) {
        System.out.println("Starting Thread Group " + group);
        CountDownLatch latch = new CountDownLatch(threadGroupSize);
        for (int i = 0; i < threadGroupSize; i++) {
          executorService.execute(() -> {
            try {
              for (int j = 0; j < callAPIKTime; j++) {
                this.callAPIRequest(serverURL, csvWritter, "GET");
                this.callAPIRequest(serverURL, csvWritter, "POST");
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
//          System.out.println("Waiting for " + delay + " seconds before starting the next Thread Group.");
          // Convert the delay time from seconds to mili seconds
          Thread.sleep(delay * 1000);
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Caught IO exception while writing to csv");
    } finally {
      executorService.shutdown();
    }
  }

  private void callAPIRequest(String serverURL, CSVWritter csvWritter, String apiType)
      throws InterruptedException, IOException {
    SingleThreadClient client = new SingleThreadClient();
    boolean requestSuccess = false;
    int retryCount = 0;
    long startTime = System.currentTimeMillis();
    int responseCode = 0;
    ApiResponse<?> response;

    while (!requestSuccess && retryCount < MAX_RETRIES) {
      try {
        if (apiType.equals("GET")) {
          response = client.getAlbumWithHttpInfo(serverURL);
          responseCode = response.getStatusCode();
        } else {
          response = client.postAlbumWithHttpInfo(serverURL + "albums/");
          responseCode = response.getStatusCode();
        }
        requestSuccess = true;
      } catch (Exception e) {
        retryCount++;
        // Wait for 1 second before retrying
        Thread.sleep(1 * 1000);
      }
    }

    if (!requestSuccess) {
      // When the request failed, we want to count this as a failed requests
      failedRequests.incrementAndGet();
      System.out.println("Request failed after " + MAX_RETRIES + " retries.");
    } else {
      long endTime = System.currentTimeMillis();
      long latency = endTime - startTime;
      csvWritter.writeToCSV(String.valueOf(startTime), apiType, latency, responseCode);
      if (apiType.equals("GET")) {
        getList.add(latency);
      } else {
        postList.add(latency);
      }
    }
  }
}
