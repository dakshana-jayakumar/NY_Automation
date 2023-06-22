package NYAutomation;



import base.BaseClass;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;

import java.io.IOException;
import java.io.ByteArrayInputStream;

import java.security.GeneralSecurityException;
import java.time.Duration;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.remote.RemoteWebElement;
import org.testng.ITestListener;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

import base.ADBDeviceFetcher;

import static base.ADBDeviceFetcher.androidVersions;
import static base.ADBDeviceFetcher.brandNames;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;



@Listeners(NYAutomation.AutomationFlow.class)
public class AutomationFlow extends BaseClass implements ITestListener {

    private String RideOtp = "";

    Map<String, String> screenStatusMap = new HashMap<>();

    @Test
    @Epic("Allure Results")
    @Feature("TestNG support")
    @Story("Application flow")
    /* Creating a method for overall flow of the applications */
    public void flow() throws InterruptedException, IOException, GeneralSecurityException {
    	/* Fetch test data from Google Sheet */
        TestDataReader.fetchTabNames();
        ADBDeviceFetcher.fetchAdbDeviceProperties();
        String[][] testData = TestDataReader.testData.toArray(new String[0][0]);

        boolean userFlag = true;
        boolean driverFlag = true;

        for (String[] actionParameter : testData) {
            String testCase = actionParameter[0];
            String screen = actionParameter[1];
            String state = actionParameter[2];
            String xpath = actionParameter[3];
            String sendKeysValue = actionParameter[4];
            boolean isUser = "user".equals(actionParameter[5]);
            String whichApp = actionParameter[5];

            if (userFlag && isUser) {
                userFlag = false;
                setup(isUser);
            } else if (driverFlag && !isUser) {
                driverFlag = false;
                setup(isUser);
            }

            try {
            	checkCase(testCase, screen, state, xpath, sendKeysValue, isUser);
            	screenStatusMap.put(whichApp + ":" + testCase + ":" + screen + ":" + state, "Passed");
            }
            catch (Exception e) {
            	screenStatusMap.put(whichApp + ":" + testCase + ":" + screen + ":" + state, "Failed");
            	logErrorToAllureReport(e.getMessage(), driver, user, screenStatusMap);
            	throw e;
            }
        }
        logPassToAllureReport("Build Passed!", driver, user, screenStatusMap);
    }

    /* method used to code all the types of functions to be handled */
    public void checkCase(String testCase, String screen, String state, String xpath, String sendKeysValue, boolean isUser) throws InterruptedException, IOException {
    	/* Variable to store wait */
    	Wait<AndroidDriver> wait = waitTime(isUser);


        
    	if ("Location Permission".equals(state)) {
            /* If the state is "Location Permission" */
            /* Call the checkLocationPermission method to modify the xpath value */
            xpath = checkLocationPermission(xpath, isUser);
        }
        
    	if ("Select Namma Yatri Partner".equals(state) && checkOverlayPermission()) {
            /* If the state is "Select Namma Yatri Partner" and checkOverlayPermission is true */
            /* Return from the current method or function */
            return;
        }
        else if ("Allow Battery Optimization".equals(state)) {
            /* If the state is "Allow Battery Optimization" */
            /* Call the checkBatteryPermission method to modify the xpath value */
            xpath = checkBatteryPermission(xpath);
        }
        else if ("AutoStart Screen Back Icon".equals(state) && checkAutoStartPermission()) {
            /* If the state is "AutoStart Screen Back Icon" and checkAutoStartPermission is true */
            /* Return from the current method or function */
            return;
        }
        
        
        
         /* Function calls for both Customer and Driver */   
        {
            
            /* Function call to perform android back button */
            androidBack(screen, state, isUser);
            
        	/* Function call for ride otp fetch from user and enter in driver */
            rideOTP(state, xpath, wait);

	        /* Function call to validate the mobile number and otp is entered correct */
	        validateMobileNumberAndOtp(state, sendKeysValue, screen, driver);
	        
	        /* Function for checking cancel ride for both user and driver */
	        cancelRide(state, xpath);
        }
              
        
        /* Function calls only in driver application */
        {
	        /* Function for checking scroll in choose language screen in driver */
	        languageScroll(screen);
        }
        

        /* Button layout locator */
        By buttonLayoutLocator = By.xpath(xpath);
        /* Performing action based on input */
        performAction(wait, buttonLayoutLocator, sendKeysValue);
    }


