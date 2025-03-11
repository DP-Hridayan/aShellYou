package in.hridayan.ashell.shell.wifiadb;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class AdbPairingNotificationWorker extends Worker {

  public AdbPairingNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
    super(context, params);
  }

  @NonNull
  @Override
  public Result doWork() {
    Context context = getApplicationContext();
    String pairingCode = getInputData().getString("pairingCode");

    Intent serviceIntent = new Intent(context, AdbPairingNotification.class);
    serviceIntent.putExtra("pairingCode", pairingCode);

    context.startForegroundService(serviceIntent);

    return Result.success();
  }
}
