package in.hridayan.ashell.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import in.hridayan.ashell.activities.CrashReportActivity;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
  private final Context context;

  public CrashHandler(Context context) {
    this.context = context;
  }

  @Override
  public void uncaughtException(Thread thread, Throwable throwable) {
    String stackTrace = Log.getStackTraceString(throwable);
    Intent intent = new Intent(context, CrashReportActivity.class);
    intent.putExtra("stackTrace", stackTrace);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    context.startActivity(intent);

    android.os.Process.killProcess(android.os.Process.myPid());
    System.exit(1);
  }

  private void showCrashDialog(String stackTrace, String message) {
    new Thread(
            () -> {
              Looper.prepare();
              Intent intent = new Intent(context, CrashReportActivity.class);
              intent.putExtra("stackTrace", stackTrace);
              intent.putExtra("message", message);
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              context.startActivity(intent);
              Looper.loop();
            })
        .start();
  }
}
