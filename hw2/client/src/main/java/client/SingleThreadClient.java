package client;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SingleThreadClient {
  public static void main(String[] args) throws InterruptedException, ApiException {
    // The url for java server
    String serverURL = "http://ec2-54-205-15-109.compute-1.amazonaws.com:8080/albumsServlet_war/";
    // The url for go server
//    String serverURL = "http://ec2-3-90-1-139.compute-1.amazonaws.com:8080/AlbumStore/1.0.0/";
    SingleThreadClient client = new SingleThreadClient();

//    System.out.println(client.getAlbumWithHttpInfo(serverURL));
//    System.out.println(client.postAlbumWithHttpInfo(serverURL + "albums/"));
  }

  public ApiResponse<AlbumInfo> getAlbumWithHttpInfo(DefaultApi apiInstance, String url) throws ApiException {
    String albumID = "albumID_example"; // String | path  parameter is album key to retrieve
    long startTime = System.currentTimeMillis();
    apiInstance.getApiClient().setBasePath(url);
    long endTime = System.currentTimeMillis();
    return apiInstance.getAlbumByKeyWithHttpInfo(albumID);
  }

  public ApiResponse<ImageMetaData> postAlbumWithHttpInfo(DefaultApi apiInstance, String url) throws ApiException {
    apiInstance.getApiClient().setBasePath(url);
    File image = new File("nmtb.png");

    AlbumsProfile profile = new AlbumsProfile().artist("Sex Pistols").title("Never Mind The Bollocks!").year("1977");
    return apiInstance.newAlbumWithHttpInfo(image, profile);
  }
}