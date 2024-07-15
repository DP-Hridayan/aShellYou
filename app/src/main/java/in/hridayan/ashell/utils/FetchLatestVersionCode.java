package in.hridayan.ashell.utils;

import android.content.Context;
import android.os.AsyncTask;
import in.hridayan.ashell.utils.Utils.FetchLatestVersionCodeCallback;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchLatestVersionCode extends AsyncTask<String, Void, Integer> {
  private Context context;
  private FetchLatestVersionCodeCallback callback;
  public FetchLatestVersionCode(Context context, FetchLatestVersionCodeCallback callback) {
    this.context = context;
    this.callback = callback;
  }

  @Override
  protected Integer doInBackground(String... params) {
    StringBuilder result = new StringBuilder();
    try {
      URL url = new URL(params[0]);
      HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
      try {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
          result.append(line).append("\n");
        }
      } finally {
        urlConnection.disconnect();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return Preferences.CONNECTION_ERROR; // Error occurred
    }

    int latestVersionCode = Utils.extractVersionCode(result.toString());
    if (Utils.isUpdateAvailable(latestVersionCode)) {
      return Preferences.UPDATE_AVAILABLE; // Update available
    } else {
      return Preferences.UPDATE_NOT_AVAILABLE; // No update available
    }
  }

  @Override
  protected void onPostExecute(Integer result) {
    if (callback != null) {
      callback.onResult(result);
    }
  }
}
