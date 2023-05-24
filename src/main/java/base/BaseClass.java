package base;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import io.appium.java_client.android.AndroidDriver;
public class BaseClass extends Utilities {

    public static String rideOTP = "";

    @BeforeClass
    public void launchApp() throws MalformedURLException, InterruptedException {

        String appPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main"
                + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "resources"
                + File.separator;
        String driverApp = appPath + "NY_Driver_without_testid(26.04.23).apk";
        String userApp = appPath + "NY_Customer_without_testid(26.04.23).apk";
        
		DesiredCapabilities userCap = new DesiredCapabilities();
        userCap.setCapability("platformName", "Android");
        userCap.setCapability("udid", "emulator-5554");
        userCap.setCapability("automationName", "UIAutomator2");
        userCap.setCapability("fullReset", true);
        userCap.setCapability("app", userApp);
		user = new AndroidDriver(new URL("http://0.0.0.0:4724"), userCap);
		user.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        DesiredCapabilities driverCap = new DesiredCapabilities();
        driverCap.setCapability("platformName", "Android");
        driverCap.setCapability("udid", "emulator-5556");
        driverCap.setCapability("automationName", "UIAutomator2");
        driverCap.setCapability("fullReset", true);
        driverCap.setCapability("app", driverApp);
		driver = new AndroidDriver(new URL("http://0.0.0.0:4724"), driverCap);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

    }

    @AfterClass
    public void tearDown() throws InterruptedException {
        driver.quit();
        user.quit();
    }
}