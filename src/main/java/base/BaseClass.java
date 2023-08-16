package base;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.screenrecording.CanRecordScreen;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import org.openqa.selenium.remote.DesiredCapabilities;



public class BaseClass {

    public static String rideOTP = "";
    public static AndroidDriver user, driver;
    public static int UserDeviceIndex, DriverDeviceIndex;
    public static int DeviceIndex = 0;
    public static String version = "";
    public static String appPath = "";
    public static String userApkName = SlackBotIntegration.CUSTOMER_FILE;
    public static String driverApkName = SlackBotIntegration.DRIVER_FILE;
   
    public static void setup(boolean isUser) throws MalformedURLException {

        DesiredCapabilities userCapabilities = new DesiredCapabilities();
        DesiredCapabilities driverCapabilities = new DesiredCapabilities();
        if(isUser) {
            user = setupAndroidCapUser(ADBDeviceFetcher.devices.get(DeviceIndex), userApkName, userCapabilities);
            UserDeviceIndex = DeviceIndex;
            System.out.println("\nUser App Installed in " + ADBDeviceFetcher.devices.get(DeviceIndex) + " device\n");
        }
        else{
            driver = setupAndroidCapDriver(ADBDeviceFetcher.devices.get(DeviceIndex), driverApkName, driverCapabilities);
            DriverDeviceIndex = DeviceIndex;
            System.out.println("\nDriver App Installed in " + ADBDeviceFetcher.devices.get(DeviceIndex) + " device\n");
        }
        DeviceIndex ++;
    }

    private static AndroidDriver setupAndroidCapDriver(String udid, String appFile, DesiredCapabilities capabilities) throws MalformedURLException {
        appPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator
                + "java" + File.separator + "NYAutomation" + File.separator + "resources" + File.separator + "BotApk" + File.separator;
        String appFilePath = appPath + appFile;

        capabilities.setCapability("platformName", "Android");
       
        capabilities.setCapability("udid", udid);
        capabilities.setCapability("automationName", "UIAutomator2");
        capabilities.setCapability("newCommandTimeout", 1800000);   // 30 mins
        capabilities.setCapability("fullReset", true);
        capabilities.setCapability("app", appFilePath);
        
        AndroidDriver driver = new AndroidDriver(new URL("http://0.0.0.0:4724"), capabilities);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        return driver;
    }

    private static AndroidDriver setupAndroidCapUser(String udid, String appFile, DesiredCapabilities capabilities) throws MalformedURLException {
        appPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator
                + "java" + File.separator + "NYAutomation" + File.separator + "resources" + File.separator + "BotApk" + File.separator;
        String appFilePath = appPath + appFile;

        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("udid", udid);
        capabilities.setCapability("automationName", "UIAutomator2");
        capabilities.setCapability("newCommandTimeout", 1800000);   // 30 mins
        capabilities.setCapability("fullReset", true);
        capabilities.setCapability("app", appFilePath);

        AndroidDriver driver = new AndroidDriver(new URL("http://0.0.0.0:4725"), capabilities);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		return driver;
    }

    public static void driverStartScreenRecording() {
        ((CanRecordScreen) driver).startRecordingScreen();
        System.out.println("driver startScreenRecording executed");
    }

    public static void userStartScreenRecording() {
        ((CanRecordScreen) user).startRecordingScreen();
        System.out.println("user startScreenRecording executed");
    }
}