package NYAutomation;



import base.BaseClass;
import base.LogcatToFile;
import base.ADBDeviceFetcher;
import static base.ADBDeviceFetcher.androidVersions;
import static base.ADBDeviceFetcher.brandNames;
import static base.ADBDeviceFetcher.modelNames;
import static base.ADBDeviceFetcher.resolutions;
import static base.ADBDeviceFetcher.devices;

import com.google.common.collect.ImmutableMap;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import io.appium.java_client.TouchAction;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;

import java.time.Duration;
import java.util.Scanner;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.NoSuchElementException;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;




public class AutomationFlow extends BaseClass {

    private String rideOtp = "";

    static Map<String, String> screenStatusMap = new HashMap<>();

	private final String newUserMobileNumber = "7777777733";
	private final String newDriverMobileNumber = "9999999933";
	
	private final String existingUserMobileNumber = "7777777724";
	private final String existingDriverMobileNumber = "9999999922";

    private final String enterWrongOtp = "7895";
    private final String enterCorrectOtp = "7891";
	
	private String cugUserMobileNumber = "8497879347";
	private String cugDriverMobileNumber = "8497879347";
	
    private static boolean ReportFlag = true;
    private static char[] firstAltNumber;
    long prevTimeStamp;
    long currentTimeStamp;
    String formattedTimeDifference = "";
    
    public static boolean userFlag = true;
    public static boolean driverFlag = true;
    public static boolean isUser = false;
    public static String cugOrMasterInput;
    
    
	@Test
    @Epic("Allure Results")
    @Feature("TestNG support")
    @Story("Application flow")
    /* Creating a method for overall flow of the applications */
    public void flow() throws Exception {
    	/* Add Allure report cleanup code here */
    	/* Fetch test data from Google Sheet */
        TestDataReader.fetchTabNames();
        ADBDeviceFetcher.fetchAdbDeviceProperties();
        String[][] testData = TestDataReader.testData.toArray(new String[0][0]);
        LogcatToFile.CaptureLogs();
        cleanupAllureReport();
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter 'cug' or 'master': ");
        cugOrMasterInput = scanner.next();
        scanner.close();
        
        prevTimeStamp = System.currentTimeMillis();

        for (String[] actionParameter : testData) {
            String testCase = actionParameter[0];
            String screen = actionParameter[1];
            String state = actionParameter[2];
            String xpath = actionParameter[3];
            String sendKeysValue = actionParameter[4];
            isUser = "user".equals(actionParameter[5]);
            String whichApp = actionParameter[5];

            if ((userFlag && isUser)) {
                userFlag = false;
                callSetup(isUser);
            }
            else if ((driverFlag && !isUser)) {
                driverFlag = false;
                callSetup(isUser);
            }

            System.out.println("EpochTime: " + currentTimeStamp + " | ActionTime: " + formattedTimeDifference + " | screen: " + screen + " | state: " + state + " | XPath: " + xpath + " | SendKeys Value: " + sendKeysValue);
            try {
            	checkCase(testCase, screen, state, xpath, sendKeysValue, isUser);
            	screenStatusMap.put(currentTimeStamp + "|" + formattedTimeDifference + "|" + whichApp + "|" + testCase + "|" + screen + "|" + state, "Passed");
            }
            catch (Exception e) {
                screenStatusMap.put(currentTimeStamp + "|" + formattedTimeDifference + "|" + whichApp + "|" + testCase + "|" + screen + "|" + state, "Failed");
            	logErrorToAllureReport(e.getMessage(), driver, user, screenStatusMap);
                throw e;
            }
        }
        logPassToAllureReport("Build Passed!", driver, user, screenStatusMap);
    }
    
    private void callSetup(boolean isUser) throws IOException {
        if(cugOrMasterInput.toLowerCase().equals("cug")) {
            cugSetup(isUser);
        }
        else {
            setup(isUser);
        }
    }

    // Method to format time difference as "X m Y s Z ms"
    public String formatTimeDifference(long timeDifference) {
        long milliseconds = timeDifference % 1000;
        timeDifference /= 1000;
        long seconds = timeDifference % 60;
        timeDifference /= 60;
        long minutes = timeDifference % 60;

        StringBuilder formattedTime = new StringBuilder();
        if (minutes > 0) {
            formattedTime.append(minutes).append("m ");
        }
        if (seconds > 0) {
            formattedTime.append(seconds).append("s ");
        }
        if (milliseconds > 0) {
            formattedTime.append(milliseconds).append("ms");
        }
        return formattedTime.toString().trim();
    }

    public static void addDeviceConfigToReport() {               
        StringBuilder devicesConfig = new StringBuilder();
        devicesConfig.append(String.format("%-20s | %-15s | %-30s | %-15s | %-15s%n", "Device", "Brand", "Model", "Version", "Resolution"));
        
        for (int i = 0; i < deviceIndex; i++) {
            String device = i < devices.size() ? devices.get(i) : "N/A";
            String brand = i < brandNames.size() ? brandNames.get(i) : "N/A";
            String model = i < modelNames.size() ? modelNames.get(i) : "N/A";
            String version = i < androidVersions.size() ? androidVersions.get(i) : "N/A";
            String resolution = i < resolutions.size() ? resolutions.get(i) : "N/A";
            devicesConfig.append(String.format("%-20s | %-15s | %-30s | %-15s | %-15s%n", device, brand, model, version, resolution));
        }
        Allure.addAttachment("Devices Config", devicesConfig.toString());
    }

    public void cleanupAllureReport() throws IOException {
        // Specify the directory path where the Allure report files are located
        String reportDirectoryPath = System.getProperty("user.dir") + File.separator + "allure-results";

        // Create a File object for the directory
        File reportDirectory = new File(reportDirectoryPath);

        // Check if the directory exists
        if (reportDirectory.exists() && reportDirectory.isDirectory()) {
        // Read user input

            Scanner scanner = new Scanner(System.in);

            System.out.print("Do you want to delete the files in the Allure report directory? (yes/no): ");
            String deleteReportFilesInput = scanner.nextLine();
            // Check user response
            if (deleteReportFilesInput.equalsIgnoreCase("yes")) {
                // Get all files in the directory
                File[] files = reportDirectory.listFiles();

                // Iterate through each file
                if (files != null) {
                    for (File file : files) {
                        // Check if the file is a regular file (not a directory)
                        if (file.isFile()) {
                            // Delete the file
                            boolean deleted = file.delete();

                            // Check if the file deletion was successful
                            if (deleted) {
                                System.out.println("Deleted file: " + file.getName());
                            } else {
                                System.out.println("Failed to delete file: " + file.getName());
                            }
                        }
                    }
                }

                System.out.println("Allure report files have been deleted.");
            } else {
                System.out.println("Allure report files will not be deleted.");
            }
        } else {
            System.out.println("Allure report directory does not exist or is not a directory.");
        }
    }
    
