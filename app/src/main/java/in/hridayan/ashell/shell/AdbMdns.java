package in.hridayan.ashell.shell;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

@RequiresApi(Build.VERSION_CODES.R)
public class AdbMdns {

  private static final String TAG = "AdbMdns";
  public static final String TLS_CONNECT = "_adb-tls-connect._tcp";
  public static final String TLS_PAIRING = "_adb-tls-pairing._tcp";

  private final Set<String> resolvedServices = new HashSet<>();

  private final NsdManager nsdManager;
  private boolean running = false;
  private final AdbFoundCallback callback;
  private boolean stopResolving = false;
  private final Handler handler = new Handler(Looper.getMainLooper());

  private final NsdManager.DiscoveryListener pairingListener;
  private final NsdManager.DiscoveryListener connectListener;

  public interface AdbFoundCallback {
    void onPairingCodeDetected(String ipAddress, int port);

    void onConnectCodeDetected(String ipAddress, int port);
  }

  public AdbMdns(Context context, AdbFoundCallback callback) {
    this.nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    this.callback = callback;

    pairingListener = new DiscoveryListener(TLS_PAIRING);
    connectListener = new DiscoveryListener(TLS_CONNECT);
  }

  public void start() {
    if (running) return;
    running = true;
    try {
      nsdManager.discoverServices(TLS_PAIRING, NsdManager.PROTOCOL_DNS_SD, pairingListener);
      nsdManager.discoverServices(TLS_CONNECT, NsdManager.PROTOCOL_DNS_SD, connectListener);
    } catch (Exception e) {
      Log.e(TAG, "Error starting service discovery: ", e);
      running = false;
    }
  }

  public void stop() {
    if (!running) return;
    running = false;

    try {
      nsdManager.stopServiceDiscovery(pairingListener);
      nsdManager.stopServiceDiscovery(connectListener);
    } catch (Exception e) {
      Log.e(TAG, "Error stopping service discovery: ", e);
    }

    resolvedServices.clear();
    handler.removeCallbacksAndMessages(null);
    stopResolving = false;
  }

  private boolean isMatchingNetwork(NsdServiceInfo resolvedService) {
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      for (NetworkInterface networkInterface : Collections.list(interfaces)) {
        if (networkInterface.isUp()) {
          for (java.net.InetAddress inetAddress :
              Collections.list(networkInterface.getInetAddresses())) {
            if (resolvedService.getHost().getHostAddress().equals(inetAddress.getHostAddress())) {
              return true;
            }
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private boolean isPortAvailable(int port) {
    try (ServerSocket socket = new ServerSocket()) {
      socket.bind(new InetSocketAddress("127.0.0.1", port), 1);
      return false;
    } catch (IOException e) {
      return true;
    }
  }

  private class DiscoveryListener implements NsdManager.DiscoveryListener {
    private final String serviceType;

    DiscoveryListener(String serviceType) {
      this.serviceType = serviceType;
    }

    @Override
    public void onDiscoveryStarted(String serviceType) {
      Log.v(TAG, "Discovery started: " + serviceType);
    }

    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
      Log.v(TAG, "Start discovery failed: " + serviceType + ", " + errorCode);
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
      Log.v(TAG, "Discovery stopped: " + serviceType);
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
      Log.v(TAG, "Stop discovery failed: " + serviceType + ", " + errorCode);
    }

    @Override
    public void onServiceFound(NsdServiceInfo info) {
      if (!running) return;

      String serviceKey = info.getHost() + ":" + info.getPort();
      if (!resolvedServices.contains(serviceKey)) {
        resolvedServices.add(serviceKey);
        nsdManager.resolveService(info, new ResolveListener(serviceType));

        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(() -> stopResolving = true, 3 * 60 * 1000);
      } else if (!stopResolving) {
        nsdManager.resolveService(info, new ResolveListener(serviceType));
      }
    }

    @Override
    public void onServiceLost(NsdServiceInfo info) {
      if (info == null || info.getHost() == null) return;
      String serviceKey = info.getHost().getHostAddress() + ":" + info.getPort();
      resolvedServices.remove(serviceKey);
      Log.d(TAG, "Service lost: " + serviceKey);
    }
  }

  private class ResolveListener implements NsdManager.ResolveListener {
    private final String serviceType;

    ResolveListener(String serviceType) {
      this.serviceType = serviceType;
    }

    @Override
    public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int errorCode) {
      Log.v(
          TAG, "Resolve failed: " + nsdServiceInfo.getServiceName() + ", errorCode: " + errorCode);
    }

    @Override
    public void onServiceResolved(NsdServiceInfo resolvedService) {
      if (running
          && isMatchingNetwork(resolvedService)
          && isPortAvailable(resolvedService.getPort())) {
        String ipAddress = resolvedService.getHost().getHostAddress();
        int portNumber = resolvedService.getPort();

        if (serviceType.equals(TLS_PAIRING) && callback != null) {
          callback.onPairingCodeDetected(ipAddress, portNumber);
        } else if (serviceType.equals(TLS_CONNECT) && callback != null) {
          callback.onConnectCodeDetected(ipAddress, portNumber);
        }
      }
    }
  }
}
