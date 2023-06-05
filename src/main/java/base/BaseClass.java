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

    public void setup(boolean isUser) throws MalformedURLException {
        DesiredCapabilities userCapabilities = new DesiredCapabilities();
        DesiredCapabilities driverCapabilities = new DesiredCapabilities();
        
        if(isUser) {
        	user = setupAndroidDriver("emulator-5554", "nammaYatri-prod-debug-29-may-2023.apk", userCapabilities);
        }
        else {
        	driver = setupAndroidDriver("emulator-5556", "nammaYatriPartner-prod-debug-29-may-2023.apk", driverCapabilities);
        }
    }

    private AndroidDriver setupAndroidDriver(String udid, String appFile, DesiredCapabilities capabilities) throws MalformedURLException {
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