    /* method used to code all the types of functions to be handled */
    public void checkCase(String testCase, String screen, String state, String xpath, String sendKeysValue, boolean isUser) throws Exception {
    	/* Variable to store wait */
    	Wait<AndroidDriver> wait = waitTime(isUser);

        if ("Enter Mobile Number".equals(state)) {
            (isUser ? user : driver).manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
            try {
                WebElement element = (isUser ? user : driver).findElement(AppiumBy.xpath("//android.widget.Button[@text='NONE OF THE ABOVE']"));
                if (element.isDisplayed()) {
                    element.click();
                }
            } catch (NoSuchElementException e) {}
            (isUser ? user : driver).findElement(AppiumBy.xpath(xpath)).click();
            if (cugOrMasterInput.contains("cug")) {
            	sendKeysValue = (isUser ? cugUserMobileNumber : cugDriverMobileNumber);
            }
            else {
            	sendKeysValue = (isUser ? newUserMobileNumber : newDriverMobileNumber);
            }
        }
        
        
        if ("Second Enter Mobile Number".equals(state)) {
            (isUser ? user : driver).manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
            try {
                WebElement element = (isUser ? user : driver).findElement(AppiumBy.xpath("//android.widget.Button[@text='NONE OF THE ABOVE']"));
                if (element.isDisplayed()) {
                    element.click();
                }
            } catch (NoSuchElementException e) {}
            (isUser ? user : driver).findElement(AppiumBy.xpath(xpath)).click();
            if (cugOrMasterInput.contains("cug")) {
            	sendKeysValue = (isUser ? cugUserMobileNumber : cugDriverMobileNumber);
            }
            else {
            	sendKeysValue = (isUser ? existingUserMobileNumber : existingDriverMobileNumber);
            }
        }

        else if ("Re enter mobile number".equals(state)) {
            try {
                WebElement element = (isUser ? user : driver).findElement(AppiumBy.xpath("//android.widget.Button[@text='NONE OF THE ABOVE']"));
                if (element.isDisplayed()) {
                    element.click();
                }
            } catch (NoSuchElementException e) {}
            WebElement element = user.findElement(AppiumBy.xpath(xpath));
            element.click();
            element.clear();
            return;
        }

    	else if ("Location Permission".equals(state)) {
            /* If the state is "Location Permission" */
            /* Call the checkLocationPermission method to modify the xpath value */
            xpath = checkLocationPermission(xpath, isUser);
        }
        
    	else if ("Select Namma Yatri Partner".equals(state)) {
            /* If the state is "Select Namma Yatri Partner"*/
            /* Call the checkOverlayPermission method to perform action */
            boolean doAction = checkOverlayPermission();
            if(!doAction) {return;}
        }
        
    	else if ("Enable Toggle".equals(state)) {
        	if ("realme".equals(brandNames.get(driverDeviceIndex))) {
        		driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Always']")).click();
        	}
        	else {
        		By buttonLayoutLocator = By.xpath(xpath);
            	driver.findElement(buttonLayoutLocator).click();
        	}
        	return;
        }
        
        else if("Overlay screen Back Icon".equals(state)){
            if (("Redmi".equals(brandNames.get(driverDeviceIndex)) && ("adb-ZTJF8P9HYPXCOBPN-tmmRaD._adb-tls-connect._tcp.".equals(devices.get(driverDeviceIndex))))) {
                driver.findElement(AppiumBy.xpath("//android.view.ViewGroup/android.widget.ImageView")).click();
            }
            int androidVersion = Integer.parseInt(androidVersions.get(driverDeviceIndex));
            if ((androidVersion == 10) || (androidVersion <= 8) || (androidVersion == 12)) {
                KeyEvent appSwitcherKeyEvent = new KeyEvent(AndroidKey.BACK);
    	        (isUser ? user : driver).pressKey(appSwitcherKeyEvent);
                return;
    	    }
        }
        
        else if ("Allow Battery Optimization".equals(state)) {
        	if ("Redmi".equals(brandNames.get(driverDeviceIndex)) && ("eb23ba".equals(devices.get(driverDeviceIndex))) || ("Redmi".equals(brandNames.get(driverDeviceIndex)) && ("adb-ZTJF8P9HYPXCOBPN-tmmRaD._adb-tls-connect._tcp.".equals(devices.get(driverDeviceIndex))))) {
        		driver.findElement(AppiumBy.xpath("//android.widget.CheckedTextView[@text='Battery saver (recommended)']")).click();
        		return;
        	}
        	else {
        		xpath = checkBatteryPermission(xpath);
        	}
        }
        
        else if ("AutoStart".equals(state)) {
        	int androidVersion = Integer.parseInt(androidVersions.get(driverDeviceIndex));
        	driver.findElement(AppiumBy.xpath(xpath)).click();
        	
        	if (androidVersion == 10) {
    	        Thread.sleep(2000);
                KeyEvent appSwitcherKeyEvent = new KeyEvent(AndroidKey.BACK);
    	        (isUser ? user : driver).pressKey(appSwitcherKeyEvent);
            }
    	    if ("Redmi".equals(brandNames.get(driverDeviceIndex)) && ("eb23ba".equals(devices.get(driverDeviceIndex))) || ("Redmi".equals(brandNames.get(driverDeviceIndex)) && ("adb-ZTJF8P9HYPXCOBPN-tmmRaD._adb-tls-connect._tcp.".equals(devices.get(driverDeviceIndex))))) {
    	    	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Autostart app in background']")).click();
    	    	scrollToText("Namma Yatri Partner");
    	    	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Namma Yatri Partner']")).click();
    	    	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Allow apps to start automatically']")).click();
    	    	driver.findElement(AppiumBy.xpath("//android.view.ViewGroup/android.widget.ImageView")).click();
    	    	driver.findElement(AppiumBy.xpath("//android.view.ViewGroup/android.widget.ImageView")).click();
    	    }
    	    return;
        }
        
        else if ("AutoStart Screen Back Icon".equals(state) && checkAutoStartPermission()) {
            /* If the state is "AutoStart Screen Back Icon" and checkAutoStartPermission is true */
            /* Return from the current method or function */
            return;
        }
        
        
        else if ("Scroll Function".equals(state)) {
            int androidVersion = Integer.parseInt(androidVersions.get(userDeviceIndex));
        	By buttonLayoutLocator = By.xpath(xpath);
            WebElement source = user.findElement(buttonLayoutLocator);
            if (androidVersion == 9 || androidVersion == 10 || androidVersion == 11 || androidVersion == 12 || androidVersion == 13) {
                boolean canScrollMore = (Boolean)user.executeScript("mobile: scrollGesture", ImmutableMap.of(
                        "left", 100, "top", 100, "width", 1200, "height", 1200,
                        "direction", "down",
                        "percent", 3.0
                        ));
                return;
            }
            else {
        		// Duration of the drag gesture in milliseconds
        		int durationInMillis = 3000;
                int startX = source.getLocation().getX();
                int startY = source.getLocation().getY();
                int endX = startX + 500; // Drag the element horizontally by 500 pixels
                int endY = startY;

                long startTime = System.currentTimeMillis();

                while (System.currentTimeMillis() - startTime <= durationInMillis) {
                    TouchAction<?> touchAction = new TouchAction<>(user);
                    touchAction.press(PointOption.point(startX, startY))
                        .waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000)))
                        .moveTo(PointOption.point(endX, endY))
                        .release()
                        .perform();
                }
                return;
            }
        }
        
        else if (("Fetch Otp").equals(state)) {
            // Fetch correct OTP
            String rideOtp = "";
            for (int i = 1; i <= 4; i++) {
                rideOtp = rideOtp + wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath + "/../android.widget.LinearLayout/android.widget.LinearLayout[" + i + "]/android.widget.TextView"))).getText();
            }
            // Print fetched OTP
            System.out.println("Fetched Ride OTP = " + rideOtp);

           Thread.sleep(2000);
           driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='1']")).click();
           driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='2']")).click();
           driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='3']")).click();
           driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='4']")).click();

            // Convert OTP to char array and enter correct OTP
            char[] otp = rideOtp.toCharArray();
            for (int i = 0; i < 4; i++) {
                char digit = otp[i];
                System.out.println("Entering Otp Digit = " + digit);
                String xpath2 = "//android.widget.TextView[@text='Please ask the customer for the OTP']/../../../android.widget.LinearLayout[2]/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.TextView[@text='" + digit + "']";
                driver.findElement(AppiumBy.xpath(xpath2)).click();
            }
            return;
        }
        
        else if ("App switch".equals(state)) {
            int loopCount = 2; // Number of times to loop
            for (int i = 0; i < loopCount; i++) {
    	        Thread.sleep(1000);
    	        /* Simulate the App Switcher (Recent Apps) key */
    	        KeyEvent appSwitcherKeyEvent = new KeyEvent(AndroidKey.APP_SWITCH);
    	        (isUser ? user : driver).pressKey(appSwitcherKeyEvent);
            }
    	    return;
    	}

        else if ("Google Map Navigation".equals(state)) {
            Thread.sleep(3000);
            try {
                if (driver.findElement(AppiumBy.xpath("//android.widget.ImageView[@content-desc='Close navigation']")).isDisplayed()) {
                    driver.findElement(AppiumBy.xpath("//android.widget.ImageView[@content-desc='Close navigation']")).click();
                }
            } catch (NoSuchElementException e) {
            }
            return;
        }
        
        else if ("Home Press".equals(state)) {
            Thread.sleep(2000);
            (isUser ? user : driver).pressKey(new KeyEvent(AndroidKey.HOME));
            return;
        }

        else if ("Map render".equals(state)) {
            (isUser ? user : driver).manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            try {
                By buttonLayoutLocator = By.xpath(xpath);
                wait.until(ExpectedConditions.elementToBeClickable(buttonLayoutLocator)).click();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        else if ("Back to hamburger".equals(state)) {
            (isUser ? user : driver).manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            try {
                By buttonLayoutLocator = By.xpath(xpath);
                wait.until(ExpectedConditions.elementToBeClickable(buttonLayoutLocator)).click();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        else if ("Booking Preference".equals(state)) {
            user.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            try {
                By buttonLayoutLocator = By.xpath(xpath);
                wait.until(ExpectedConditions.elementToBeClickable(buttonLayoutLocator)).click();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        else if ("Select contacts".equals(state)) {
            user.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            try {
                By buttonLayoutLocator = By.xpath(xpath);
                wait.until(ExpectedConditions.elementToBeClickable(buttonLayoutLocator)).click();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        else if ("Where To".equals(state)) {
            user.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
            try {
                By buttonLayoutLocator = By.xpath(xpath);
                wait.until(ExpectedConditions.invisibilityOfElementLocated(buttonLayoutLocator));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        else if ("All time".equals(state)) {
            (isUser ? user : driver).manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
            try {
                By buttonLayoutLocator = By.xpath(xpath);
                wait.until(ExpectedConditions.elementToBeClickable(buttonLayoutLocator)).click();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        else if ("Share app".equals(state)) {
            user.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
            try {
                By buttonLayoutLocator = By.xpath(xpath);
                wait.until(ExpectedConditions.elementToBeClickable(buttonLayoutLocator)).click();        
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        else if ("Choose ride".equals(state)) {
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
            try {
                By buttonLayoutLocator = By.xpath(xpath);
                wait.until(ExpectedConditions.elementToBeClickable(buttonLayoutLocator)).click();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        else if ("Driver map render".equals(state)) {
            (isUser ? user : driver).manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            try {
                By buttonLayoutLocator = By.xpath(xpath);
                while (isElementPresent(buttonLayoutLocator, isUser ? user : driver)) {
                    WebElement element = user.findElement(By.xpath("//android.widget.TextView[@text='Update']"));
                    element.click();
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(buttonLayoutLocator));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        else if ("2 sec wait".equals(state)) {
            Thread.sleep(2000);
            return;
        }
        
        else if ("SwipeFromUp".equals(state)) {
            /* Perform the pull down the notifications */
            user.openNotifications();
            /* Perform a click action on the element */
            return;
        }
        
        else if ("Swipe".equals(state)) {
            int loopCount = 3;
            for (int i = 0; i < loopCount; i++) {
                By buttonLayoutLocator = By.xpath(xpath);
                WebElement element = user.findElement(buttonLayoutLocator);
                
                // Get the size of the element
                Dimension size = element.getSize();
                int startX = size.width / 2;
                int startY = size.height / 2;

                // Calculate the end coordinates for the swipe gesture
                int endX = (int) (startX - size.width * 0.70);
                int endY = startY;

                // Perform the swipe gesture
                TouchAction<?> touchAction = new TouchAction<>(user);
                touchAction.press(PointOption.point(startX, startY))
                        .waitAction(WaitOptions.waitOptions(Duration.ofMillis(000)))
                        .moveTo(PointOption.point(endX, endY))
                        .release()
                        .perform();
            }
            return;
        }
        
        else if ("Recenter Button".equals(state)) {
            int androidVersion = Integer.parseInt(androidVersions.get(userDeviceIndex));
            if(androidVersion <= 7){
                return;
            }
	    	user.findElement(AppiumBy.xpath(xpath)).click();
        	return;
        }
        
        else if ("Allow Permission".equals(state)) {
            user.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
            try {
        		WebElement element = user.findElement(By.xpath(xpath));
	            if (element.isDisplayed()) {
	            	element.click();
	            }
        	} catch (NoSuchElementException e) {
        	}
        	return;
        }

        else if ("Emergency contacts Permission".equals(state)) {
            try {
        		WebElement element = user.findElement(By.xpath(xpath));
	            if (element.isDisplayed()) {
	            	element.click();
	            }
        	} catch (NoSuchElementException e) {
        	}
        	return;
        }
        
        else if ("Favourite update toast".equals(state)) {
        	String ToastMessage = user.findElement(By.xpath("(//android.widget.Toast)[1]")).getAttribute("name");
    		Assert.assertEquals(ToastMessage, "Favourite Updated Successfully");
    		System.out.println(ToastMessage);
    		return;
        }

        else if ("Location exists toast".equals(state)) {
        	String ToastMessage1 = user.findElement(By.xpath("(//android.widget.Toast)[1]")).getAttribute("name");

    		Assert.assertEquals(ToastMessage1, "location already exists");
    		System.out.println("Validated Toast:"+ ToastMessage1);
    		return;
        }
        
        else if ("Home location toast".equals(state)) {
        	String ToastMessage2 = user.findElement(By.xpath("(//android.widget.Toast)[1]")).getAttribute("name");
    		Assert.assertEquals(ToastMessage2, "Home location already exists");
    		System.out.println("Validated Toast:"+ ToastMessage2);
    		Thread.sleep(3000);
    		return;
        }
        
        else if ("Work location toast".equals(state)) {
        	String ToastMessage3 = user.findElement(By.xpath("(//android.widget.Toast)[1]")).getAttribute("name");
    		Assert.assertEquals(ToastMessage3, "Work location already exists");
    		System.out.println("Validated Toast:"+ ToastMessage3);
    		Thread.sleep(3000);
    		return;
        }
        
        else if ("Error msg".equals(state)) {
        	By buttonLayoutLocator = By.xpath(xpath);
        	String PopUp = user.findElement(buttonLayoutLocator).getText();
    		System.out.println("Error message : " + PopUp);
    		return;
        }
        
        else if ("Clear text".equals(state)) {
        	By buttonLayoutLocator = By.xpath(xpath);
        	user.findElement(buttonLayoutLocator).clear();
        	return;
        }
        
        else if ("Favourite added toast".equals(state)) {
        	String ToastMessage4 = user.findElement(By.xpath("(//android.widget.Toast)[1]")).getAttribute("name");
    		Assert.assertEquals(ToastMessage4, "Favourite Added Successfully");
    		System.out.println("Validated Toast:"+ ToastMessage4);
    		System.out.println("-----------------FAVOURITE TESTCASES DONE-------------------------------");
    		return;
        }
    	
        /* Test cases for 36 search location combinations */
        else if ("Sleep Time".equals(state)) {
        	By buttonLayoutLocator = By.xpath(xpath);
        	user.findElement(buttonLayoutLocator).click();
        	Thread.sleep(4000);
        	return;
        }
        
        else if ("Book Ride Check".equals(state)) {
            user.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
            try {
                WebElement bookRideElement = user.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Book Ride']"));
                if (bookRideElement.isDisplayed()) {
                    bookRideElement.click();
                } else {
                }
                return;
            } catch (Exception e) {
                // Handle the exception
                System.out.println("An exception occurred: " + e.getMessage());
                return;
            }  
        }
    	
        else if ("Back Pressing".equals(state)) {
        	Thread.sleep(6000);
        	user.pressKey(new KeyEvent(AndroidKey.BACK));
        	Thread.sleep(6000);
        	return;
        }
    	
        else if ("Click Fav".equals(state)) {
        	user.findElement(AppiumBy.xpath("//android.widget.TextView[@text='All Favourites']")).click();
            List<WebElement> favList = user.findElements(AppiumBy.xpath("//android.widget.TextView[@text='Select Favourite']/../../android.widget.ScrollView/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.TextView"));
            System.out.println("Favlist Size is _" + favList.size());
            for (int n = 0; n < favList.size(); n++) {
                if (n == 2) {
                    favList.get(n).click();
                }
            }
            Thread.sleep(4000);
            user.pressKey(new KeyEvent(AndroidKey.BACK));
            System.out.println("Case_3_All_Favourites_Executed");
            return;
        }
    	
        else if ("Click Dest Fav".equals(state)) {
        	user.findElement(AppiumBy.xpath("//android.widget.TextView[@text='All Favourites']")).click();
            List<WebElement> favList = user.findElements(AppiumBy.xpath("//android.widget.TextView[@text='Select Favourite']/../../android.widget.ScrollView/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.TextView"));
            System.out.println("Favlist Size is _" + favList.size());
            for (int n = 0; n < favList.size(); n++) {
                if (n == 2) {
                    favList.get(n).click();
                }
            }
            Thread.sleep(2000);
            user.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Book Ride']")).click();
            Thread.sleep(4000);
            user.pressKey(new KeyEvent(AndroidKey.BACK));
            System.out.println("Case_3_All_Favourites_Executed");
            return;
        }
    	
        else if ("Dest Auto Suggestion".equals(state)) {
        	((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.U));
            ((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.L));
            ((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.S));
            ((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.O));
            ((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.BACK));
            return;
        }
    	
        else if ("Source Auto Suggestion".equals(state)) {
        	((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.M));
            ((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.A));
            ((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.J));
            ((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.E));
            
            ((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.BACK));
            return;
        }
        
    	/* Validating the otp entered is correct or not */
        else if ("Login OTP".equals(state)) {
        	if (cugOrMasterInput.contains("cug")) {
        		return;
        	}
            WebElement otpField = user.findElement(AppiumBy.xpath("//android.widget.EditText[@text='Enter 4 digit OTP']"));
            otpField.click();
            otpField.sendKeys(enterWrongOtp);
        	user.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Resend']")).click();
            otpField.sendKeys(enterCorrectOtp);
        	return;
        }
    	
        else if ("Driver Login OTP".equals(state)) {
        	if (cugOrMasterInput.contains("cug")) {
        		return;
        	}
            Thread.sleep(2000);
            WebElement otpField = driver.findElement(AppiumBy.xpath("//android.widget.EditText[@text='Auto Reading OTP...']"));
            otpField.click();
            otpField.sendKeys(enterWrongOtp);
            WebElement driverOtp = driver.findElement(AppiumBy.xpath("//android.widget.EditText"));
            driverOtp.click();
            driverOtp.clear();
            driverOtp.sendKeys(enterCorrectOtp);
        	return;
    	}
        
        /* Test cases to validate the Driver status mode */
    	else if ("Driver Validation".equals(state)) {
            /* Driver status mode validation test case */
            driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
            try {
    			if (driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='GO!']")).isDisplayed()) {
    				System.out.println("Driver is in offline mode");
    				driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='GO!']")).click();
                    System.out.println("Driver changed to Online mode");
    			}
    		} catch (NoSuchElementException e) {
    			try {
    				if (driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Silent']/../android.widget.ImageView")).isDisplayed()) {
    					System.out.println("Driver is in Silent mode");
    					driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Online']")).click();
                        System.out.println("Driver changed to Online mode");
    				}
    			} catch (NoSuchElementException e1) {
    				System.out.println("Driver is in Online mode");
    			}
    		}
    		return;
    	}
        
    	else if ("Waiting Time".equals(state)) {
        	Thread.sleep(5000);
        	return;
        }
        
        else if ("Delete icon".equals(state)) {
        	int loopCount = 10;
            for (int i = 0; i < loopCount; i++) {
            	((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.DEL));
            }
        	return;
        }

        else if ("Notification permission".equals(state)) {
            (isUser ? user : driver).manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        	try {
        		WebElement notificationPopupElement = (isUser ? user : driver).findElement(By.xpath(xpath));
	            if (notificationPopupElement.isDisplayed()) {
	                notificationPopupElement.click();
	            }
        	} catch (NoSuchElementException e) {
        	}
        	return;
        }
        
        else if ("Book Ride".equals(state)) {
            user.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        	try {
        		WebElement element = user.findElement(By.xpath(xpath));
	            if (element.isDisplayed()) {
	            	element.click();
	            }
        	} catch (NoSuchElementException e) {
        	}
        	return;
        }
        
        else if ("Enter Name".equals(state)) {
            user.findElement(AppiumBy.xpath(xpath)).click();
            ((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.R));
            ((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.A));
            ((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.M));
            ((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.U));
            ((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.BACK));
            return;
        }
        
        else if ("Android Back".equals(state)) {
        	((AndroidDriver) (isUser ? user : driver)).pressKey(new KeyEvent(AndroidKey.BACK));
            return;
        }
        
        else if ("Driver Gender Update".equals(state)) {
        	try {
        		WebElement element = driver.findElement(By.xpath(xpath));
	            if (element.isDisplayed()) {
	            	element.click();
	            	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Male']")).click();
	            	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Confirm']")).click();
	            	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Personal Details']/../../android.widget.LinearLayout[1]/android.widget.ImageView")).click();
	            }
        	} catch (NoSuchElementException e) {
        	}
        	return;
        	
        }
        
        else if ("Languages update".equals(state)) {
        	scrollToText("About me");
        	try {
         		WebElement element = driver.findElement(By.xpath("//android.widget.TextView[@text='Languages spoken']/../../android.widget.LinearLayout/android.widget.TextView[@text='Add']"));
 	            if (element.isDisplayed()) {
 	            	element.click();
 	            	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Select the languages you can speak']/../android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout[1]")).click();
	            	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Select the languages you can speak']/../android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout[2]")).click();
	            	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Select the languages you can speak']/../android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout[3]")).click();
	            	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Select the languages you can speak']/../android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout[4]")).click();
	            	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Select the languages you can speak']/../android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout[5]")).click();
                    driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Select the languages you can speak']/../android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout[6]")).click();
	            	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Update']")).click();
 	            }
         	} catch (NoSuchElementException e) {
         		driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Languages spoken']/../../android.widget.LinearLayout/android.widget.ImageView")).click();
         		driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Select the languages you can speak']/../android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout[1]")).click();
            	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Select the languages you can speak']/../android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout[2]")).click();
            	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Select the languages you can speak']/../android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout[3]")).click();
            	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Select the languages you can speak']/../android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout[4]")).click();
            	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Select the languages you can speak']/../android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout[5]")).click();
            	driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Select the languages you can speak']/../android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout[6]")).click();
                driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Update']")).click();
         	}
            Thread.sleep(3000);
         	return;
        }

        else if ("Add Fav loading".equals(state)) {
            user.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
            try {
                By buttonLayoutLocator = By.xpath(xpath);
                wait.until(ExpectedConditions.elementToBeClickable(buttonLayoutLocator)).isDisplayed();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        else if ("Start Ride".equals(state)) {
    	    By buttonLayoutLocator = By.xpath(xpath);
    	    WebElement source = user.findElement(buttonLayoutLocator);
    	    boolean startRideDisplayed = false;
    	    while (!startRideDisplayed) {
    	        try {
    	            WebElement startRideElement = user.findElement(By.xpath("//android.widget.TextView[@text='Start Ride']"));
    	            if (startRideElement.isDisplayed() && startRideElement.isEnabled()) {
    	                startRideDisplayed = true;
    	                startRideElement.click();  // Perform the desired action on the "Start Ride" element
    	            }
    	        } catch (NoSuchElementException | StaleElementReferenceException e) {
    	        }
    	        // Scroll the layout by dragging from source to destination coordinates
    	        ((JavascriptExecutor) user).executeScript("mobile: dragGesture", ImmutableMap.of(
    	                "elementId", ((RemoteWebElement) source).getId(),
    	                "endX", 447,
    	                "endY", 400
    	        ));
    	    }
            return;
    	}

        while ("Remove all the fav".equals(state)) {
            user.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
            try {
                WebElement element = user.findElement(AppiumBy.xpath(xpath));
                if (element.isDisplayed()) {
                    System.out.println("Is Displayed");
                    Thread.sleep(1000);
                    element.click();
                    user.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Yes, Remove']")).click();
                }
            } catch (StaleElementReferenceException e) {
                System.out.println("Element has become stale. Retrying...");
            } catch (NoSuchElementException e) {
                System.out.println("Removed all the fav addresses");
                return;
            }
        }
        
         /* Function calls for both Customer and Driver */   
        {
	        /* Function call to validate the mobile number and otp is entered correct */
	        validateMobileNumberAndOtp(state, sendKeysValue, screen, driver);
	        
	        /* Function for checking cancel ride for both user and driver */
	        cancelRide(state, xpath);
        }
        
        
        /* Function calls only in driver application */
        {
	        /* Function for checking scroll in choose language screen in driver */
	        languageScroll(screen, state, xpath);
	        
	        /* Function for checking alternate mobile number validation for driver */
	        if(state.contains("Alternate Number")){alternateMobileNumberValidation(state, xpath);return;}
        }
        
        
        /* Button layout locator */
        By buttonLayoutLocator = By.xpath(xpath);
        /* Performing action based on input */
        performAction(wait, buttonLayoutLocator, sendKeysValue);
    }


    public Wait<AndroidDriver> waitTime(boolean isUser) {
        /* Creating a wait object to wait for the user or driver */
        Wait<AndroidDriver> wait = new FluentWait<>(isUser ? user : driver)
                .withTimeout(Duration.ofSeconds(180))
                .pollingEvery(Duration.ofMillis(2000))
                .ignoring(Exception.class);
        return wait;
    }

    /**
    * Performs the action based on the provided parameters.
    *
    * @param wait           Wait object for waiting conditions
    * @param buttonLayoutLocator Locator for the button layout
    * @param sendKeysValue  Value to be sent as keys (if not empty)
    **/

    public void performAction(Wait<AndroidDriver> wait, By buttonLayoutLocator, String sendKeysValue) {
        currentTimeStamp = System.currentTimeMillis();
        long timeDifference = currentTimeStamp - prevTimeStamp;
        prevTimeStamp = currentTimeStamp;
        formattedTimeDifference = formatTimeDifference(timeDifference);
          if (!sendKeysValue.isEmpty()) {
              /* perform send keys action to the element */
            wait.until(ExpectedConditions.visibilityOfElementLocated(buttonLayoutLocator)).sendKeys(sendKeysValue);
        } else {
              /* perform click action on the element */
            wait.until(ExpectedConditions.elementToBeClickable(buttonLayoutLocator)).click();
        }
    }
    
    public void scrollToText(String text) {
        /* Scrolls to the specified text */
        driver.findElement(new AppiumBy.ByAndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true).instance(0))"
    				+ ".scrollIntoView(new UiSelector()" + ".textMatches(\"" + text + "\").instance(0))"));
    }
    
	public String checkLocationPermission(String modifiedXpath, boolean isUser) {
	    /* Check if any of the first two connected devices has Android version < 10 */
	    if(isUser){
	        return (Integer.parseInt(androidVersions.get(userDeviceIndex)) < 10) ? modifiedXpath + "2]" : modifiedXpath + "1]";
	    }else{
	        return (Integer.parseInt(androidVersions.get(driverDeviceIndex)) < 10) ? modifiedXpath + "2]" : modifiedXpath + "1]";
	    }
	}
	
	private String checkBatteryPermission(String modifiedXpath) {
	    /* Check if the brand name at index 1 is "google" or "Android" */
	    if ("google".equals(brandNames.get(driverDeviceIndex)) || ("Android".equals(brandNames.get(driverDeviceIndex)) || ("samsung".equals(brandNames.get(driverDeviceIndex))) || ("vivo".equals(brandNames.get(driverDeviceIndex))) || ("OPPO".equals(brandNames.get(driverDeviceIndex)) || ("iQOO".equals(brandNames.get(driverDeviceIndex)) || ("Realme".equals(brandNames.get(driverDeviceIndex)) || ("realme".equals(brandNames.get(driverDeviceIndex)) || ("OnePlus".equals(brandNames.get(driverDeviceIndex)) || ("Redmi".equals(brandNames.get(driverDeviceIndex))) || ("TECNO".equals(brandNames.get(driverDeviceIndex)))))))))) {
	        modifiedXpath += "2]"; /* Append "2]" to xpath */
	    }
        else if("POCO".equals(brandNames.get(driverDeviceIndex)) || "Redmi".equals(brandNames.get(driverDeviceIndex))){
            modifiedXpath = "//androidx.recyclerview.widget.RecyclerView/android.widget.LinearLayout[1]";
        }
        return modifiedXpath;
	}
	
	private boolean checkOverlayPermission() {
        int androidVersion = Integer.parseInt(androidVersions.get(driverDeviceIndex));
        String deviceResolution = resolutions.get(driverDeviceIndex);
        String deviceBrandString = brandNames.get(driverDeviceIndex);
        String modelNamesString = modelNames.get(driverDeviceIndex);
    
        /* Check if the Android version of the second connected device is greater than 10 */
        if ("SM-A032F".equals(modelNamesString)) {
                return false;
        }
        if (androidVersion <= 10) {
            return false;
        } else if (androidVersion > 10 && "1080x2400".equals(deviceResolution) && !"POCO".equals(deviceBrandString) && !"OPPO".equals(deviceBrandString)) {
            if ("Pixel 6a".equals(modelNamesString) || "google".equals(deviceBrandString)) {
                while (true) {
                    try {
                        driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='in.juspay.nammayatripartner.debug']")).click();
                        return true;
                        } catch (NoSuchElementException e) {
                        scrollToText("Namma Yatri Partner");
                        driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Namma Yatri Partner']")).click();
                        return true;
                    }
                }
            }
        }
        return true;
    }

    // Method to check if an element is present
    private boolean isElementPresent(By locator, WebDriver driver) {
        try {
                return driver.findElement(locator).isDisplayed();
            } catch (NoSuchElementException e) {
                return false;
            }
    }
   
	private boolean checkAutoStartPermission() {
	    /* Check if the brand name at index 1 is "google" or "Android" */
	    return (brandNames.get(driverDeviceIndex).equals("google") || brandNames.get(driverDeviceIndex).equals("Android") || brandNames.get(driverDeviceIndex).equals("vivo") || brandNames.get(driverDeviceIndex).equals("samsung") || brandNames.get(driverDeviceIndex).equals("OPPO") || brandNames.get(driverDeviceIndex).equals("iQOO") || brandNames.get(driverDeviceIndex).equals("Realme") || brandNames.get(driverDeviceIndex).equals("realme") || brandNames.get(driverDeviceIndex).equals("OnePlus") || brandNames.get(driverDeviceIndex).equals("Redmi") || brandNames.get(driverDeviceIndex).equals("TECNO"));
	}
    
    public void validateMobileNumberAndOtp(String state, String sendKeysValue, String screen, WebDriver driver) throws InterruptedException, IOException {
        /* Validating the length and mobile number entered is correct or not */
        if ("Enter Mobile Number".equals(state)) {
            String mobileNumber = sendKeysValue;
            String mobileNumberError = null;
            if (mobileNumber.length() != 10) {
                mobileNumberError = "Invalid mobile number : Length should be 10";
            } else if (mobileNumber.charAt(0) < '6') {
                mobileNumberError = "Invalid mobile number : First digit should be greater than or equal to 6";
            }

            if (mobileNumberError != null) {
                System.out.println(mobileNumberError);
                logErrorToAllureReport(mobileNumberError, driver, user, screenStatusMap);
            }
        }
	}
    
    public void languageScroll(String screen, String state, String xpath) {
        /* if any specific cases have to be performed */
        if ("Choose language".equals(state)) {
            boolean tamilDisplayed = driver.findElement(AppiumBy.xpath(xpath)).isDisplayed();
            while (!tamilDisplayed) {
                scrollToText("Tamil");
                tamilDisplayed = driver.findElement(AppiumBy.xpath(xpath)).isDisplayed();
            }
        }
        return;
    }
    
    public void cancelRide(String state, String xpath) throws InterruptedException {
    	if ("Draging bottom layout user".equals(state)) {
    	    By buttonLayoutLocator = By.xpath(xpath);
    	    WebElement source = user.findElement(buttonLayoutLocator);
    	    boolean cancelRideDisplayed = false;
    	    while (!cancelRideDisplayed) {
    	        try {
    	            WebElement cancelRideElement = user.findElement(By.xpath("//android.widget.TextView[@text='Cancel Ride']"));
    	            if (cancelRideElement.isDisplayed() && cancelRideElement.isEnabled()) {
    	                cancelRideDisplayed = true;
    	                cancelRideElement.click();  // Perform the desired action on the "Cancel Ride" element
    	            }
    	        } catch (NoSuchElementException | StaleElementReferenceException e) {
    	        }
    	        // Scroll the layout by dragging from source to destination coordinates
    	        ((JavascriptExecutor) user).executeScript("mobile: dragGesture", ImmutableMap.of(
    	                "elementId", ((RemoteWebElement) source).getId(),
    	                "endX", 447,
    	                "endY", 400
    	        ));
    	    }
    	}
    	
    	else if ("Draging bottom layout driver".equals(state)) {
            (isUser ? user : driver).manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    	    By buttonLayoutLocator = By.xpath(xpath);
    	    WebElement source = driver.findElement(buttonLayoutLocator);
    	    boolean cancelRideDisplayed = false;
    	    while (!cancelRideDisplayed) {
    	        try {
    	            WebElement cancelRideElement = driver.findElement(By.xpath("//android.widget.TextView[@text='Cancel Ride']"));
    	            if (cancelRideElement.isDisplayed() && cancelRideElement.isEnabled()) {
    	                cancelRideDisplayed = true;
    	                cancelRideElement.click();  // Perform the desired action on the "Cancel Ride" element
    	            }
    	        } catch (NoSuchElementException | StaleElementReferenceException e) {
    	        }
    	        // Scroll the layout by dragging from source to destination coordinates
    	        ((JavascriptExecutor) driver).executeScript("mobile: dragGesture", ImmutableMap.of(
    	                "elementId", ((RemoteWebElement) source).getId(),
    	                "endX", 123,
    	                "endY", 891
    	        ));
    	    }
    	}	
    	
        else if ("Hamburger Click".equals(state)) {
        	Thread.sleep(7000);
        	By buttonLayoutLocator = By.xpath(xpath);
            WebElement element = user.findElement(buttonLayoutLocator);
        }
    	
        else if ("Invoice Check".equals(state)) {
            String expected = "View Invoice";
            By buttonLayoutLocator = By.xpath(xpath);
            WebElement element = user.findElement(buttonLayoutLocator);
            String result = element.getText();
            try {
                Assert.assertEquals(result, expected, "Blocker!!! Invoice is found when the ride is canceled");
                System.out.println("Invoice found: " + expected);
            } catch (AssertionError e) {
                System.out.println("Invoice not found when the ride is canceled");
                return;
            }
        }
    	return;
    }
    
    public void customizedOtpKeyboardAction(String xpath, int interation, char[] number){
    	String xpath1 = "//android.widget.TextView[@text='";
        for (int i = 0; i < interation; i++) {
            driver.findElement(AppiumBy.xpath(xpath1 + number[i] + ("']"))).click();
            System.out.println(xpath + number[i] + ("']"));
        }
        driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='0']/../../../../android.widget.LinearLayout[4]/android.widget.LinearLayout[3]/android.widget.LinearLayout/android.widget.ImageView")).click();
    }
    
    
    
    public void customizedKeyboardAction(String xpath, int interation, char[] number){
        String xpath2 = "//android.widget.EditText";
        driver.findElement(AppiumBy.xpath(xpath2)).click();
        for (int i = 0; i < interation; i++) {
            char digit = number[i];
            AndroidKey key = AndroidKey.valueOf("DIGIT_" + digit);
            ((AndroidDriver) driver).pressKey(new KeyEvent(key));
            System.out.println("//android.widget.EditText[@text='" + number[i] + ("']"));
        }
        driver.pressKey(new KeyEvent(AndroidKey.BACK));
        driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Update']")).click();
    }
    
    
    private char[] generateNewMobNumber() {
        long randomNumber2 = (long) (Math.random() * 900000000L) + 100000000L;
            String phoneNumber2 = Long.toString(randomNumber2);
            String phoneNumberSecond = "9" + phoneNumber2;
            return phoneNumberSecond.toCharArray();
    }
    
    public void alternateMobileNumberValidation(String state, String xpath) throws IOException, InterruptedException  {
    	if ("Check Alternate Number in Homescreen".equals(state)) {
    	    try {
    	        if (driver.findElement(AppiumBy.xpath(xpath)).isDisplayed()) {
                    System.out.println("Is Displayed");
                    driver.findElement(AppiumBy.xpath(xpath)).click();
    	        }
    	    }
    	    catch (NoSuchElementException e) {
                System.out.println("Alternative number already added");
                driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Offline']/../../../../android.widget.LinearLayout[1]")).click();
                driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Driver Details']")).click();
                driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Alternate Number']/../../android.widget.LinearLayout[1]/android.widget.ImageView")).click();
                driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Remove']")).click();
                driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Yes, Remove It']")).click();
                driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Alternate Number']/../../android.widget.LinearLayout[1]/android.widget.TextView[2]")).click();
    	    }
            return;
    	}
        else if(state.contains("Add Alternate Number")){
            // Enter the first random phone number
            if(state.contains("1")){
                firstAltNumber = generateNewMobNumber();
                customizedKeyboardAction(xpath, 10, firstAltNumber);
                Thread.sleep(2000);
                // Enter the wrong OTP
                char[] wrongOtp = {'7', '8', '9', '5'};
                // Enter the wrong OTP digit by digit
                customizedOtpKeyboardAction(xpath, 4, wrongOtp);
                Thread.sleep(2000);
                // Enter the correct OTP
                char[] correctOtp = {'7', '8', '9', '1'};
                // Enter the correct OTP digit by digit
                customizedOtpKeyboardAction(xpath, 4, correctOtp);
                Thread.sleep(2000);
            }
            else if(state.contains("2")){
                customizedKeyboardAction(xpath, 10, firstAltNumber);
                Thread.sleep(2000);
            }
        }
        return;
    }
    
    
    /* When the build is failed 
     * Attaching UI errors, API errors, screenshots, logs and the screen wise status */
    public static void logErrorToAllureReport(String errorMessage, WebDriver driver, WebDriver user, Map<String, String> screenStatusMap) throws IOException {
        ReportFlag = !ReportFlag;
        if (ReportFlag) {
            return;
        }
        Allure.addAttachment("Error Message", errorMessage);

        StringBuilder screensInfo = new StringBuilder();
        /* Determine the maximum lengths of the columns for alignment */
        int maxIsUserLength = 0;
        int maxTestCaseLength = 0;
        int maxScreenLength = 0;
        int maxStateLength = 0;
        int maxEpochTimeLength = 0;
        int maxActionTimeLength = 0;
        // Using TreeMap for sorting based on testCase
        Map<String, String> sortedMap = new TreeMap<>(screenStatusMap);

        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            String key = entry.getKey();
            String status = entry.getValue();
            String[] keyParts = key.split("\\|");
            String epochTime = keyParts[0];
            String actionTime = keyParts[1];
            String whichApp = keyParts[2];
            String testCase = keyParts[3];
            String screen = keyParts[4];
            String state = keyParts[5];

            maxIsUserLength = Math.max(maxIsUserLength, whichApp.length());
            maxTestCaseLength = Math.max(maxTestCaseLength, testCase.length());
            maxScreenLength = Math.max(maxScreenLength, screen.length());
            maxStateLength = Math.max(maxStateLength, state.length());
            maxEpochTimeLength = Math.max(maxEpochTimeLength, epochTime.length());
            maxActionTimeLength = Math.max(maxActionTimeLength, String.valueOf(actionTime).length());
        }

        /* Build the table format */
        String format = "%-" + (maxEpochTimeLength + 3) + "s%-" + (maxActionTimeLength + 4) + "s%-" +
                (maxIsUserLength + 5) + "s%-" + (maxTestCaseLength + 3) + "s%-" + (maxScreenLength + 3) + "s%-" + (maxStateLength + 3) + "s%s%n";

        /* Add the table header */
        String header = String.format(format, "TIME STAMP ", "TIME TAKEN ", "APPTYPE ", "TESTCASES ", "SCREEN ", "ACTION ", "STATUS");
        screensInfo.append(header);

        /* Add the table rows */
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            String key = entry.getKey();
            String status = entry.getValue();
            String[] keyParts = key.split("\\|");
            String epochTime = keyParts[0];
            String actionTime = keyParts[1];
            String whichApp = keyParts[2];
            String testCase = keyParts[3];
            String screen = keyParts[4];
            String state = keyParts[5];

            String row = String.format(format, epochTime, actionTime, whichApp, testCase, screen, state, status);
            screensInfo.append(row);
        }
        /* Print or use the screensInfo StringBuilder as desired */
        System.out.println(screensInfo.toString());
        Allure.addAttachment("Screens Status", screensInfo.toString());
        if (driver != null) {
            Allure.addAttachment("Driver App Screenshot", new ByteArrayInputStream(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES)));
        } else {
            String message = "Driver application is not initialized for testing";
            Allure.addAttachment("Driver App Screenshot", message);
        }

        if (user != null) {
            Allure.addAttachment("User App Screenshot", new ByteArrayInputStream(((TakesScreenshot) user).getScreenshotAs(OutputType.BYTES)));
        } else {
            String message = "User application is not initialized for testing";
            Allure.addAttachment("User App Screenshot", message);
        }
        Allure.getLifecycle().updateTestCase(testResult -> testResult.setStatus(Status.FAILED));
        addDeviceConfigToReport();
        LogcatToFile.fetchAppDetails();
        LogcatToFile.searchApiErr();
        LogcatToFile.searchJavaScriptError();
        LogcatToFile.fetchExceptions();
    }
    
    /* When the build is passed 
     * Attaching the success message and the screen wise status */
    public void logPassToAllureReport(String successMessage, WebDriver driver, WebDriver user, Map<String, String> screenStatusMap) throws IOException {
        Allure.addAttachment("Success Message", successMessage);
        
        StringBuilder screensInfo = new StringBuilder();
        /* Determine the maximum lengths of the columns for alignment */
        int maxIsUserLength = 0;
        int maxTestCaseLength = 0;
        int maxScreenLength = 0;
        int maxStateLength = 0;
        int maxEpochTimeLength = 0;
        int maxActionTimeLength = 0;
        // Using TreeMap for sorting based on testCase
        Map<String, String> sortedMap = new TreeMap<>(screenStatusMap);

        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            String key = entry.getKey();
            String status = entry.getValue();
            String[] keyParts = key.split("\\|");
            String epochTime = keyParts[0];
            String actionTime = keyParts[1];
            String whichApp = keyParts[2];
            String testCase = keyParts[3];
            String screen = keyParts[4];
            String state = keyParts[5];

            maxIsUserLength = Math.max(maxIsUserLength, whichApp.length());
            maxTestCaseLength = Math.max(maxTestCaseLength, testCase.length());
            maxScreenLength = Math.max(maxScreenLength, screen.length());
            maxStateLength = Math.max(maxStateLength, state.length());
            maxEpochTimeLength = Math.max(maxEpochTimeLength, epochTime.length());
            maxActionTimeLength = Math.max(maxActionTimeLength, String.valueOf(actionTime).length());
        }

        /* Build the table format */
        String format = "%-" + (maxEpochTimeLength + 3) + "s%-" + (maxActionTimeLength + 4) + "s%-" +
                (maxIsUserLength + 5) + "s%-" + (maxTestCaseLength + 3) + "s%-" + (maxScreenLength + 3) + "s%-" + (maxStateLength + 3) + "s%s%n";

        /* Add the table header */
        String header = String.format(format, "TIME STAMP", "TIME TAKEN", "APPTYPE", "TESTCASES", "SCREEN", "STATE", "STATUS");
        screensInfo.append(header);

        /* Add the table rows */
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            String key = entry.getKey();
            String status = entry.getValue();
            String[] keyParts = key.split("\\|");
            String epochTime = keyParts[0];
            String actionTime = keyParts[1];
            String whichApp = keyParts[2];
            String testCase = keyParts[3];
            String screen = keyParts[4];
            String state = keyParts[5];

            String row = String.format(format, epochTime, actionTime, whichApp, testCase, screen, state, status);
            screensInfo.append(row);
        }
        /* Print or use the screensInfo StringBuilder as desired */
        System.out.println(screensInfo.toString());

        Allure.addAttachment("Screens Status", screensInfo.toString());
        Allure.getLifecycle().updateTestCase(testResult -> testResult.setStatus(Status.PASSED));
        addDeviceConfigToReport();
        LogcatToFile.fetchAppDetails();
        LogcatToFile.searchApiErr();
        LogcatToFile.searchJavaScriptError();
        LogcatToFile.fetchExceptions();
    }
    
    public static String runAllureServe(String allurePath, String resultsPath) {
        String reportUrl = null;
        String lastReportUrl = null; // Store the last parsed report URL
        try {
            // Specify the command to execute
            String command = allurePath + " serve " + System.getProperty("user.dir") + File.separator + resultsPath;
            // Execute the command
            Process process = Runtime.getRuntime().exec(command);
            // Read the output from the process to get the report URL
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                reportUrl = parseReportUrl(line);
                if (reportUrl != null) {
                    lastReportUrl = reportUrl; // Update the last known report URL
                    System.out.println("Report URL: " + reportUrl);
                }
            }
            // Wait for the process to complete
            int exitCode = process.waitFor();
            // Print the exit code
            System.out.println("Allure serve command executed. Exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return lastReportUrl; // Return the last parsed report URL
    }
    
    private static String parseReportUrl(String line) {
        String reportUrl = null;
        Pattern pattern = Pattern.compile("<(http://[^>]+)>");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            reportUrl = matcher.group(1);
        }
        return reportUrl;
    }
    
    
    /* Method is executed after the test suite finishes and quits the WebDriver instances for both user and driver */
    @AfterSuite
    public void tearDown() throws IOException {
        if (user != null) {
            user.quit();
        } if (driver != null) {
            driver.quit();
        }
        // String allurePath = "/opt/homebrew/bin/allure";
        String allurePath = "/usr/local/bin/allure";
        String resultsPath = "allure-results";
        runAllureServe(allurePath, resultsPath);
    }
}