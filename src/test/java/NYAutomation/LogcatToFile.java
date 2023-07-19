package NYAutomation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import base.ADBDeviceFetcher;
import static base.ADBDeviceFetcher.devices;
import static base.BaseClass.DriverDeviceIndex;
import static base.BaseClass.UserDeviceIndex;
import static base.BaseClass.version;
import static base.BaseClass.appPath;
import static base.BaseClass.userApkName;
import static base.BaseClass.driverApkName;

import io.qameta.allure.Allure;

public class LogcatToFile {

    private static String LogcatFileLocation;
    private static String automationStartedTime;

    /**
     * Captures logs from all devices.
     * @throws IOException if an I/O error occurs.
     */
    public static void CaptureLogs() throws IOException {
        fetchCurrentTime();
        for (String deviceSerialNumber : ADBDeviceFetcher.devices) {
            Thread logcatThread = new Thread(() -> {
                try {
                    String adbPath = "/Users/" + System.getProperty("user.name") + "/Library/Android/sdk/platform-tools/adb";
                    LogcatFileLocation = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "resources" + File.separator 
                                    + "logFile_" + deviceSerialNumber + ".txt";
                    StartFetchingLogs(adbPath, deviceSerialNumber, LogcatFileLocation);
                } catch (IOException | InterruptedException e) {
                    // Handle any exceptions
                }
            });
            logcatThread.start();
        }
    }

    /**
     * Starts fetching logs from the device using the specified ADB path.
     * @param adbPath the path to the ADB executable.
     * @param deviceSerialNumber the serial number of the device.
     * @param logcatFileLocation the location to store the logcat file.
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the thread is interrupted.
     */
    public static void StartFetchingLogs(String adbPath, String deviceSerialNumber, String logcatFileLocation) throws IOException, InterruptedException {
        Path file = Path.of(logcatFileLocation);
        if (Files.exists(file)) {
            // Delete the logcat file
            Files.delete(file);
            System.out.println("Cleared content from logFile_" + deviceSerialNumber + ".txt");
        } else {
            // Create the logcat file
            Files.createFile(file);
            System.out.println("Created Log file named as logFile_" + deviceSerialNumber + ".txt");
        }

        ProcessBuilder processBuilder = new ProcessBuilder(adbPath, "-s", deviceSerialNumber, "logcat");

        // Redirect the logcat output to a file
        processBuilder.redirectOutput(new File(logcatFileLocation));

        // Start the process
        Process process = processBuilder.start();

        // Wait for the process to finish
        process.waitFor();
    }

    /**
     * Fetches the current time and stores it in the automationStartedTime variable.
     */
    private static void fetchCurrentTime() {
        LocalTime currentDateTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        automationStartedTime = currentDateTime.format(formatter);
    }
    
