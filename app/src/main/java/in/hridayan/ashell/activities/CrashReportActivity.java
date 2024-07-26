package in.hridayan.ashell.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.ThemeUtils;
import in.hridayan.ashell.utils.Utils;

public class CrashReportActivity extends AppCompatActivity {
  private MaterialTextView copyText, crashInfo;
  private AppCompatImageButton copyButton;
  private ExtendedFloatingActionButton reportButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);
    ThemeUtils.updateTheme(this);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_crash_report);

    copyText = findViewById(R.id.copy);
    copyButton = findViewById(R.id.copy_button);
    reportButton = findViewById(R.id.report_button);

    // Get the crash report from intent or SharedPreferences
    String stackTrace = getIntent().getStringExtra("stackTrace");
    String message = getIntent().getStringExtra("message");

    // Show the crash info
    crashInfo = findViewById(R.id.crash_info);
    crashInfo.setText(stackTrace + "\n\n" + message);
    reportButton.setOnClickListener(v -> sendCrashReport(stackTrace, message));
    copyText.setOnClickListener(
        v -> {
          copyReportToClipboard(stackTrace, message);
        });
    copyButton.setOnClickListener(
        v -> {
          copyReportToClipboard(stackTrace , message);
        });
  }

  private void sendCrashReport(String stackTrace, String message) {
    String deviceDetails = Utils.getDeviceDetails();
    String reportContent =
        "Device Details:\n"
            + deviceDetails
            + "\n\nMessage:\n"
            + message
            + "\n\nStack Trace:\n"
            + stackTrace;

    String subject = "Crash Report";
    String to = Preferences.devEmail;

    try {
      String uriText = "mailto:" + to + "?subject=" + subject + "&body=" + reportContent;

      Uri uri = Uri.parse(uriText);
      Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);
      startActivity(Intent.createChooser(emailIntent, "Send email using..."));
    } catch (Exception e) {
      Toast.makeText(
              CrashReportActivity.this, "Failed to encode email content.", Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void copyReportToClipboard(String stackTrace , String message) {
    String deviceDetails = Utils.getDeviceDetails();
    String reportContent =
        "Device Details:\n"
            + deviceDetails
            + "\n\nMessage:\n"
            + message
            + "\n\nStack Trace:\n"
            + stackTrace;
    Utils.copyToClipboard(reportContent, this);
  }
}