    public Wait<AndroidDriver> waitTime(boolean isUser) {
    	/* Creating a wait object to wait for the user or driver */
        Wait<AndroidDriver> wait = new FluentWait<>(isUser ? user : driver)
                .withTimeout(Duration.ofSeconds(20))
                .pollingEvery(Duration.ofMillis(1000))
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
    
    public void androidBack(String screen, String state, Boolean isUser) throws InterruptedException {
    	/* To perform android back action */
    	if (("Stats Dashboard".equals(screen)) || ("Logout Section".equals(screen)) || ("Minimise Keyboard").equals(state)) {
            Thread.sleep(5000);
            (isUser ? user : driver).pressKey(new KeyEvent(AndroidKey.BACK));
            return;
        }
    }

	public String checkLocationPermission(String modifiedXpath, boolean isUser) {
	    /* Check if any of the first two connected devices has Android version < 10 */
	    if(isUser){
	        return (Integer.parseInt(androidVersions.get(0)) < 10) ? modifiedXpath + "2]" : modifiedXpath + "1]";
	    }else{
	        return (Integer.parseInt(androidVersions.get(1)) < 10) ? modifiedXpath + "2]" : modifiedXpath + "1]";
	    }
	}
	
	private String checkBatteryPermission(String modifiedXpath) {
	    /* Check if the brand name at index 1 is "google" or "Android" */
	    if ("google".equals(brandNames.get(1)) || ("Android".equals(brandNames.get(1)))) {
	        modifiedXpath += "2]"; /* Append "2]" to xpath */
	    }
	    return modifiedXpath;
	}
	
	private boolean checkOverlayPermission() {
	    int androidVersion = Integer.parseInt(androidVersions.get(1));
	    /* Check if the Android version of the second connected device is greater than 10 */
	    if (androidVersion > 10) {
	        scrollToText("Namma Yatri Partner");
	        return false;
	    }
	    return true;
	}
   
	private boolean checkAutoStartPermission() {
	    /* Check if the brand name at index 1 is "google" or "Android" */
	    return (brandNames.get(1).equals("google") || brandNames.get(1).equals("Android"));
	}
    
    public void validateMobileNumberAndOtp(String state, String sendKeysValue, String screen, WebDriver driver) throws InterruptedException, IOException {
        /* Validating the length and mobile number entered is correct or not */
        if ("Enter Mobile Number".equals(state)) {
            String mobileNumber = sendKeysValue;
            Thread.sleep(2000);

            String mobileNumberError = null;
            if (mobileNumber.length() != 10) {
                mobileNumberError = "Invalid mobile number: Length should be 10";
            } else if (mobileNumber.charAt(0) < '6') {
                mobileNumberError = "Invalid mobile number: First digit should be greater than or equal to 6";
            }

            if (mobileNumberError != null) {
                System.out.println(mobileNumberError);
                logErrorToAllureReport(mobileNumberError, driver, user, screenStatusMap);
            }
        }

        /* Validating the length and the otp entered is correct or not */
        if ("Otp".equals(screen)) {
            String otp = sendKeysValue;
            Thread.sleep(2000);

            String otpError = null;
            if (otp.length() != 4) {
                otpError = "Wrong OTP: Length should be 4";
            } else if (!otp.equals("7891")) {
                otpError = "Wrong OTP: Enter the correct otp";
            }

            if (otpError != null) {
                System.out.println(otpError);
                logErrorToAllureReport(otpError, driver, user, screenStatusMap);
            }
        }
    } 
    
    public void rideOTP(String state, String xpath, Wait<AndroidDriver> wait) {
    	if (state.equals("Fetch Otp")) {
        	/* Fetching OTP digits */
            for (int readOtp = 1; readOtp <= 4; readOtp++) {
                RideOtp = RideOtp + wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath + "/../android.widget.LinearLayout/android.widget.LinearLayout[" + readOtp + "]/android.widget.TextView"))).getText();
            }
            System.out.println("Ride Otp = " + RideOtp);

            char[] otp = RideOtp.toCharArray();
            /* Entering OTP digits */
            for (int enterOtp = 0; enterOtp < 4; enterOtp++) {
                char digit = otp[enterOtp];
                System.out.println("Otp Digit = " + digit);
                String xpath2 = "//android.widget.TextView[@text='Please ask the customer for the OTP']/../../../android.widget.LinearLayout[2]/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.TextView[@text='" + digit + "']";
                driver.findElement(AppiumBy.xpath(xpath2)).click();
            }
            System.out.println("Ride Otp = " + RideOtp);
            
