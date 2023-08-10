package base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import static base.ADBDeviceFetcher.devices;
import static base.BaseClass.driverDeviceIndex;
import static base.BaseClass.userDeviceIndex;
import static base.BaseClass.version;
import static base.BaseClass.appPath;
import static base.BaseClass.userApkName;
import static base.BaseClass.driverApkName;

import io.qameta.allure.Allure;

public class LogcatToFile {

    private static String automationStartedDateTime;

    /**
     * Captures logs from all devices.
     * @throws IOException if an I/O error occurs.
     */
    public static void CaptureLogs() throws IOException {
        fetchCurrentTime();
        for (String deviceSerialNumber : ADBDeviceFetcher.devices) {
            Thread logcatThread = new Thread(() -> {
                try {
                    // Extracted method to fetch logs for each device
                    fetchLogsForDevice(deviceSerialNumber);
                } catch (IOException | InterruptedException e) {
                    // Handle any exceptions
                }
            });
            logcatThread.start();
        }
    }

    /**
     * Fetch logs for a specific device using ADB.
     * @param deviceSerialNumber the serial number of the device.
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the thread is interrupted.
     */
    private static void fetchLogsForDevice(String deviceSerialNumber) throws IOException, InterruptedException {
        String adbPath = "/Users/" + System.getProperty("user.name") + "/Library/Android/sdk/platform-tools/adb";
        String LogcatFileLocation = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "resources" + File.separator + "logFiles" + File.separator
                + "logFile_" + deviceSerialNumber + ".txt";

        // Delete or create the logcat file
        Path file = Path.of(LogcatFileLocation);
        if (Files.exists(file)) {
            Files.delete(file);
            System.out.println("Deleted content from logFile_" + deviceSerialNumber + ".txt");
        } else {
            Files.createFile(file);
            System.out.println("Created Log file named as logFile_" + deviceSerialNumber + ".txt");
        }
        ProcessBuilder processBuilder = new ProcessBuilder(adbPath, "-s", deviceSerialNumber, "logcat");
        processBuilder.redirectOutput(new File(LogcatFileLocation));
        Process process = processBuilder.start();
        process.waitFor();
    }

