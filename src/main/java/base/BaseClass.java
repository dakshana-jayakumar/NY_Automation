package base;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import org.openqa.selenium.remote.DesiredCapabilities;
import io.appium.java_client.android.AndroidDriver;

public class BaseClass {

    public static String rideOTP = "";
    public static AndroidDriver androidUser, androidDriver;
    public static int UserDeviceIndex = -1, DriverDeviceIndex = -1;
    public static int DeviceIndex = 0;
    public static String version = "";
    public static String appPath = "";
    public static String userApkName = "customer.apk";
    public static String driverApkName = "driver.apk";

    public static void setup(boolean isUser) throws IOException {
        if((DeviceIndex < ADBDeviceFetcher.devices.size())){
            if (isUser) {
                setupAndroidUser(isUser);
            }
            else {
                setupAndroidDriver(isUser);
            }
            DeviceIndex++;
        }
    }

    public static void setupAndroidDriver(boolean isUser) throws IOException {
        try {
            DesiredCapabilities driverCapabilities = new DesiredCapabilities();
            setAndroidCapability(ADBDeviceFetcher.devices.get(DeviceIndex), driverApkName,
                    driverCapabilities, isUser);
            DriverDeviceIndex = DeviceIndex;
            System.out.println("\nDriver App Installed in " + ADBDeviceFetcher.devices.get(DeviceIndex) + " device\n");
        } catch (Exception e) {
            if(DriverDeviceIndex > 0){
                LogcatToFile.fetchAppDetails();
                LogcatToFile.searchApiErr();
                LogcatToFile.searchJavaScriptError();
                LogcatToFile.fetchExceptions();
            }
        }
    }
    
    public static void setupAndroidUser(boolean isUser) throws IOException {
    	try {
            DesiredCapabilities userCapabilities = new DesiredCapabilities();
                setAndroidCapability(ADBDeviceFetcher.devices.get(DeviceIndex), userApkName,
                        userCapabilities, isUser);
                UserDeviceIndex = DeviceIndex;
                System.out.println("\nUser App Installed in " + ADBDeviceFetcher.devices.get(DeviceIndex) + " device\n");
        } catch (Exception e) {
            if(UserDeviceIndex > 0){
                LogcatToFile.fetchAppDetails();
                LogcatToFile.searchApiErr();
                LogcatToFile.searchJavaScriptError();
                LogcatToFile.fetchExceptions();
            }
        }
    }

    private static void setAndroidCapability(String udid, String appFile, DesiredCapabilities capabilities, boolean isUser) throws IOException {
        appPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator
                + "java" + File.separator + "NYAutomation" + File.separator + "resources" + File.separator;
        String appFilePath = appPath + appFile;

        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("udid", udid);
        capabilities.setCapability("automationName", "UIAutomator2");
        capabilities.setCapability("newCommandTimeout", 1800000);   // 30 mins
        capabilities.setCapability("fullReset", true);
        capabilities.setCapability("app", appFilePath);
        if(isUser){
            androidUser = new AndroidDriver(new URL("http://0.0.0.0:4725"), capabilities);
            androidUser.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        }else{
            androidDriver = new AndroidDriver(new URL("http://0.0.0.0:4726"), capabilities);
            androidDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        }
    }
}