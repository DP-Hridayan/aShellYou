package in.hridayan.ashell.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import in.hridayan.ashell.R;
import in.hridayan.ashell.ui.BehaviorFAB.FabExtendingOnScrollViewListener;
import in.hridayan.ashell.ui.ThemeUtils;
import in.hridayan.ashell.databinding.ActivityCrashReportBinding;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.utils.DeviceUtils;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Utils;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrashReportActivity extends AppCompatActivity {
  private ActivityCrashReportBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);
    ThemeUtils.updateTheme(this);

    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_crash_report);

    new FabExtendingOnScrollViewListener(binding.scrollView, binding.reportButton);

    // Get the crash report from intent or SharedPreferences
    String stackTrace = getIntent().getStringExtra("stackTrace");
    String message = getIntent().getStringExtra("message");

    binding.crashLogTextView.setText(stackTrace + "\n\n" + message);

    binding.reportButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          sendCrashReport(stackTrace, message);
        });

    binding.copyText.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          Utils.copyToClipboard(reportContent(stackTrace, message), this);
        });

    binding.copyIcon.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          Utils.copyToClipboard(reportContent(stackTrace, message), this);
        });

    binding.shareButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          Utils.shareOutput(this, this, "crash_report.txt", reportContent(stackTrace, message));
        });
  }

  /*This function takes the report text and initiate the intent to send the email with the subject and body*/
  private void sendCrashReport(String stackTrace, String message) {
    String subject = "Crash Report";
    String to = Const.DEV_EMAIL;

    try {
      String uriText =
          "mailto:" + to + "?subject=" + subject + "&body=" + reportContent(stackTrace, message);

      Uri uri = Uri.parse(uriText);
      Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);
      startActivity(Intent.createChooser(emailIntent, "Send email using..."));
    } catch (Exception e) {
      Toast.makeText(
              CrashReportActivity.this, "Failed to encode email content.", Toast.LENGTH_SHORT)
          .show();
    }
  }

  /*Text to be shown in the report. We fetch the user device details such as Android version , manufacturer , app version etc. for debugging.*/
  private static String reportContent(String stackTrace, String message) {
    String deviceDetails = DeviceUtils.getDeviceDetails();
    String reportContent =
        getCurrentDateTime()
            + "\n"
            + deviceDetails
            + "\n\nMessage:\n"
            + message
            + "\n\nStack Trace:\n"
            + stackTrace;

    return reportContent;
  }

  private static String getCurrentDateTime() {
    // Thread-safe date format
    ThreadLocal<SimpleDateFormat> threadLocalDateFormat =
        new ThreadLocal<SimpleDateFormat>() {
          @Override
          protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd MMMM : HH:mm:ss [z]");
          }
        };

    // Get the current date and time
    Date now = new Date();

    // Format the date and time using the thread-local formatter
    return threadLocalDateFormat.get().format(now);
  }
}
