package in.hridayan.ashell.utils.app.updater;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.utils.DeviceUtils;
import in.hridayan.ashell.utils.DeviceUtils.FetchLatestVersionCodeCallback;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FetchLatestVersionCode {
  private final Context context;
  private final FetchLatestVersionCodeCallback callback;
  private final ExecutorService executor;
  private final Handler handler;

  public FetchLatestVersionCode(Context context, FetchLatestVersionCodeCallback callback) {
    this.context = context;
    this.callback = callback;
    this.executor = Executors.newSingleThreadExecutor();
    this.handler = new Handler(Looper.getMainLooper());
  }

  public void execute(String url) {
    executor.execute(
        () -> {
          int result = fetchVersionCode(url);
          handler.post(
              () -> {
                if (callback != null) {
                  callback.onResult(result);
                }
              });
        });
  }

  private int fetchVersionCode(String urlString) {
    StringBuilder result = new StringBuilder();
    try {
      URL url = new URL(urlString);
      HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          result.append(line).append("\n");
        }
      } finally {
        urlConnection.disconnect();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return Const.CONNECTION_ERROR; // Error occurred
    }

    int latestVersionCode = DeviceUtils.extractVersionCode(result.toString());
    if (DeviceUtils.isUpdateAvailable(latestVersionCode)) {
      Preferences.setLatestVersionName(DeviceUtils.extractVersionName(result.toString()));
      return Const.UPDATE_AVAILABLE; // Update available
    } else {
      return Const.UPDATE_NOT_AVAILABLE; // No update available
    }
  }
}
