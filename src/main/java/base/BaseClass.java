package base;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.appium.java_client.android.AndroidDriver;

public class BaseClass {

    public static String rideOTP = "";
    public static AndroidDriver user, driver;
    public static int UserDeviceIndex, DriverDeviceIndex;
    public static int DeviceIndex = 0;
    public static String version = "";
    public static String appPath = "";
    public static String userApkName = "app-ny_-prod-debug-master-15-jul.apk";
    public static String driverApkName = "app-ny_p_-prod-debug-master-15-Jul.apk";
   
    public static void setup(boolean isUser) throws MalformedURLException {

        DesiredCapabilities userCapabilities = new DesiredCapabilities();
        DesiredCapabilities driverCapabilities = new DesiredCapabilities();
        if(isUser) {
            user = setupAndroidDriver(ADBDeviceFetcher.devices.get(DeviceIndex), userApkName, userCapabilities);
            UserDeviceIndex = DeviceIndex;
            System.out.println("\nUser App Installed in " + ADBDeviceFetcher.devices.get(DeviceIndex) + " device\n");
        }
        else{
            driver = setupAndroidDriver(ADBDeviceFetcher.devices.get(DeviceIndex), driverApkName, driverCapabilities);
            DriverDeviceIndex = DeviceIndex;
            System.out.println("\nDriver App Installed in " + ADBDeviceFetcher.devices.get(DeviceIndex) + " device\n");
        }
        DeviceIndex ++;
    }

    private static AndroidDriver setupAndroidDriver(String udid, String appFile, DesiredCapabilities capabilities) throws MalformedURLException {
        appPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main"
                        + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "resources" + File.separator;
        String appFilePath = appPath + appFile;

        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("udid", udid);
        capabilities.setCapability("automationName", "UIAutomator2");
        capabilities.setCapability("fullReset", true);
        capabilities.setCapability("app", appFilePath);

        AndroidDriver driver = new AndroidDriver(new URL("http://0.0.0.0:4724"), capabilities);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		return driver;

    }
    
    public static WebDriver getDriver() {
        return driver;
    }
    public static WebDriver getUser() {
        return user;
    }
    
}