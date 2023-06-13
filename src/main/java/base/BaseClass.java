package base;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.remote.DesiredCapabilities;

import io.appium.java_client.android.AndroidDriver;

public class BaseClass {

    public static String rideOTP = "";
    public static AndroidDriver user, driver;
    public static void setup(boolean isUser) throws MalformedURLException {
        DesiredCapabilities userCapabilities = new DesiredCapabilities();
        DesiredCapabilities driverCapabilities = new DesiredCapabilities();
        
        if(isUser) {
        	user = setupAndroidDriver(ADBDeviceFetcher.devices.get(0), "app-user-prod-debug.apk", userCapabilities);
        }
        else {
	       	driver = setupAndroidDriver(ADBDeviceFetcher.devices.get(1), "app-driver-prod-debug.apk", driverCapabilities);
        }
    }

    private static AndroidDriver setupAndroidDriver(String udid, String appFile, DesiredCapabilities capabilities) throws MalformedURLException {
        String appPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main"
                + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "resources"
                + File.separator;
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
}