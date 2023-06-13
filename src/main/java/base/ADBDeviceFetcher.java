package base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ADBDeviceFetcher {
    public static List<String> devices = new ArrayList<>();
    public static List<String> androidVersions = new ArrayList<>();
    public static List<String> brandNames = new ArrayList<>();
    public static List<String> modelNames = new ArrayList<>();

//     public static void main(String[] args) throws IOException {
//    	 fetchAdbDeviceProperties();
//     }

    public static void fetchAdbDeviceProperties() throws IOException {
        String adbPath = "/usr/local/bin/adb";
        // Fetch connected devices
        Process devicesProcess = Runtime.getRuntime().exec(adbPath + " devices");
        BufferedReader devicesReader = new BufferedReader(new InputStreamReader(devicesProcess.getInputStream()));
        String devicesLine;
        while ((devicesLine = devicesReader.readLine()) != null) {
            if (!devicesLine.contains("List of devices attached")) {
                String[] deviceInfo = devicesLine.split("\\s+");
                String deviceName = deviceInfo[0];
                devices.add(deviceName);
            }
        }

        // Fetch properties for each connected device
        for (String device : devices) {
            String androidVersion = getProperty(device, "ro.build.version.release");
            androidVersions.add(androidVersion != null ? androidVersion : "N/A");

            String brandName = getProperty(device, "ro.product.brand");
            brandNames.add(brandName != null ? brandName : "N/A");

            String modelName = getProperty(device, "ro.product.model");
            modelNames.add(modelName != null ? modelName : "N/A");
        }

        // Print connected devices and their properties
        System.out.println("Connected devices and their properties:");
        System.out.println("---------------------------------------");
        System.out.printf("%-45s | %-20s | %-30s | %-20s%n", "Device", "Brand", "Model", "Version");
        System.out.println("---------------------------------------");
        for (int i = 0; i < devices.size(); i++) {
            String device = i < devices.size() ? devices.get(i) : "N/A";
            String brand = i < brandNames.size() ? brandNames.get(i) : "N/A";
            String model = i < modelNames.size() ? modelNames.get(i) : "N/A";
            String version = i < androidVersions.size() ? androidVersions.get(i) : "N/A";
            System.out.printf("%-45s | %-20s | %-30s | %-20s%n", device, brand, model, version);
        }
        System.out.println("---------------------------------------");
        System.out.println("Count of devices: " + (devices.size() - 1));
        System.out.println("List of devices: " + devices);
    }

    // Helper method to get device property using adb shell command
    public static String getProperty(String device, String property) throws IOException {
        String adbPath = "/usr/local/bin/adb";
        Process process = Runtime.getRuntime().exec(adbPath + " -s " + device + " shell getprop " + property);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();
        return line;
    }

}