    /**
     * Fetches the current time and stores it in the automationStartedTime variable.
     */
    private static void fetchCurrentTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss.SSS");
        automationStartedDateTime = currentDateTime.format(formatter);        
    }
    
    /**
     * Compares the log timestamp with the automationStartedTime.
     * @param logTimeString the log timestamp string.
     * @return true if the log timestamp is after or equal to automationStartedTime, false otherwise.
     */
    private static Boolean compareTime(String logTimeString) {
        String timestampString = logTimeString.substring(0, 18); // Assuming the timestamp format is "MM-dd HH:mm:ss.SSS"
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        // Parse the date and time strings into Date objects
        try {
            Date logTimeDate = sdf.parse(timestampString);
            Date automationtimeDate = sdf.parse(automationStartedDateTime);
            return (logTimeDate.compareTo(automationtimeDate) > 0);
            } catch (ParseException e) {
                e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Searches for an error code in the log file.
     * @throws IOException if an I/O error occurs.
     */
    public static void searchApiErr() throws IOException {
        String deviceSerialNumber;
        String line = null;
        for(int i = 0; i < BaseClass.deviceIndex; i++){
            String apiFormattedText = "";
            deviceSerialNumber = devices.get(i);
            String readLogCatFile = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "resources" + File.separator + "LogFiles" + File.separator 
                                + "logFile_" + deviceSerialNumber + ".txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(readLogCatFile))) {
            // String previousLine = null;
            while ((line = reader.readLine()) != null) {
                if (line.contains("network_call") && line.contains("errorMessage")  && compareTime(line)) {
                    String timeOfErr = line.substring(0, 19);
                    int startIndex = line.indexOf("{");
                    String jsonPart = line.substring(startIndex);
                    JSONObject logObject = new JSONObject(jsonPart);
                    int statusCode = logObject.getJSONObject("value").getInt("status_code");
                    String errorMessage = logObject.getJSONObject("value").getString("response");
                    JSONObject responseObject = new JSONObject(errorMessage);
                    String actualErrorMessage = responseObject.getString("errorCode");
                    String url = logObject.getJSONObject("value").getString("url");
                    apiFormattedText = apiFormattedText + "\n\n-----------------------------------------------------------\n\n" 
                                    + String.format("Time of Error: %s\nStatus Code:   %d\nError Message: %s\nURL:	       %s",timeOfErr, statusCode, actualErrorMessage, url);
                }
            }
        } catch (IOException e) {
            // Handle any exceptions
        }
        if(apiFormattedText != ""){
            if(i == userDeviceIndex){
                Allure.addAttachment("User Api Error ", apiFormattedText);
            } else {
                Allure.addAttachment("Driver Api Error ", apiFormattedText);
            }
        }
    }
    }

    /**
     * Searches for JavaScript errors in the log file.
     */
    public static void searchJavaScriptError() {
        String typeError = "TypeError:";
        String uncaughtTypeError = "Uncaught TypeError:";
        String deviceSerialNumber;

        for(int i = 0; i < BaseClass.deviceIndex; i++){
            String jsErrorString = null;
            deviceSerialNumber = devices.get(i);
            String readLogCatFile = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "resources" + File.separator + "LogFiles" + File.separator 
                                + "logFile_" + deviceSerialNumber + ".txt";
            try (BufferedReader reader = new BufferedReader(new FileReader(readLogCatFile))) {
                String line;
                String previousLine = null;
                while ((line = reader.readLine()) != null) {
                    if ((line.contains(typeError) || line.contains(uncaughtTypeError)) && line.contains("chromium")) {
                        jsErrorString = previousLine + "\n" + line;
                    }
                    previousLine = line;
                }
            } catch (IOException e) {}
            if(jsErrorString != null){
                if(i == userDeviceIndex){
                    Allure.addAttachment("User JavaScript Error ", jsErrorString);
                } 
                else {
                    Allure.addAttachment("Driver JavaScript Error ", jsErrorString);
                }
            }
        }
    }

    /**
     * Fetches app details from the log file.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void fetchAppDetails() throws FileNotFoundException, IOException {
        String deviceSerialNumber;
        String apkName = null;
        String bundleVersion = null;
        
        for (int i = 0; i < devices.size(); i++) {
            deviceSerialNumber = devices.get(i);
            String readLogCatFile = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "resources" + File.separator + "LogFiles" + File.separator
                + "logFile_" + deviceSerialNumber + ".txt";
            boolean bundleVersionFound = false;
            String appDetails = "";
          
            try (BufferedReader reader = new BufferedReader(new FileReader(readLogCatFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(" D SdkTracker: ") && line.contains("bundle_version") && compareTime(line)) {
                        JSONObject jsonObject = new JSONObject(line.substring(line.indexOf("{")));
                        bundleVersion = jsonObject.getString("bundle_version");
                    }
                }

                if (i == userDeviceIndex) {
                    apkName = userApkName;
                    String fetchVersion = appPath + apkName;
                    
                    Map<String, String> versions = fetchAndroidAppVersions(fetchVersion);
                    String appVersion = "App Version='";
                    String versionCheck = versions.get(fetchVersion);
                    version = appVersion + versionCheck;
                    System.out.println("APK Name -> " + apkName);
                    System.out.println("Version Details " + version);

                    // If the "bundle_version" is not found after looping through all the lines
                    if (!bundleVersionFound) {
                        System.out.println("Bundle Version not found for " + apkName);
                    }
                    appDetails = "APK name : " + apkName + "\nVersion : " + version;
                    if (bundleVersion != null) {
                        appDetails += "\nBundle Version : " + bundleVersion;
                    }
                } else if (i == driverDeviceIndex) {
                    apkName = driverApkName;
                    String fetchVersion = appPath + apkName;
                    Map<String, String> versions = fetchAndroidAppVersions(fetchVersion);
                    String appVersion = "App Version='";
                    String versionCheck = versions.get(fetchVersion);
                    version = appVersion + versionCheck;
                    System.out.println("APK Name -> " + apkName);
                    System.out.println("Version Details " + version);

                    appDetails = "APK name : " + apkName + "\nVersion : " + version;
                    if (bundleVersion != null) {
                        appDetails += "\nBundle Version : " + bundleVersion;
                    }
                }
            }

            System.out.println("App Details:\n" + appDetails);

            // Add app details as an attachment to Allure report.
            if (i == userDeviceIndex) {
            	Allure.addAttachment("User App Version Details", appDetails);
            }
            else {
            	Allure.addAttachment("Driver App Version Details", appDetails);
            }
        }
    }
    
    /**
     * Fetches the Android app versions using the aapt tool.
     * @param appFilePath the path of the APK file.
     * @return a map containing the app versions.
     */
    public static Map<String, String> fetchAndroidAppVersions(String appFilePath) {
        Map<String, String> versions = new HashMap<>();
    
            try {
                String aaptPath = "/Users/" + System.getProperty("user.name") + "/Library/Android/sdk/build-tools/32.1.0-rc1/aapt";
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
    /**
     * Fetches exceptions from the log file.
     * @throws IOException if an I/O error occurs.
     */
    public static void fetchExceptions() throws IOException {
        String npException = "java.lang.NullPointerException";
        String ccException = "java.lang.ClassCastException";
        String arrIOBException = "java.lang.ArrayIndexOutOfBoundsException";
        String jsonException = "org.json.JSONException";
        String deviceSerialNumber;
        for(int i = 0; i < BaseClass.deviceIndex; i++){
            String exceptionString = "";
            deviceSerialNumber = devices.get(i);
            String readLogCatFile = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "resources" + File.separator + "LogFiles" + File.separator 
                                + "logFile_" + deviceSerialNumber + ".txt";
            try (BufferedReader reader = new BufferedReader(new FileReader(readLogCatFile))) {
                String line;
                while (((line = reader.readLine()) != null)) {
                    if (line.contains(npException) && compareTime(line)) {
                        int index = line.indexOf("java.lang.");
                        exceptionString = exceptionString + "\n\n-----------------------------------------------------------\n\n" + line.substring(index);
                        while(((line = reader.readLine()) != null) && line.contains("System.err")){
                            exceptionString = exceptionString + "\n" + line.substring(index);
                        }
                    }
                    else if(line.contains(ccException) && compareTime(line)){
                        int index = line.indexOf("java.lang.");
                        exceptionString = exceptionString + "\n\n-----------------------------------------------------------\n\n" + line.substring(index);
                        while(((line = reader.readLine()) != null) && line.contains("System.err")){
                            exceptionString = exceptionString + "\n" + line.substring(index);
                        }
                    }
                    else if(line.contains(arrIOBException) && compareTime(line)){
                        int index = line.indexOf("java.lang.");
                        exceptionString = exceptionString + "\n\n-----------------------------------------------------------\n\n" + line.substring(index);
                        while(((line = reader.readLine()) != null) && line.contains("System.err")){
                            exceptionString = exceptionString + "\n" + line.substring(index);
                        }
                    }
                    else if(line.contains(jsonException) && compareTime(line)){
                        int index = line.indexOf("org.json");
                        exceptionString = exceptionString + "\n\n-----------------------------------------------------------\n\n" + line.substring(index);
                        while(((line = reader.readLine()) != null) && line.contains("JBridge")){
                            exceptionString = exceptionString + "\n" + line.substring(index);
                        }
                    }
                }
            }
            if(exceptionString != ""){
                if(i == userDeviceIndex){
                    Allure.addAttachment("Exception in User", exceptionString);
                }
                else{
                    Allure.addAttachment("Exception in Driver", exceptionString);
                }
            }
        }
    }
    
}