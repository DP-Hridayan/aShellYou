package in.hridayan.ashell.utils.app.updater;

import android.util.Log;
import in.hridayan.ashell.config.Const;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReleaseFetcher {
  private static final String GITHUB_API_URL =
      "https://api.github.com/repos/"
          + Const.GITHUB_OWNER
          + "/"
          + Const.GITHUB_REPOSITORY
          + "/releases/latest";

  public interface ReleaseCallback {
    void onReleaseFound(String apkUrl, String apkFileName);

    void onError(String errorMessage);
  }

  public static void fetchLatestRelease(ReleaseCallback callback) {
    new Thread(
            () -> {
              try {
                HttpURLConnection connection =
                    (HttpURLConnection) new URL(GITHUB_API_URL).openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                StringBuilder response = new StringBuilder();
                try (InputStream inputStream = connection.getInputStream()) {
                  byte[] buffer = new byte[1024];
                  int bytesRead;
                  while ((bytesRead = inputStream.read(buffer)) != -1) {
                    response.append(new String(buffer, 0, bytesRead));
                  }
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray assets = jsonResponse.getJSONArray("assets");

                String apkUrl = null, apkFileName = null;
                for (int i = 0; i < assets.length(); i++) {
                  JSONObject asset = assets.getJSONObject(i);
                  if (asset.getString("name").endsWith(".apk")) {
                    apkFileName = asset.getString("name");
                    apkUrl = asset.getString("browser_download_url");
                    break;
                  }
                }

                if (apkUrl == null) throw new Exception("No APK file found.");
                callback.onReleaseFound(apkUrl, apkFileName);

              } catch (Exception e) {
                Log.e("ReleaseFetcher", "Error fetching update", e);
                callback.onError("Failed: " + e.getMessage());
              }
            })
        .start();
  }
}
