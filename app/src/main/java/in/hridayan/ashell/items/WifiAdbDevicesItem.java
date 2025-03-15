package in.hridayan.ashell.items;

public class WifiAdbDevicesItem {
   private final String ipPort;

    public WifiAdbDevicesItem(String ipPort) {
        this.ipPort = ipPort;
    }

    public String getIpPort() {
        return ipPort;
    }
}
