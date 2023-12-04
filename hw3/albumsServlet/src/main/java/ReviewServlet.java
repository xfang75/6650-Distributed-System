import com.zaxxer.hikari.HikariDataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import com.google.gson.Gson;
import model.AlbumInfo;
import model.ErrorMsg;
import model.ImageMetaData;

@WebServlet(name = "ReviewServlet", value = "/review/*")
@MultipartConfig(fileSizeThreshold = 10485760,    // 10 MB
    maxFileSize = 10485760,        // 10 MB
    maxRequestSize = 209715200)    // 200 MB
public class ReviewServlet extends HttpServlet {
  private Gson gson = new Gson();
  private HikariDataSource connectionPool = SQLConnector.createDataSource();

  @Override
  public void init() {
    connectionPool =  SQLConnector.createDataSource();
  }

  public void close() {
    connectionPool.close();
  }

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
      int albumId = Integer.parseInt(urlParts[1]);
      try (Connection connection = this.connectionPool.getConnection()) {
        String selectQuery =
            "SELECT AlbumId, Artist, Title, Year " +
                "FROM Albums " +
                "WHERE AlbumId=?;";
        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
        preparedStatement.setInt(1, albumId);

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
          int resultAlbumId = resultSet.getInt("AlbumId");
          String resultArtist = resultSet.getString("Artist");
          String resultTitle = resultSet.getString("Title");
          int resultYear = resultSet.getInt("Year");
          res.setStatus(HttpServletResponse.SC_OK);
          AlbumInfo albumInfo = new AlbumInfo().artist(resultArtist).title(resultTitle).year(String.valueOf(resultYear));
          String albumString = gson.toJson(albumInfo);
          res.getWriter().write(albumString);
        } else {
          res.setStatus(HttpServletResponse.SC_NOT_FOUND);
          res.getWriter().write("Album ID not found.");
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
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
    res.setCharacterEncoding("UTF-8");
    Part image = req.getPart("image");
    int imageSize = image.getInputStream().readAllBytes().length;
    Part profile = req.getPart("profile");
    String profileParameter = req.getParameter("profile");
    if (!isPostUrlValid(req) || image.getSize() == 0 || profile == null || profile.getSize() == 0) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      ErrorMsg error = new ErrorMsg().msg("Invalid empty albums post request");
      String albumString = this.gson.toJson(error);
      res.getWriter().write(albumString);
      return;
    }
    try (Connection connection = this.connectionPool.getConnection()) {
      String insertAlbumsQuery =
          "INSERT INTO Albums(Artist,Title,Year,ImageSize) " +
              "VALUES(?,?,?,?);";
      PreparedStatement preparedStatement = connection.prepareStatement(insertAlbumsQuery);
      Pattern pattern = Pattern.compile("artist: (.+?)\\n +title: (.+?)\\n +year: (\\d+)");
      Matcher matcher = pattern.matcher(profileParameter);
      AlbumInfo albumsProfile = null;
      if (matcher.find()) {
        albumsProfile = new AlbumInfo();
        albumsProfile.setArtist(matcher.group(1));
        albumsProfile.setTitle(matcher.group(2));
        albumsProfile.setYear(matcher.group(3));
      } else {
        ErrorMsg errorMsg = new ErrorMsg();
        errorMsg.setMsg("Invalid post request parameter.");
        String jsonResponse = gson.toJson(errorMsg);
        res.getWriter().write(jsonResponse);
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      if (albumsProfile == null) {
        ErrorMsg errorMsg = new ErrorMsg();
        errorMsg.setMsg("Invalid album information");
        String jsonResponse = gson.toJson(errorMsg);

        res.getWriter().write(jsonResponse);
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      preparedStatement.setString(1, albumsProfile.getArtist());
      preparedStatement.setString(2, albumsProfile.getTitle());
      preparedStatement.setInt(3, Integer.valueOf(albumsProfile.getYear()));
      preparedStatement.setInt(4, imageSize);
      res.setStatus(HttpServletResponse.SC_OK);
      preparedStatement.executeUpdate();
      ResultSet resultKey = preparedStatement.getGeneratedKeys();
      int imageId = -1;
      if(resultKey.next()) {
        imageId = resultKey.getInt(1);
      } else {
        throw new SQLException("Unable to retrieve auto-generated key.");
      }

      String imageResult = gson.toJson(new ImageMetaData().albumID(String.valueOf(imageId)).imageSize(String.valueOf(imageSize)));
      res.getWriter().write(imageResult);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
