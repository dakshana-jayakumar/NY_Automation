package NYAutomation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import base.ADBDeviceFetcher;
import static base.ADBDeviceFetcher.devices;
import static base.BaseClass.DriverDeviceIndex;
import static base.BaseClass.UserDeviceIndex;
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
    public static Boolean searchErrCode(boolean isUser) throws IOException {
        String searchString1 = "Err Code";
        String searchString2 = "code  :  ";
        String matchedLine1 = null;
        String matchedLine2 = null;
        String deviceSerialNumber;
        
        if (isUser) {
            deviceSerialNumber = devices.get(UserDeviceIndex);
        } else {
            deviceSerialNumber = devices.get(DriverDeviceIndex);
        }
        
        String logcatFile1 = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "resources" + File.separator 
                                + "logFile_" + deviceSerialNumber + ".txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(logcatFile1))) {
            String line;
            String previousLine = null;
            while ((line = reader.readLine()) != null) {
                if (line.contains(searchString1)) {
                    matchedLine1 = previousLine + "\n" + line;
                } else if (line.contains(searchString2)) {
                    matchedLine2 = matchedLine1 + "\n" + line;
                }
                previousLine = line;
            }
        } catch (IOException e) {
            // Handle any exceptions
        }
        
        if ((matchedLine2 != null) && compareTime(matchedLine2)){
            if (isUser) {
                System.out.println("\nUser Api Error:" + matchedLine2);
                Allure.addAttachment("User Api Error ", matchedLine2);
            } else {
                System.out.println("\nDriver Api Error:" + matchedLine2);
                Allure.addAttachment("Driver Api Error ", matchedLine2);
            }
            return false;
        }
        return true;
    }
}