            return;
        }
    }
    
    public void languageScroll(String screen) {
    	/* if any specific cases have to be performed */
        if ("Choose Language".equals(screen) && !"Update Language".equals(screen)) {
            scrollToText("Tamil");
        }
    }
    
    public void cancelRide(String state, String xpath) {
    	/* Function for pulling the startride popup till cancel ride, to cancel to ride */
        if ("Draging bottom layout".equals(state)) {
        	/* Screen should be drag up and "Cancel Ride" button should be visible */
            By buttonLayoutLocator = By.xpath(xpath);
            WebElement source = user.findElement(buttonLayoutLocator);
            ((JavascriptExecutor) user).executeScript("mobile: dragGesture", ImmutableMap.of(
                    "elementId", ((RemoteWebElement) source).getId(),
                    "endX", 754,
                    "endY", 1483
            ));
            return;
        }
    }
    
    
    /* When the build is failed 
     * Attaching UI errors, API errors, screenshots, logs and the screen wise status */
    public void logErrorToAllureReport(String errorMessage, WebDriver driver, WebDriver user, Map<String, String> screenStatusMap) throws IOException {
        Allure.addAttachment("Error Message", errorMessage);
        
        StringBuilder screensInfo = new StringBuilder();
        /* Determine the maximum lengths of the columns for alignment */
        int maxIsUserLength = 0;
        int maxTestCaseLength = 0;
        int maxScreenLength = 0;
        int maxStateLength = 0;

        /* Using TreeMap for sorting based on testCase */
        Map<String, String> sortedMap = new TreeMap<>(screenStatusMap);

        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            String key = entry.getKey();
            String isUser = key.split(":")[0];
            String testCase = key.split(":")[1];
            String screen = key.split(":")[2];
            String state = key.split(":")[3];

            maxIsUserLength = Math.max(maxIsUserLength, isUser.length());
            maxTestCaseLength = Math.max(maxTestCaseLength, testCase.length());
            maxScreenLength = Math.max(maxScreenLength, screen.length());
            maxStateLength = Math.max(maxStateLength, state.length());
        }

        /* Build the table format */
        String format = "%-" + (maxIsUserLength + 3) + "s%-" + (maxTestCaseLength + 3) + "s%-" +
                (maxScreenLength + 3) + "s%-" + (maxStateLength + 3) + "s%s%n";

        /* Add the table header */
        String header = String.format(format, "APP TYPE", "TEST CASES", "SCREEN", "STATE", "STATUS");
        screensInfo.append(header);

        /* Add the table rows */
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            String key = entry.getKey();
            String status = entry.getValue();
            String whichApp = key.split(":")[0];
            String testCase = key.split(":")[1];
            String screen = key.split(":")[2];
            String state = key.split(":")[3];

            String row = String.format(format, whichApp, testCase, screen, state, status);
            screensInfo.append(row);
        }
        /* Print or use the screensInfo StringBuilder as desired */
        System.out.println(screensInfo.toString());
        
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

        // Using TreeMap for sorting based on testCase
        Map<String, String> sortedMap = new TreeMap<>(screenStatusMap);

        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            String key = entry.getKey();
            String isUser = key.split(":")[0];
            String testCase = key.split(":")[1];
            String screen = key.split(":")[2];
            String state = key.split(":")[3];

            maxIsUserLength = Math.max(maxIsUserLength, isUser.length());
            maxTestCaseLength = Math.max(maxTestCaseLength, testCase.length());
            maxScreenLength = Math.max(maxScreenLength, screen.length());
            maxStateLength = Math.max(maxStateLength, state.length());
        }

        /* Build the table format */
        String format = "%-" + (maxIsUserLength + 3) + "s%-" + (maxTestCaseLength + 3) + "s%-" +
                (maxScreenLength + 3) + "s%-" + (maxStateLength + 3) + "s%s%n";

        /* Add the table header */
        String header = String.format(format, "APP TYPE", "TEST CASES", "SCREEN", "STATE", "STATUS");
        screensInfo.append(header);

        /* Add the table rows */
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            String key = entry.getKey();
            String status = entry.getValue();
            String whichApp = key.split(":")[0];
            String testCase = key.split(":")[1];
            String screen = key.split(":")[2];
            String state = key.split(":")[3];

            String row = String.format(format, whichApp, testCase, screen, state, status);
            screensInfo.append(row);
        }
        /* Print or use the screensInfo StringBuilder as desired */
        System.out.println(screensInfo.toString());

        Allure.addAttachment("Screens Status", screensInfo.toString());
        Allure.getLifecycle().updateTestCase(testResult -> testResult.setStatus(Status.PASSED));
    }

    /* Method is executed after the test suite finishes and quits the WebDriver instances for both user and driver */
    @AfterSuite
    public void tearDown() {
        if (user != null) {
            user.quit();
        } else if (driver != null) {
            driver.quit();
        }
    }
    
}