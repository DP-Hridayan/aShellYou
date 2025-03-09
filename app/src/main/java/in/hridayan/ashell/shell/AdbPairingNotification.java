package in.hridayan.ashell.shell;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.R;

public class AdbPairingNotification extends Service {

  private static final String CHANNEL_ID = "adb_pairing_channel";
  private static final int PAIRING_NOTIFICATION_ID = 101;
  private static final String PAIRING_CODE = "pairing_code";
  private static final String ACTION_SUBMIT_CODE = "action_submit_code";
  private static final String ACTION_STOP_SERVICE = "action_stop_service";

  @Override
  public void onCreate() {
    super.onCreate();
    createNotificationChannel();
    requestPairingCodeInput();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    createNotificationChannel();
    requestPairingCodeInput();

    if (intent != null) {
      String action = intent.getAction();

      if (ACTION_STOP_SERVICE.equals(action)) {
        stopNotification();
        stopSelf();
        return START_NOT_STICKY;
      }

      if (!isNotificationActive()) {
        requestPairingCodeInput();
      }

      handleUserInput(intent);
    }
    return START_STICKY;
  }

  private void handleUserInput(Intent intent) {
    String action = intent.getAction();
    Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
    if (remoteInput == null) return;

    if (ACTION_SUBMIT_CODE.equals(action)) {
      String code = remoteInput.getString(PAIRING_CODE, "");
      if (!code.isEmpty()) {
        storeAdbCredentials(code);
      } else {
        requestPairingCodeInput();
      }
    }
  }

  private void requestPairingCodeInput() {
    sendNotification(
        getString(R.string.enter_pairing_code),
        getString(R.string.enter_pairing_code),
        PAIRING_CODE,
        ACTION_SUBMIT_CODE);
  }

  private void storeAdbCredentials(String code) {
    Preferences.setAdbPairingCode(code);

    String ip = Preferences.getAdbIp();
    String port = Preferences.getAdbPairingPort();

    WifiAdbShell.pair(
        this,
        ip,
        port,
        code,
        new WifiAdbShell.PairingCallback() {
          @Override
          public void onSuccess() {
            connectAdb();
          }

          @Override
          public void onFailure(String errorMessage) {
            showFinalNotification(getString(R.string.failed), ": " + errorMessage);
          }
        });

    stopNotification();
    stopSelf();
  }

  private void connectAdb() {
    String ip = Preferences.getAdbIp();
    String port = Preferences.getAdbConnectingPort();
    WifiAdbShell.connect(
        this,
        ip,
        port,
        new WifiAdbShell.ConnectingCallback() {
          @Override
          public void onSuccess() {
            showFinalNotification(getString(R.string.success), getString(R.string.connected));
          }

          @Override
          public void onFailure(String errorMessage) {
            showFinalNotification(getString(R.string.failed), ": " + errorMessage);
          }
        });
  }

  private void sendNotification(String title, String label, String key, String action) {
    cancelNotification(PAIRING_NOTIFICATION_ID);

    RemoteInput remoteInput = new RemoteInput.Builder(key).setLabel(label).build();

    PendingIntent submitIntent =
        PendingIntent.getService(
            this,
            0,
            new Intent(this, AdbPairingNotification.class).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

    PendingIntent stopServiceIntent =
        PendingIntent.getService(
            this,
            0,
            new Intent(this, AdbPairingNotification.class).setAction(ACTION_STOP_SERVICE),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

    NotificationCompat.Action inputAction =
        new NotificationCompat.Action.Builder(
                R.drawable.ic_adb, getString(R.string.submit), submitIntent)
            .addRemoteInput(remoteInput)
            .build();

    NotificationCompat.Action stopAction =
        new NotificationCompat.Action.Builder(
                R.drawable.ic_stop, getString(R.string.dismiss), stopServiceIntent)
            .build();

    Notification notification =
        new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_adb)
            .setContentTitle(title)
            .setContentText(getString(R.string.enter_six_digit_pairing_code))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
            .addAction(inputAction)
            .addAction(stopAction)
            .setOngoing(true)
            .build();

    startForeground(PAIRING_NOTIFICATION_ID, notification);
  }

  private void showFinalNotification(String title, String message) {
    Notification notification =
        new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_adb)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(false)
            .build();

    // Update the foreground notification
    NotificationManagerCompat manager = NotificationManagerCompat.from(this);
    manager.notify(PAIRING_NOTIFICATION_ID, notification);

    // Stop foreground mode, but keep the notification
    stopForeground(Service.STOP_FOREGROUND_DETACH);
  }

  private void stopNotification() {
    NotificationManagerCompat manager = NotificationManagerCompat.from(this);
    manager.cancel(PAIRING_NOTIFICATION_ID);
    stopForeground(Service.STOP_FOREGROUND_REMOVE);
  }

  private void cancelNotification(int notificationID) {
    NotificationManagerCompat manager = NotificationManagerCompat.from(this);
    manager.cancel(notificationID);
  }

  private boolean isNotificationActive() {
    NotificationManager manager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      for (StatusBarNotification notification : manager.getActiveNotifications()) {
        if (notification.getId() == PAIRING_NOTIFICATION_ID) {
          return true; // Notification is still active
        }
      }
    }
    return false; // Notification was dismissed/swiped away
  }

  private void createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel =
          new NotificationChannel(
              CHANNEL_ID, "ADB Pairing Channel", NotificationManager.IMPORTANCE_MAX);
      channel.setDescription("Channel for ADB Pairing notifications");
      channel.enableVibration(true);
      channel.enableLights(true);
      channel.setSound(null, null);
      channel.setShowBadge(false);
      channel.setAllowBubbles(false);

      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
