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
    private static int DeviceIndex = 0;
    public static void setup(boolean isUser) throws MalformedURLException {

        DesiredCapabilities userCapabilities = new DesiredCapabilities();
        DesiredCapabilities driverCapabilities = new DesiredCapabilities();
        if(isUser) {
            user = setupAndroidDriver(ADBDeviceFetcher.devices.get(DeviceIndex), "user-apk-name.apk", userCapabilities);
            UserDeviceIndex = DeviceIndex;
            System.out.println("\nUser App Installed in " + ADBDeviceFetcher.devices.get(DeviceIndex) + " device\n");
        }
        else{
            driver = setupAndroidDriver(ADBDeviceFetcher.devices.get(DeviceIndex), "driver-apk-name.apk", driverCapabilities);
            DriverDeviceIndex = DeviceIndex;
            System.out.println("\nDriver App Installed in " + ADBDeviceFetcher.devices.get(DeviceIndex) + " device\n");
        }
        DeviceIndex ++;
    }

    private static AndroidDriver setupAndroidDriver(String udid, String appFile, DesiredCapabilities capabilities) throws MalformedURLException {
        String appPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main"
                        + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "resources" + File.separator;
        String appFilePath = appPath + appFile;

        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("udid", udid);
        capabilities.setCapability("automationName", "UIAutomator2");
        capabilities.setCapability("fullReset", true);
        capabilities.setCapability("app", appFilePath);

        AndroidDriver driver = new AndroidDriver(new URL("http://0.0.0.0:4725"), capabilities);
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