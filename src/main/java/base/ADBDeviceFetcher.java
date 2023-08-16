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
    public static List<String> resolutions = new ArrayList<>();

    // public static void main(String[] args) throws IOException {
   	//  fetchAdbDeviceProperties();
    // }

    public static void fetchAdbDeviceProperties() throws IOException {
        String adbPath = "/home/" + System.getProperty("user.name") + "/Android/Sdk/platform-tools/adb";
        // Fetch connected devices
        Process devicesProcess = Runtime.getRuntime().exec(adbPath + " devices");
        BufferedReader devicesReader = new BufferedReader(new InputStreamReader(devicesProcess.getInputStream()));
        String devicesLine;
        while ((devicesLine = devicesReader.readLine()) != null) {
            if (!devicesLine.contains("List of devices attached")) {
                String[] deviceInfo = devicesLine.split("\\s+");
                String deviceName = deviceInfo[0];
                if(deviceName.length() > 2){devices.add(deviceName);}
            }
        }

        // Fetch properties for each connected device
        for (String device : devices) {
            String androidVersion = getProperty(device, "getprop ro.build.version.release");
            String firstVersion = (androidVersion.split("\\.")[0]);
            if(androidVersion != null){androidVersions.add(firstVersion);}
            
            String brandName = getProperty(device, "getprop ro.product.brand");
            if(brandName != null){brandNames.add(brandName);}
            
            String modelName = getProperty(device, "getprop ro.product.model");
            if(modelName != null){modelNames.add(modelName);}

            String resolution = getProperty(device, "wm size");
            if((resolution != null) && (resolution.contains("Physical size:"))) {
                String resolution1 = resolution.replace("Physical size: ", "").trim();
                resolutions.add(resolution1);
            }
        }

        // Print connected devices and their properties
        System.out.println("Connected devices and their properties:");
        System.out.println("---------------------------------------");
        System.out.printf("%-40s | %-15s | %-30s | %-15s | %-15s%n", "Device", "Brand", "Model", "Version", "Resolution");
        System.out.println("---------------------------------------");
        for (int i = 0; i < (devices.size()); i++) {
            String device = i < devices.size() ? devices.get(i) : "N/A";
            String brand = i < brandNames.size() ? brandNames.get(i) : "N/A";
            String model = i < modelNames.size() ? modelNames.get(i) : "N/A";
            String version = i < androidVersions.size() ? androidVersions.get(i) : "N/A";
            String resolution = i < resolutions.size() ? resolutions.get(i) : "N/A";
            System.out.printf("%-40s | %-15s | %-30s | %-15s | %-15s%n", device, brand, model, version, resolution);
        }
        System.out.println("---------------------------------------");
        System.out.println("Count of devices: " + devices.size());
        System.out.println("List of devices: " + devices);
    }

    // Helper method to get device property using adb shell command
    public static String getProperty(String device, String property) throws IOException {
        String adbPath = "/home/" + System.getProperty("user.name") + "/Android/Sdk/platform-tools/adb";
        Process process = Runtime.getRuntime().exec(adbPath + " -s " + device + " shell " + property);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();
        return line;
    }

}
