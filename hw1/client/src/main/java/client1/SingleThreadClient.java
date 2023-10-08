package client1;

import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;
import java.io.File;

public class SingleThreadClient {
  public static void main(String[] args) throws InterruptedException, ApiException {
    // The url for java server
    String serverURL = "http://http://ec2-54-198-84-36.compute-1.amazonaws.com:8080/AlbumStore/1.0.0/";
    // The url for go server
//    String serverURL = "http://ec2-3-90-1-139.compute-1.amazonaws.com:8080/AlbumStore/1.0.0/";
    SingleThreadClient client = new SingleThreadClient();

    System.out.println(client.getAlbum(serverURL));
    System.out.println(client.postAlbum(serverURL + "albums/"));

  }

  public AlbumInfo getAlbum(String url) throws ApiException {
    DefaultApi apiInstance = new DefaultApi();
    String albumID = "albumID_example"; // String | path  parameter is album key to retrieve
    apiInstance.getApiClient().setBasePath(url);
    return apiInstance.getAlbumByKey(albumID);
  }

  public ImageMetaData postAlbum(String url) throws ApiException {
    DefaultApi apiInstance = new DefaultApi();
    apiInstance.getApiClient().setBasePath(url);
    File image = new File("nmtb.png");
    AlbumsProfile profile = new AlbumsProfile().artist("Sex Pistols").title("Never Mind The Bollocks!").year("1977");
    return apiInstance.newAlbum(image, profile);
  }
}