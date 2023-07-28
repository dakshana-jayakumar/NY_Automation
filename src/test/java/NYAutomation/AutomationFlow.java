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
import org.xmlpull.v1.sax2.Driver;



public class AutomationFlow extends BaseClass {

    private String rideOtp = "";

    static Map<String, String> screenStatusMap = new HashMap<>();

	private final String userMobileNumber = "9876543213";
	private final String driverMobileNumber = "9999999916";
    private static boolean ReportFlag = true;
    private static char[] firstAltNumber;
    long prevTimeStamp;
    long currentTimeStamp;
    String formattedTimeDifference = "";
    

    @Test
    @Epic("Allure Results")
    @Feature("TestNG support")
    @Story("Application flow")
    /* Creating a method for overall flow of the applications */
    public void flow() throws Exception {
    	/* Add Allure report cleanup code here */
    	cleanupAllureReport();
    	/* Fetch test data from Google Sheet */
        TestDataReader.fetchTabNames();
        ADBDeviceFetcher.fetchAdbDeviceProperties();
        String[][] testData = TestDataReader.testData.toArray(new String[0][0]);
        boolean userFlag = true;
        boolean driverFlag = true;
        boolean isUser = false;
        LogcatToFile.CaptureLogs();

        // SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        prevTimeStamp = System.currentTimeMillis();

        for (String[] actionParameter : testData) {
            String testCase = actionParameter[0];
            String screen = actionParameter[1];
            String state = actionParameter[2];
            String xpath = actionParameter[3];
            String sendKeysValue = actionParameter[4];
            isUser = "user".equals(actionParameter[5]);
            String whichApp = actionParameter[5];
           

            if (userFlag && isUser) {
                userFlag = false;
                setup(isUser);
            } else if (driverFlag && !isUser) {
                driverFlag = false;
                setup(isUser);
            }
            System.out.println("EpochTime: " + currentTimeStamp + " | ActionTime: " + formattedTimeDifference + " | screen: " + screen + " | state: " + state + " | XPath: " + xpath + " | SendKeys Value: " + sendKeysValue);
            try {
            	checkCase(testCase, screen, state, xpath, sendKeysValue, isUser);
            	screenStatusMap.put(currentTimeStamp + "|" + formattedTimeDifference + "|" + whichApp + "|" + testCase + "|" + screen + "|" + state, "Passed");
            }
            catch (Exception e) {
                screenStatusMap.put(currentTimeStamp + "|" + formattedTimeDifference + "|" + whichApp + "|" + testCase + "|" + screen + "|" + state, "Failed");
            	logErrorToAllureReport(e.getMessage(), androidDriver, androidUser, screenStatusMap);
                // Thread.sleep(3000);
                throw e;
            }
        }
        logPassToAllureReport("Build Passed!", androidDriver, androidUser, screenStatusMap);
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
        
        for (int i = 0; i < DeviceIndex; i++) {
            String device = i < devices.size() ? devices.get(i) : "N/A";
            String brand = i < brandNames.size() ? brandNames.get(i) : "N/A";
            String model = i < modelNames.size() ? modelNames.get(i) : "N/A";
            String version = i < androidVersions.size() ? androidVersions.get(i) : "N/A";
            String resolution = i < resolutions.size() ? resolutions.get(i) : "N/A";
            devicesConfig.append(String.format("%-20s | %-15s | %-30s | %-15s | %-15s%n", device, brand, model, version, resolution));
        }
        Allure.addAttachment("Devices Config", devicesConfig.toString());
    }