    /**
     * Compares the log timestamp with the automationStartedTime.
     * @param logTimeString the log timestamp string.
     * @return true if the log timestamp is after or equal to automationStartedTime, false otherwise.
     */
    private static Boolean compareTime(String logTimeString) {
        String timestampString = logTimeString.substring(6, 18);
        LocalTime fetchedLogtime = LocalTime.parse(timestampString, DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        // Parse the timestamp from the log entry
        return !fetchedLogtime.isBefore(LocalTime.parse(automationStartedTime, formatter));
    }
    
    /**
     * Searches for an error code in the log file.
     * @param isUser true if searching in the user's log file, false for the driver's log file.
     * @return true if no error is found or the error is not within the specified time range, false otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public static void searchApiErr(boolean isUser) throws IOException {
        String formattedText = null;
        String deviceSerialNumber;
        String line = null;
        String errTimeStampString = null;
        
        if (isUser) {
            deviceSerialNumber = devices.get(UserDeviceIndex);
        } else {
            deviceSerialNumber = devices.get(DriverDeviceIndex);
        }

        String logcatFile1 = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "resources" + File.separator 
                                + "logFile_" + deviceSerialNumber + ".txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(logcatFile1))) {
            // String previousLine = null;
            while ((line = reader.readLine()) != null) {
                if (line.contains("network_call") && line.contains("errorMessage")) {
                    errTimeStampString = line;
                    int startIndex = line.indexOf("{");
                    String jsonPart = line.substring(startIndex);
                    JSONObject logObject = new JSONObject(jsonPart);
                    int statusCode = logObject.getJSONObject("value").getInt("status_code");
                    String errorMessage = logObject.getJSONObject("value").getString("response");
                    JSONObject responseObject = new JSONObject(errorMessage);
                    String actualErrorMessage = responseObject.getString("errorCode");
                    String url = logObject.getJSONObject("value").getString("url");
                    formattedText = String.format("Status Code:   %d\nError Message: %-10s\nURL:      %20s", statusCode, actualErrorMessage, url);
                }
            }
        } catch (IOException e) {
            // Handle any exceptions
        }

        if ((formattedText != null) && compareTime(errTimeStampString)){
            if (isUser) {
                System.out.println("\nUser Api Error:" + formattedText);
                Allure.addAttachment("User Api Error ", formattedText);
            } else {
                System.out.println("\nDriver Api Error:" + formattedText);
                Allure.addAttachment("Driver Api Error ", formattedText);
            }
        }
    }

    public static void searchJavaScriptError(boolean isUser) {
        String JavaScriptError = "TypeError:";
        String errorString = null;
        String deviceSerialNumber;

        if (isUser) {deviceSerialNumber = devices.get(UserDeviceIndex);}
        else {deviceSerialNumber = devices.get(DriverDeviceIndex);}
        String readLogCatFile = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "resources" + File.separator 
                                + "logFile_" + deviceSerialNumber + ".txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(readLogCatFile))) {
            String line;
            String previousLine = null;
            while ((line = reader.readLine()) != null) {
                if (line.contains(JavaScriptError) && line.contains("chromium")) {
                    errorString = previousLine + "\n" + line;
                }
                previousLine = line;
            }
        } catch (IOException e) {}
        if ((errorString != null) && compareTime(errorString)){
            if (isUser) {
                Allure.addAttachment("User JavaScript Error ", errorString);
            } else {
                Allure.addAttachment("Driver JavaScript Error ", errorString);
            }
        }
    }

    public static void fetchAppDetails(boolean isUser) {
        String deviceSerialNumber;
        String apkName = null;
        String bundleVersion = null;
        
        for (int i = 0; i < devices.size(); i++) {
            deviceSerialNumber = devices.get(i);
            String readLogCatFile = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "resources" + File.separator
                + "logFile_" + deviceSerialNumber + ".txt";
            boolean bundleVersionFound = false;
            String appDetails = "";
          
            try (BufferedReader reader = new BufferedReader(new FileReader(readLogCatFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(" D SdkTracker: ")) {
                        // Extract the JSON string from the log message.
                        String jsonString = extractJSONStringFromLogLine(line);
                        if (jsonString != null) {
                            // Parse the JSON string into a JSONObject.
                            JSONObject jsonObject = new JSONObject(jsonString);
                            // Extract the "bundle_version" from the JSONObject.
                            bundleVersion = jsonObject.optString("bundle_version");
                            // Check if "bundle_version" has a value.
                            if (!bundleVersion.isEmpty()) {
                                // Set the flag to true to stop looping
                                bundleVersionFound = true;
                                break;
                            }
                        } else {
                            System.out.println("JSON object not found in the log line.");
                        }
                    }
                }

                if (i == UserDeviceIndex) {
                    apkName = userApkName;
                    String fetchVersion = appPath + apkName;
                    
                    Map<String, String> versions = fetchAndroidAppVersions(fetchVersion);
                    String appVersion = "App Version='";
                    String versionCheck = versions.get(fetchVersion);
                    version = appVersion + versionCheck;
                    System.out.println("APK Name -> " + apkName);
                    System.out.println("Version Details" + version);

                    // If the "bundle_version" is not found after looping through all the lines
                    if (!bundleVersionFound) {
                        System.out.println("Bundle Version not found for " + apkName);
                    }
                    appDetails = "APK name : " + apkName + "\nVersion : " + version;
                    if (bundleVersion != null) {
                        appDetails += "\nBundle Version : " + bundleVersion;
                    }
                } else if (i == DriverDeviceIndex) {
                    apkName = driverApkName;
                    String fetchVersion = appPath + apkName;
                    Map<String, String> versions = fetchAndroidAppVersions(fetchVersion);
                    String appVersion = "App Version='";
                    String versionCheck = versions.get(fetchVersion);
                    version = appVersion + versionCheck;
                    System.out.println("APK Name -> " + apkName);
                    System.out.println("Version Details" + version);

                    appDetails = "APK name : " + apkName + "\nVersion : " + version;
                    if (bundleVersion != null) {
                        appDetails += "\nBundle Version : " + bundleVersion;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("App Details:\n" + appDetails);

            // Add app details as an attachment to Allure report.
            if (i == UserDeviceIndex) {
            	Allure.addAttachment("User App Version Details", appDetails);
            }
            else if(i == DriverDeviceIndex) {
            	Allure.addAttachment("Driver App Version Details", appDetails);
            }
        }
    }

    private static String extractJSONStringFromLogLine(String logLine) {
        int jsonStartIndex = logLine.indexOf("{");
        int jsonEndIndex = logLine.lastIndexOf("}");

        if (jsonStartIndex >= 0 && jsonEndIndex > jsonStartIndex) {
            return logLine.substring(jsonStartIndex, jsonEndIndex + 1);
        }

        return null;
    }
    
    public static  Map<String, String> fetchAndroidAppVersions(String appFilePath) {
        Map<String, String> versions = new HashMap<>();
        
            try {
                String aaptPath = "/Users/dakshana.j/Library/Android/sdk/build-tools/32.1.0-rc1/aapt";
                // Execute the ‘aapt’ command to extract the version information
                Process process = Runtime.getRuntime().exec(aaptPath + " dump badging " + appFilePath + " | grep Version");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                // Read the output of the command
                String line;
                while ((line = reader.readLine()) != null) {
                    // Extract the version information using regular expressions
                    Pattern pattern = Pattern.compile("versionName='([^‘]+)'");
                    Matcher matcher = pattern.matcher(line);

                    if (matcher.find()) {
                        versions.put(appFilePath, matcher.group(1));
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        return versions;
    }

}