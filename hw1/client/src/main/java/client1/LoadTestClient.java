package client1;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;
import java.io.File;
import java.util.concurrent.CountDownLatch;

public class LoadTestClient {
  public static void main(String[] args) throws InterruptedException, ApiException {
    File image = new File("nmtb.png");
//    int numReqs = 100;
    AlbumsProfile profile = new AlbumsProfile().artist("Sex Pistols").title("Never Mind The Bollocks!").year("1977");

    String javaServerURL = "http://ec2-3-90-1-139.compute-1.amazonaws.com:8080/albumsServlet_war/";
    String goServerURL = "http://ec2-3-90-1-139.compute-1.amazonaws.com:8080/AlbumStore/1.0.0/";

    long start;
    long end;
    DefaultApi apiInstance = new DefaultApi();
    String albumID = "albumID_example"; // String | path  parameter is album key to retrieve
    apiInstance.getApiClient().setBasePath(javaServerURL);

    try {
      AlbumInfo result = apiInstance.getAlbumByKey(albumID);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#getAlbumByKey");
      e.printStackTrace();
    }
    try {
//      ApiResponse<ImageMetaData> response = apiInstance.newAlbumWithHttpInfo(image, profile);
//      System.out.println(response.getData());
      ImageMetaData postResult = apiInstance.newAlbum(image, profile);
      System.out.println(postResult);
    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#newAlbum");
      System.err.println(e.getCode());
      e.printStackTrace();
    }
  }
}