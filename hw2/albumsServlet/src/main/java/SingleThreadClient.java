import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class SingleThreadClient {

  public ApiResponse<AlbumInfo> getAlbumWithHttpInfo(DefaultApi apiInstance, String url) throws ApiException {
    Random rand = new Random();
    int id = rand.nextInt(10);
    apiInstance.getApiClient().setBasePath(url);
    return apiInstance.getAlbumByKeyWithHttpInfo(String.valueOf(id));
  }

  public ApiResponse<ImageMetaData> postAlbumWithHttpInfo(DefaultApi apiInstance, String url) throws ApiException {
    apiInstance.getApiClient().setBasePath(url);
    File image = new File("nmtb.png");

    AlbumsProfile profile = new AlbumsProfile().artist("Sex Pistols").title("Never Mind The Bollocks!").year("1977");
    return apiInstance.newAlbumWithHttpInfo(image, profile);
  }
}