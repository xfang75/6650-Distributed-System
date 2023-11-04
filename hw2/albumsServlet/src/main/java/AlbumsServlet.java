import java.io.InputStream;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import com.google.gson.Gson;
import model.AlbumInfo;
import model.ErrorMsg;
import model.ImageMetaData;

@WebServlet(name = "AlbumsServlet", value = "/albums/*")
@MultipartConfig(fileSizeThreshold = 10485760,    // 10 MB
    maxFileSize = 10485760,        // 10 MB
    maxRequestSize = 209715200)    // 200 MB
public class AlbumsServlet extends HttpServlet {
  private Gson gson = new Gson();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();

    // check we have a non empty URL
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      ErrorMsg error = new ErrorMsg().msg("Empty albums get request");
      String albumString = gson.toJson(error);
      res.getWriter().write(albumString);
      return;
    }

    String[] urlParts = urlPath.split("/");
    if (!isGetUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("error");
      ErrorMsg error = new ErrorMsg().msg("Invalid albums get request");
      String albumString = gson.toJson(error);
      res.getWriter().write(albumString);
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      AlbumInfo albumInfo = new AlbumInfo().artist("Sex Pistols").title("Never Mind The Bollocks!").year("1977");
      String albumString = gson.toJson(albumInfo);
      res.getWriter().write(albumString);
    }
  }

  private boolean isGetUrlValid(String[] urlPath) {
    // /albums/{albumID}, we only allow get album by key
    if (urlPath.length == 2 && !urlPath[1].isEmpty()) {
      return true;
    }
    return false;
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
//    res.setCharacterEncoding("UTF-8");
    Part image = req.getPart("image");
    Part profile = req.getPart("profile");
//    if (!isPostUrlValid(req) || image.getSize() == 0 || profile.getSize() == 0) {
//      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//      ErrorMsg error = new ErrorMsg().msg("Invalid empty albums post request");
//      String albumString = this.gson.toJson(error);
//      res.getWriter().write(albumString);
//      return;
//    }
    res.setStatus(HttpServletResponse.SC_OK);
    int imageSize = image.getInputStream().readAllBytes().length;
    String imageResult = gson.toJson(new ImageMetaData().albumID("albumId").imageSize(String.valueOf(imageSize)));
    res.getWriter().write(imageResult);
  }
}
