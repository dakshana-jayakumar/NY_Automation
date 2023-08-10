package base;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import org.openqa.selenium.remote.DesiredCapabilities;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;

public class BaseClass {

	public static String rideOTP = "";
    public static AndroidDriver user, driver;
    public static int userDeviceIndex = -1, driverDeviceIndex = -1;
    public static int deviceIndex = 0;
    public static String version = "";
    public static String appPath = "";
    public static String userApkName = "app-ny_-prod-debug-master-15-jul.apk";
    public static String driverApkName = "app-ny_p_-prod-debug-master-15-Jul.apk";
   
    public static void setup(boolean isUser) throws IOException {
        if((deviceIndex < ADBDeviceFetcher.devices.size())){
            if (isUser) {
                setupAndroidUser(isUser);
            }
            else {
                setupAndroidDriver(isUser);
            }
            deviceIndex++;
        }
    }
    
    public static void cugSetup(boolean isUser) throws IOException {
        if((deviceIndex < ADBDeviceFetcher.devices.size())){
            if (isUser) {
                setupAndroidCugUser(isUser);
            }
            else {
                setupAndroidCugDriver(isUser);
            }
            deviceIndex++;
        }
    }


   public static void setupAndroidDriver(boolean isUser) throws IOException {
       try {
           DesiredCapabilities driverCapabilities = new DesiredCapabilities();
           setAndroidCapability(ADBDeviceFetcher.devices.get(deviceIndex), driverApkName, driverCapabilities, isUser);
           driverDeviceIndex = deviceIndex;
           System.out.println("\nDriver App Installed in " + ADBDeviceFetcher.devices.get(deviceIndex) + " device\n");
       } catch (Exception e) {
           if(driverDeviceIndex > 0){
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
           setAndroidCapability(ADBDeviceFetcher.devices.get(deviceIndex), userApkName, userCapabilities, isUser);
           userDeviceIndex = deviceIndex;
           System.out.println("\nUser App Installed in " + ADBDeviceFetcher.devices.get(deviceIndex) + " device\n");
       } catch (Exception e) {
           if(userDeviceIndex > 0){
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
            user = new AndroidDriver(new URL("http://0.0.0.0:4724"), capabilities);
            user.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        }else{
            driver = new AndroidDriver(new URL("http://0.0.0.0:4725"), capabilities);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        }
    }
    

    public static void setupAndroidCugDriver(boolean isUser) throws IOException {
        try {
            DesiredCapabilities driverCapabilities = new DesiredCapabilities();
            setAndroidCugCapability(ADBDeviceFetcher.devices.get(deviceIndex), driverCapabilities, isUser);
            driverDeviceIndex = deviceIndex;
            System.out.println("\nDriver App Installed in " + ADBDeviceFetcher.devices.get(deviceIndex) + " device\n");
        } catch (Exception e) {
            if(driverDeviceIndex > 0){
                LogcatToFile.fetchAppDetails();
                LogcatToFile.searchApiErr();
                LogcatToFile.searchJavaScriptError();
                LogcatToFile.fetchExceptions();
            }
        }
    }
    
    public static void setupAndroidCugUser(boolean isUser) throws IOException {
    	try {
            DesiredCapabilities userCapabilities = new DesiredCapabilities();
            setAndroidCugCapability(ADBDeviceFetcher.devices.get(deviceIndex), userCapabilities, isUser);
            userDeviceIndex = deviceIndex;
            System.out.println("\nUser App Installed in " + ADBDeviceFetcher.devices.get(deviceIndex) + " device\n");
        } catch (Exception e) {
            if(userDeviceIndex > 0){
                LogcatToFile.fetchAppDetails();
                LogcatToFile.searchApiErr();
                LogcatToFile.searchJavaScriptError();
                LogcatToFile.fetchExceptions();
            }
        }
    }
    
    private static void setAndroidCugCapability(String udid, DesiredCapabilities capabilities, boolean isUser) throws MalformedURLException {
    	capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("udid", udid);
        capabilities.setCapability("automationName", "UIAutomator2");
        capabilities.setCapability("newCommandTimeout", 1800000);   // 30 mins
        
        if (isUser) {
	      	user = new AndroidDriver(new URL("http://0.0.0.0:4724"), capabilities);
	      	user.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
	        Activity activity = new Activity("in.juspay.nammayatri", "in.juspay.mobility.MainActivity");
            user.startActivity(activity);
	      }
        else {
        	driver = new AndroidDriver(new URL("http://0.0.0.0:4725"), capabilities);
        	driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        	Activity activity = new Activity("in.juspay.nammayatripartner", "in.juspay.mobility.MainActivity");
        	driver.startActivity(activity);
        }
    }
}