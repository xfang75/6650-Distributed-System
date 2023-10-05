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
public class AlbumsServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    res.setContentType("text/plain");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();
    Gson gson = new Gson();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      ErrorMsg error = new ErrorMsg().msg("Empty albums get request");
      String albumString = gson.toJson(error);
      res.getWriter().write(albumString);
      return;
    }

    String[] urlParts = urlPath.split("/");
    if (!isUrlValid(urlParts)) {
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
      res.getWriter().write("OK");
    }
  }

  private boolean isUrlValid(String[] urlPath) {
    // /albums/{albumID}, we only allow get album by key
    if (urlPath.length == 2 && !urlPath[1].isEmpty()) {
      return true;
    }
    return false;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    Gson gson = new Gson();
//    String urlPath = req.getPathInfo();
//    String servletPath = req.getServletPath();
//    if (urlPath == null || urlPath.isEmpty()) {
//      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//      ErrorMsg error = new ErrorMsg().msg("Empty albums post request");
//      String albumString = this.gson.toJson(error);
//      res.getWriter().write(albumString);
//      return;
//    }
//    res.setStatus(HttpServletResponse.SC_OK);
//    InputStream inputStream = req.getInputStream();
//    int imageSize = inputStream.readAllBytes().length;
//    String imageResult = gson.toJson(new ImageMetaData().albumID("albumId").imageSize(String.valueOf(imageSize)));
    res.getWriter().write("imageResult");
  }
}