    private void cleanupAllureReport() {
        // Specify the directory path where the Allure report files are located
        String reportDirectoryPath = System.getProperty("user.dir") + File.separator + "allure-results";

        // Create a File object for the directory
        File reportDirectory = new File(reportDirectoryPath);

        // Check if the directory exists
        if (reportDirectory.exists() && reportDirectory.isDirectory()) {
            // Prompt the user for confirmation
            System.out.print("Do you want to delete the files in the Allure report directory? (yes/no): ");

            // Read user input
            Scanner scanner = new Scanner(System.in);
            String userInput = scanner.nextLine();

            // Check user response
            if (userInput.equalsIgnoreCase("yes")) {
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
        	/* Alter the mobile numbers by individual testers according to their use cases */
    		if (isUser) {
         		sendKeysValue = userMobileNumber;
         		}
         	else {
         		sendKeysValue = driverMobileNumber;
         		}
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

        else if("Overlay screen Back Icon".equals(state)){
            int androidVersion = Integer.parseInt(androidVersions.get(DriverDeviceIndex));
            if ((androidVersion == 10) || (androidVersion <= 8) || (androidVersion == 12)) {
                KeyEvent appSwitcherKeyEvent = new KeyEvent(AndroidKey.BACK);
    	        (isUser ? androidUser : androidDriver).pressKey(appSwitcherKeyEvent);
                return;
    	    }
        }

        else if ("Allow Battery Optimization".equals(state)) {
            /* If the state is "Allow Battery Optimization" */
            /* Call the checkBatteryPermission method to modify the xpath value */
            xpath = checkBatteryPermission(xpath);
        }

        else if ("AutoStart".equals(state)) {
        	int androidVersion = Integer.parseInt(androidVersions.get(DriverDeviceIndex));
    	    if (androidVersion == 10) {
    	        Thread.sleep(2000);
                KeyEvent appSwitcherKeyEvent = new KeyEvent(AndroidKey.BACK);
    	        (isUser ? androidUser : androidDriver).pressKey(appSwitcherKeyEvent);
            }
        }

        else if ("AutoStart Screen Back Icon".equals(state) && checkAutoStartPermission()) {
            /* If the state is "AutoStart Screen Back Icon" and checkAutoStartPermission is true */
            /* Return from the current method or function */
            return;
        }

        else if ("Back Press".equals(state)) {
            Thread.sleep(5000);
            (isUser ? androidUser : androidDriver).pressKey(new KeyEvent(AndroidKey.BACK));
            return;
        }

        else if ("Scroll Function".equals(state)) {
        	int androidVersion = Integer.parseInt(androidVersions.get(UserDeviceIndex));
        	By buttonLayoutLocator = By.xpath(xpath);
            WebElement source = androidUser.findElement(buttonLayoutLocator);
        	if (androidVersion == 9 || androidVersion == 10 || androidVersion == 11 || androidVersion == 12 || androidVersion == 13) {
        		boolean canScrollMore = (Boolean)androidUser.executeScript("mobile: scrollGesture", ImmutableMap.of(
        				"left", 100, "top", 100, "width", 1200, "height", 1200,
        				"direction", "down",
        				"percent", 3.0
        				));
            	Thread.sleep(3000);
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
                    TouchAction<?> touchAction = new TouchAction<>(androidUser);
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
        	/* Fetching OTP digits */
            for (int i = 1; i <= 4; i++) {
                rideOtp = rideOtp + wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath + "/../android.widget.LinearLayout/android.widget.LinearLayout[" + i + "]/android.widget.TextView"))).getText();
            }
            System.out.println("Ride Otp = " + rideOtp);

            char[] otp = rideOtp.toCharArray();
            /* Entering OTP digits */
            for (int i = 0; i < 4; i++) {
                char digit = otp[i];
                System.out.println("Otp Digit = " + digit);
                String xpath2 = "//android.widget.TextView[@text='Please ask the customer for the OTP']/../../../android.widget.LinearLayout[2]/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.TextView[@text='" + digit + "']";
                androidDriver.findElement(AppiumBy.xpath(xpath2)).click();
            }
            System.out.println("Ride Otp = " + rideOtp);

            Thread.sleep(2000);
            return;
        }

        else if ("Google Map".equals(state)) {
    	    	int loopCount = 2; // Number of times to loop

    	    	for (int i = 0; i < loopCount; i++) {
    	        Thread.sleep(2000);
    	        /* Simulate the App Switcher (Recent Apps) key */
    	        KeyEvent appSwitcherKeyEvent = new KeyEvent(AndroidKey.APP_SWITCH);
    	        (isUser ? androidUser : androidDriver).pressKey(appSwitcherKeyEvent);
    	    }
    	    return;
    	}

        else if ("Home Press".equals(state)) {
            Thread.sleep(2000);
            (isUser ? androidUser : androidDriver).pressKey(new KeyEvent(AndroidKey.HOME));
            return;
        }
        else if ("SwipeFromUp".equals(state)) {
            Thread.sleep(2000);

            /* Perform the pull down the notifications */
            androidUser.openNotifications();

            /* Perform a click action on the element */
            Thread.sleep(2000);
            androidUser.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Invoice Downloaded']")).click();
            Thread.sleep(5000);
            return;
        }

        else if ("Swipe".equals(state)) {
            int loopCount = 2;
            for (int i = 0; i < loopCount; i++) {
                By buttonLayoutLocator = By.xpath(xpath);
                WebElement element = androidUser.findElement(buttonLayoutLocator);

                // Get the size of the element
                Dimension size = element.getSize();
                int startX = size.width / 2;
                int startY = size.height / 2;

                // Calculate the end coordinates for the swipe gesture
                int endX = (int) (startX - size.width * 0.70);
                int endY = startY;

                // Perform the swipe gesture
                TouchAction<?> touchAction = new TouchAction<>(androidUser);
                touchAction.press(PointOption.point(startX, startY))
                        .waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000)))
                        .moveTo(PointOption.point(endX, endY))
                        .release()
                        .perform();
            }
            return;
        }

        else if ("Recenter Button".equals(state)) {
            int androidVersion = Integer.parseInt(androidVersions.get(UserDeviceIndex));
            if(androidVersion <= 7){
                Thread.sleep(5000);
                return;
            }
        	int loopCount = 3; // Number of times to loop
	    	for (int i = 0; i < loopCount; i++) {
	    		androidUser.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Where to?']/../../../../../android.widget.LinearLayout/android.widget.LinearLayout[1]/android.widget.LinearLayout[2]/android.widget.ImageView")).click();
	    	}
        	Thread.sleep(5000);
        	return;
        }
        
        else if (("Allow Permission".equals(state)) && ("12".equals(androidVersions.get(UserDeviceIndex)) || ("13".equals(androidVersions.get(UserDeviceIndex))))){return;}
        
        else if ("Favourite update toast".equals(state)) {
        	String ToastMessage = androidUser.findElement(By.xpath("(//android.widget.Toast)[1]")).getAttribute("name");
    		Assert.assertEquals(ToastMessage, "Favourite Updated Successfully");
    		System.out.println(ToastMessage);
    		return;
        }
        else if ("Location exists toast".equals(state)) {
        	String ToastMessage1 = androidUser.findElement(By.xpath("(//android.widget.Toast)[1]")).getAttribute("name");

    		Assert.assertEquals(ToastMessage1, "location already exists");
    		System.out.println("Validated Toast:"+ ToastMessage1);
    		return;
        }
        else if ("Home location toast".equals(state)) {
        	String ToastMessage2 = androidUser.findElement(By.xpath("(//android.widget.Toast)[1]")).getAttribute("name");
    		Assert.assertEquals(ToastMessage2, "Home location already exists");
    		System.out.println("Validated Toast:"+ ToastMessage2);
    		Thread.sleep(3000);
    		return;
        }
        else if ("Work location toast".equals(state)) {
        	String ToastMessage3 = androidUser.findElement(By.xpath("(//android.widget.Toast)[1]")).getAttribute("name");
    		Assert.assertEquals(ToastMessage3, "Work location already exists");
    		System.out.println("Validated Toast:"+ ToastMessage3);
    		Thread.sleep(3000);
    		return;
        }
        else if ("Error msg".equals(state)) {
        	By buttonLayoutLocator = By.xpath(xpath);
        	String PopUp = androidUser.findElement(buttonLayoutLocator).getText();
    		System.out.println("Error message : " + PopUp);
    		return;
        }
        else if ("Clear text".equals(state)) {
        	By buttonLayoutLocator = By.xpath(xpath);
        	androidUser.findElement(buttonLayoutLocator).clear();
        	return;
        }
        else if ("Favourite added toast".equals(state)) {
        	String ToastMessage4 = androidUser.findElement(By.xpath("(//android.widget.Toast)[1]")).getAttribute("name");
    		Assert.assertEquals(ToastMessage4, "Favourite Added Successfully");
    		System.out.println("Validated Toast:"+ ToastMessage4);
    		System.out.println("-----------------FAVOURITE TESTCASES DONE-------------------------------");
    		return;
        }
    	
        /* Test cases for 36 search location combinations */
        else if ("Sleep Time".equals(state)) {
        	By buttonLayoutLocator = By.xpath(xpath);
        	androidUser.findElement(buttonLayoutLocator).click();
        	Thread.sleep(4000);
        	return;
        }

        else if ("Book Ride Check".equals(state)) {
            try {
                System.out.println("inside if");
                WebElement bookRideElement = androidUser.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Book Ride']"));
                if (bookRideElement.isDisplayed()) {
                    System.out.println("Is Displayed");
                    bookRideElement.click();
                } else {
                    System.out.println("inside else");
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
        	androidUser.pressKey(new KeyEvent(AndroidKey.BACK));
        	Thread.sleep(6000);
        	return;
        }
    	
        else if ("Click Fav".equals(state)) {
        	androidUser.findElement(AppiumBy.xpath("//android.widget.TextView[@text='All Favourites']")).click();
            List<WebElement> favList = androidUser.findElements(AppiumBy.xpath("//android.widget.TextView[@text='Select Favourite']/../../android.widget.ScrollView/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.TextView"));
            System.out.println("Favlist Size is _" + favList.size());
            for (int n = 0; n < favList.size(); n++) {
                if (n == 2) {
                    favList.get(n).click();
                }
            }
            Thread.sleep(4000);
            androidUser.pressKey(new KeyEvent(AndroidKey.BACK));
            System.out.println("Case_3_All_Favourites_Executed");
            return;
        }
    	
        else if ("Click Dest Fav".equals(state)) {
        	androidUser.findElement(AppiumBy.xpath("//android.widget.TextView[@text='All Favourites']")).click();
            List<WebElement> favList = androidUser.findElements(AppiumBy.xpath("//android.widget.TextView[@text='Select Favourite']/../../android.widget.ScrollView/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.TextView"));
            System.out.println("Favlist Size is _" + favList.size());
            for (int n = 0; n < favList.size(); n++) {
                if (n == 2) {
                    favList.get(n).click();
                }
            }
            Thread.sleep(2000);
            androidUser.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Book Ride']")).click();
            Thread.sleep(4000);
            androidUser.pressKey(new KeyEvent(AndroidKey.BACK));
            System.out.println("Case_3_All_Favourites_Executed");
            return;
        }
    	
        else if ("Dest Auto Suggestion".equals(state)) {
        	((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.U));
            ((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.L));
            ((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.S));
            ((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.O));
            ((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.BACK));
            return;
        }
    	
        else if ("Source Auto Suggestion".equals(state)) {
        	((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.M));
            ((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.A));
            ((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.J));
            ((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.E));
            
            ((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.BACK));
            return;
        }

    	/* Validating the otp entered is correct or not */
        else if ("Login OTP".equals(state)) {
    	    androidUser.findElement(AppiumBy.xpath("//android.widget.EditText[@text='Enter 4 digit OTP']")).click();
            
        	((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.DIGIT_7));
        	((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.DIGIT_8));
        	((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.DIGIT_9));
        	((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.DIGIT_5));
        	
        	androidUser.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Resend']")).click();
        	Thread.sleep(16000);
        	
        	((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.DIGIT_7));
        	((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.DIGIT_8));
        	((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.DIGIT_9));
        	((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.DIGIT_1));
        	
        	return;
        }
    	
        else if ("Driver Login OTP".equals(state)) {
    		androidDriver.findElement(AppiumBy.xpath("//android.widget.EditText[@text='Auto Reading OTP...']")).click();
            
    		Thread.sleep(3000);
    		
        	((AndroidDriver) androidDriver).pressKey(new KeyEvent(AndroidKey.DIGIT_7));
        	((AndroidDriver) androidDriver).pressKey(new KeyEvent(AndroidKey.DIGIT_8));
        	((AndroidDriver) androidDriver).pressKey(new KeyEvent(AndroidKey.DIGIT_9));
        	((AndroidDriver) androidDriver).pressKey(new KeyEvent(AndroidKey.DIGIT_5));
        	
        	Thread.sleep(12000);
        	
        	androidDriver.findElement(AppiumBy.xpath("//android.widget.EditText")).click();
        	
        	((AndroidDriver) androidDriver).pressKey(new KeyEvent(AndroidKey.DEL));
        	((AndroidDriver) androidDriver).pressKey(new KeyEvent(AndroidKey.DEL));
        	((AndroidDriver) androidDriver).pressKey(new KeyEvent(AndroidKey.DEL));
        	((AndroidDriver) androidDriver).pressKey(new KeyEvent(AndroidKey.DEL));

        	Thread.sleep(2000);

        	((AndroidDriver) androidDriver).pressKey(new KeyEvent(AndroidKey.DIGIT_7));
        	((AndroidDriver) androidDriver).pressKey(new KeyEvent(AndroidKey.DIGIT_8));
        	((AndroidDriver) androidDriver).pressKey(new KeyEvent(AndroidKey.DIGIT_9));
        	((AndroidDriver) androidDriver).pressKey(new KeyEvent(AndroidKey.DIGIT_1));
        	return;
    	}
        
        /* Test cases to validate the Driver status mode */
    	else if ("Driver Validation".equals(state)) {
            /* Driver status mode validation test case */
    		try {
    			if (androidDriver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='GO!']")).isDisplayed()) {
    				System.out.println("Driver is in offline mode");
    				androidDriver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='GO!']")).click();
                    System.out.println("Driver changed to Online mode");
    			}
    		} catch (NoSuchElementException e) {

    			try {
    				if (androidDriver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Silent']/../android.widget.ImageView")).isDisplayed()) {
    					System.out.println("Driver is in Silent mode");
    					androidDriver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Online']")).click();
                        System.out.println("Driver changed to Online mode");
    				}
    			} catch (NoSuchElementException e1) {
    				System.out.println("Driver is in Online mode");
    			}
    		}
    		return;
    	}
    	
    	
    	else if (("Writing user reason".equals(state)) || ("Writing driver reason".equals(state))) {
        	((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.S));
            ((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.O));
            ((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.R));
            ((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.R));
            ((AndroidDriver) androidUser).pressKey(new KeyEvent(AndroidKey.Y));
        	return;
        }

    	else if ("Waiting Time".equals(state)) {
        	Thread.sleep(5000);
        	return;
        }

        while ("Remove all the fav".equals(state)) {
    	    try {
    	        if (androidUser.findElement(AppiumBy.xpath(xpath)).isDisplayed()) {
                    System.out.println("Is Displayed");
                    androidUser.findElement(AppiumBy.xpath(xpath)).click();
                    androidUser.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Yes, Remove']")).click();
    	        }
    	    }
    	    catch (NoSuchElementException e) {
                System.out.println("Removed all the fav addresses");
                return;
    	    }
    	}
        
         /* Function calls for both Customer and Driver */   
        {
	        /* Function call to validate the mobile number and otp is entered correct */
	        validateMobileNumberAndOtp(state, sendKeysValue, screen, androidDriver);
	        
	        /* Function for checking cancel ride for both user and driver */
	        cancelRide(state, xpath);
        }
              
        
        /* Function calls only in driver application */
        {
	        /* Function for checking scroll in choose language screen in driver */
	        languageScroll(screen, state);
	        
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
        Wait<AndroidDriver> wait = new FluentWait<>(isUser ? androidUser : androidDriver)
                .withTimeout(Duration.ofSeconds(60))
                .pollingEvery(Duration.ofMillis(3000))
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
        androidDriver.findElement(new AppiumBy.ByAndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true).instance(0))"
    				+ ".scrollIntoView(new UiSelector()" + ".textMatches(\"" + text + "\").instance(0))"));
    }
    

	public String checkLocationPermission(String modifiedXpath, boolean isUser) {
	    /* Check if any of the first two connected devices has Android version < 10 */
	    if(isUser){
	        return (Integer.parseInt(androidVersions.get(UserDeviceIndex)) < 10) ? modifiedXpath + "2]" : modifiedXpath + "1]";
	    }else{
	        return (Integer.parseInt(androidVersions.get(DriverDeviceIndex)) < 10) ? modifiedXpath + "2]" : modifiedXpath + "1]";
	    }
	}
	
	private String checkBatteryPermission(String modifiedXpath) {
	    /* Check if the brand name at index 1 is "google" or "Android" */
	    if ("google".equals(brandNames.get(DriverDeviceIndex)) || ("Android".equals(brandNames.get(DriverDeviceIndex)) || ("samsung".equals(brandNames.get(DriverDeviceIndex))) || ("vivo".equals(brandNames.get(DriverDeviceIndex))))) {
	        modifiedXpath += "2]"; /* Append "2]" to xpath */
	    }
        else if("POCO".equals(brandNames.get(DriverDeviceIndex)) || "Redmi".equals(brandNames.get(DriverDeviceIndex))){
            modifiedXpath = "//androidx.recyclerview.widget.RecyclerView/android.widget.LinearLayout[1]";
        }
        return modifiedXpath;
	}
	
	private boolean checkOverlayPermission() {
	    int androidVersion = Integer.parseInt(androidVersions.get(DriverDeviceIndex));
        String deviceResolution = resolutions.get(DriverDeviceIndex);
        String deviceBrandString = brandNames.get(DriverDeviceIndex);
	    /* Check if the Android version of the second connected device is greater than 10 */
	    if ((androidVersion <= 10) ){return false;}
	    else if((androidVersion > 10) && ("1080x2400".equals(deviceResolution)) && (!"POCO".equals(deviceBrandString))) {return true;}
        scrollToText("Namma Yatri Partner");
        return true;
	}
   
	private boolean checkAutoStartPermission() {
	    /* Check if the brand name at index 1 is "google" or "Android" */
	    return (brandNames.get(DriverDeviceIndex).equals("google") || brandNames.get(DriverDeviceIndex).equals("Android") || brandNames.get(DriverDeviceIndex).equals("vivo"));
	}
    
    public void validateMobileNumberAndOtp(String state, String sendKeysValue, String screen, WebDriver driver) throws InterruptedException, IOException {
        /* Validating the length and mobile number entered is correct or not */
        if ("Enter Mobile Number".equals(state)) {
            String mobileNumber = sendKeysValue;
            Thread.sleep(2000);

            String mobileNumberError = null;
            if (mobileNumber.length() != 10) {
                mobileNumberError = "Invalid mobile number : Length should be 10";
            } else if (mobileNumber.charAt(0) < '6') {
                mobileNumberError = "Invalid mobile number : First digit should be greater than or equal to 6";
            }

            if (mobileNumberError != null) {
                System.out.println(mobileNumberError);
                logErrorToAllureReport(mobileNumberError, driver, androidUser, screenStatusMap);
            }
        }
	}
    
    public void languageScroll(String screen, String state) {
    	/* if any specific cases have to be performed */
        if ("Choose Language".equals(screen) && ("Kannada".equals(state)) && !"Update Language".equals(screen) && !("1080x2400".equals(resolutions.get(DriverDeviceIndex)))) {
            scrollToText("Tamil");
        }
    }
    
    public void cancelRide(String state, String xpath) throws InterruptedException {
    	
    	if ("Draging bottom layout user".equals(state)) {
    	    By buttonLayoutLocator = By.xpath(xpath);
    	    WebElement source = androidUser.findElement(buttonLayoutLocator);
    	    boolean cancelRideDisplayed = false;

    	    while (!cancelRideDisplayed) {
    	        try {
    	            WebElement cancelRideElement = androidUser.findElement(By.xpath("//android.widget.TextView[@text='Cancel Ride']"));
    	            if (cancelRideElement.isDisplayed() && cancelRideElement.isEnabled()) {
    	                cancelRideDisplayed = true;
    	                cancelRideElement.click();  // Perform the desired action on the "Cancel Ride" element
    	            }
    	        } catch (NoSuchElementException | StaleElementReferenceException e) {
    	        }

    	        // Scroll the layout by dragging from source to destination coordinates
    	        ((JavascriptExecutor) androidUser).executeScript("mobile: dragGesture", ImmutableMap.of(
    	                "elementId", ((RemoteWebElement) source).getId(),
    	                "endX", 447,
    	                "endY", 400
    	        ));
       	        Thread.sleep(2000);
    	    }
    	}
    	
    	else if ("Draging bottom layout driver".equals(state)) {
    	    By buttonLayoutLocator = By.xpath(xpath);
    	    WebElement source = androidDriver.findElement(buttonLayoutLocator);
    	    boolean cancelRideDisplayed = false;

    	    while (!cancelRideDisplayed) {
    	        try {
    	            WebElement cancelRideElement = androidDriver.findElement(By.xpath("//android.widget.TextView[@text='Cancel Ride']"));
    	            if (cancelRideElement.isDisplayed() && cancelRideElement.isEnabled()) {
    	                cancelRideDisplayed = true;
    	                cancelRideElement.click();  // Perform the desired action on the "Cancel Ride" element
    	            }
    	        } catch (NoSuchElementException | StaleElementReferenceException e) {
    	        }

    	        // Scroll the layout by dragging from source to destination coordinates
    	        ((JavascriptExecutor) androidDriver).executeScript("mobile: dragGesture", ImmutableMap.of(
    	                "elementId", ((RemoteWebElement) source).getId(),
    	                "endX", 123,
    	                "endY", 891
    	        ));
    	        Thread.sleep(2000);
    	    }
    	}	
    	
        else if ("Hamburger Click".equals(state)) {
        	Thread.sleep(7000);
        	By buttonLayoutLocator = By.xpath(xpath);
            WebElement element = androidUser.findElement(buttonLayoutLocator);
        }

        else if ("Invoice Check".equals(state)) {
            String expected = "View Invoice";
            By buttonLayoutLocator = By.xpath(xpath);
            WebElement element = androidUser.findElement(buttonLayoutLocator);
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
    
    public void customizedKeyboardAction(String xpath, int interation, char[] number){
        for (int i = 0; i < interation; i++) {
            androidDriver.findElement(AppiumBy.xpath(xpath + number[i] + ("']"))).click();
            System.out.println(xpath + number[i] + ("']"));
        }
        androidDriver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='0']/../../../../android.widget.LinearLayout[4]/android.widget.LinearLayout[3]/android.widget.LinearLayout/android.widget.ImageView")).click();
    }
    
    private char[] generateNewMobNumber() {
        long randomNumber2 = (long) (Math.random() * 900000000L) + 100000000L;
            String phoneNumber2 = Long.toString(randomNumber2);
            String phoneNumberSecond = "9" + phoneNumber2;
            return phoneNumberSecond.toCharArray();
    }
    
    public void alternateMobileNumberValidation(String state, String xpath) throws IOException  {
    	if ("Check Alternate Number in Homescreen".equals(state)) {
    	    try {
    	        if (androidDriver.findElement(AppiumBy.xpath(xpath)).isDisplayed()) {
                    System.out.println("Is Displayed");
                    androidDriver.findElement(AppiumBy.xpath(xpath)).click();
    	        }
    	    }
    	    catch (NoSuchElementException e) {
                System.out.println("Alternative number already added");
                androidDriver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Offline']/../../../../android.widget.LinearLayout[1]")).click();
                androidDriver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Personal Details']/../../../android.widget.LinearLayout[1]")).click();
                androidDriver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Remove']")).click();
                androidDriver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Yes, Remove It']")).click();
                androidDriver.findElement(AppiumBy.xpath(xpath)).click();
                return;
    	    }
    	}
        else if(state.contains("Add Alternate Number")){
            // Enter the first random phone number
            if(state.contains("1")){
                firstAltNumber = generateNewMobNumber();
                customizedKeyboardAction(xpath, 10, firstAltNumber);
                // Enter the wrong OTP
                char[] wrongOtp = {'7', '8', '9', '5'};
                // Enter the wrong OTP digit by digit
                customizedKeyboardAction(xpath, 4, wrongOtp);
                // Enter the correct OTP
                char[] correctOtp = {'7', '8', '9', '1'};
                // Enter the correct OTP digit by digit
                customizedKeyboardAction(xpath, 4, correctOtp);
            }
            else if(state.contains("2")){
                customizedKeyboardAction(xpath, 10, firstAltNumber);
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
        if (androidUser != null) {
            androidUser.quit();
        } if (androidDriver != null) {
            androidDriver.quit();
        }
        // String allurePath = "/opt/homebrew/bin/allure";
        String allurePath = "/usr/local/bin/allure";
        System.out.println("report1 :: ");
        String resultsPath = "allure-results";
        runAllureServe(allurePath, resultsPath);
        System.out.println("report2 :: ");
    }
}