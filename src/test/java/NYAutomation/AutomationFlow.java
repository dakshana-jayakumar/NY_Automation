package NYAutomation;



import base.BaseClass;

import base.ADBDeviceFetcher;
import static base.ADBDeviceFetcher.androidVersions;
import static base.ADBDeviceFetcher.brandNames;

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
import java.util.Scanner;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.NoSuchElementException;

import org.testng.Assert;
import org.testng.ITestListener;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static base.ADBDeviceFetcher.resolutions;


@Listeners(NYAutomation.AutomationFlow.class)
public class AutomationFlow extends BaseClass implements ITestListener {

    private String rideOtp = "";

    static Map<String, String> screenStatusMap = new HashMap<>();

	private final String userMobileNumber = "7777777714";
	private final String driverMobileNumber = "9999999920";
    private static boolean ReportFlag = true;


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
        LogcatToFile.CaptureLogs();

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
            System.out.println("screen: " + screen + " | state: " + state + " | XPath: " + xpath + " | SendKeys Value: " + sendKeysValue);
            try {
            	checkCase(testCase, screen, state, xpath, sendKeysValue, isUser);
            	screenStatusMap.put(whichApp + ":" + testCase + ":" + screen + ":" + state, "Passed");
            }
            catch (Exception e) {
                LogcatToFile.searchErrCode(isUser);
                screenStatusMap.put(whichApp + ":" + testCase + ":" + screen + ":" + state, "Failed");
            	logErrorToAllureReport(e.getMessage(), driver, user, screenStatusMap);
                // Thread.sleep(3000);
                throw e;
            }
        }
        logPassToAllureReport("Build Passed!", driver, user, screenStatusMap);
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
    
    public void runAllureServe() throws IOException {
    	try {
            // Replace "/opt/homebrew/bin/allure" with the actual path to the Allure command-line tool executable
            String allurePath = "/opt/homebrew/bin/allure";
            // String allurePath = "/usr/local/bin/allure";
            
            // Specify the command to execute
            String command = allurePath + " serve " + System.getProperty("user.dir") + File.separator + "allure-results";
            
            // Execute the command
            Process process = Runtime.getRuntime().exec(command);
            
            // Read the output from the process if needed
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            
            // Wait for the process to complete
            int exitCode = process.waitFor();
            
            // Print the exit code
            System.out.println("Allure serve command executed. Exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
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
        
    	if ("Location Permission".equals(state)) {
            /* If the state is "Location Permission" */
            /* Call the checkLocationPermission method to modify the xpath value */
            xpath = checkLocationPermission(xpath, isUser);
        }
        
    	if ("Select Namma Yatri Partner".equals(state)) {
            /* If the state is "Select Namma Yatri Partner"*/
            /* Call the checkLocationPermission method to perform action */
            checkOverlayPermission();
        }
    	else if ("Battery Optimization".equals(state)) {
            int androidVersion = Integer.parseInt(androidVersions.get(DriverDeviceIndex));
            if (androidVersion == 10) {
            	int loopCount = 2; // Number of times to loop
    	    	for (int i = 0; i < loopCount; i++) {
    	        Thread.sleep(2000);
                KeyEvent appSwitcherKeyEvent = new KeyEvent(AndroidKey.APP_SWITCH);
    	        (isUser ? user : driver).pressKey(appSwitcherKeyEvent);
    	    	}
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
            	int loopCount = 2; // Number of times to loop
    	    	for (int i = 0; i < loopCount; i++) {
    	        Thread.sleep(2000);
                KeyEvent appSwitcherKeyEvent = new KeyEvent(AndroidKey.APP_SWITCH);
    	        (isUser ? user : driver).pressKey(appSwitcherKeyEvent);
    	    	}
            }
        }
        else if ("AutoStart Screen Back Icon".equals(state) && checkAutoStartPermission()) {
            /* If the state is "AutoStart Screen Back Icon" and checkAutoStartPermission is true */
            /* Return from the current method or function */
            return;
        }
        else if ("Back Press".equals(state)) {
            Thread.sleep(5000);
            (isUser ? user : driver).pressKey(new KeyEvent(AndroidKey.BACK));
            return;
        }
        else if ("Scroll Function".equals(state)) {
        	boolean canScrollMore = (Boolean)user.executeScript("mobile: scrollGesture", ImmutableMap.of(
    				"left", 100, "top", 100, "width", 1200, "height", 1200,
    				"direction", "down",
    				"percent", 3.0
    				));
        	Thread.sleep(3000);
        	return;
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
                driver.findElement(AppiumBy.xpath(xpath2)).click();
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
    	        (isUser ? user : driver).pressKey(appSwitcherKeyEvent);
    	    }
    	    return;
    	}
        else if ("Home Press".equals(state)) {
            Thread.sleep(2000);
            (isUser ? user : driver).pressKey(new KeyEvent(AndroidKey.HOME));
            return;
        }
        else if ("SwipeFromUp".equals(state)) {
            Thread.sleep(2000);
            System.out.println("check");
            By buttonLayoutLocator = By.xpath(xpath);
            WebElement element = user.findElement(buttonLayoutLocator);

            /* Get the size of the element */
            Dimension size = element.getSize();
            int startX = size.width / 2;
            int startY = size.height / 2;

            /* Calculate the end coordinates for pulling down the notifications */
            int endX = startX;
            int endY = (int) (startY + size.height * 0.4);

            /* Perform the swipe gesture to pull down the notifications */
            TouchAction<?> touchAction = new TouchAction<>(user);
            touchAction.press(PointOption.point(startX, startY))
                    .waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000)))
                    .moveTo(PointOption.point(endX, endY))
                    .release()
                    .perform();

            /* Perform a click action on the element */
            By invoiceLocator = By.xpath("//android.widget.TextView[@text='Invoice Downloaded']");
            WebElement invoiceElement = user.findElement(invoiceLocator);
            invoiceElement.click();
            Thread.sleep(5000);
            return;
        }
        else if ("Swipe".equals(state)) {
        	int loopCount = 2;
        	for (int i = 0; i < loopCount; i++) {
        	By buttonLayoutLocator = By.xpath(xpath);
            WebElement element = user.findElement(buttonLayoutLocator);
        	/* Perform the swipe gesture */
            (isUser ? user : driver).executeScript("mobile: swipeGesture", ImmutableMap.of(
                    "elementId", ((RemoteWebElement) element).getId(),
                    "direction", "left",
                    "percent", 0.75
            ));
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
        else if ("Back Pressing".equals(state)) {
        	user.pressKey(new KeyEvent(AndroidKey.BACK));
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
    	    user.findElement(AppiumBy.xpath("//android.widget.EditText[@text='Enter 4 digit OTP']")).click();
            
        	((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.DIGIT_7));
        	((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.DIGIT_8));
        	((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.DIGIT_9));
        	((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.DIGIT_5));
        	
        	user.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Resend']")).click();
        	Thread.sleep(16000);
        	
        	((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.DIGIT_7));
        	((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.DIGIT_8));
        	((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.DIGIT_9));
        	((AndroidDriver) user).pressKey(new KeyEvent(AndroidKey.DIGIT_1));
        	
        	return;
        }
        else if ("Driver Login OTP".equals(state)) {
    		driver.findElement(AppiumBy.xpath("//android.widget.EditText[@text='Auto Reading OTP...']")).click();
            
    		Thread.sleep(3000);
    		
        	((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.DIGIT_7));
        	((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.DIGIT_8));
        	((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.DIGIT_9));
        	((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.DIGIT_5));
        	
        	Thread.sleep(12000);
        	
        	driver.findElement(AppiumBy.xpath("//android.widget.EditText")).click();
        	
        	((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.DEL));
        	((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.DEL));
        	((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.DEL));
        	((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.DEL));

        	Thread.sleep(2000);

        	((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.DIGIT_7));
        	((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.DIGIT_8));
        	((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.DIGIT_9));
        	((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.DIGIT_1));
        	return;
    	}
        
        /* Test cases to validate the driver status mode */
    	else if ("Driver Validation".equals(state)) {
            /* Driver status mode validation test case */
    		try {
    			if (driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='GO!']")).isDisplayed()) {
    				System.out.println("Driver is in offline mode");
    				driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='GO!']")).click();
    			}
    		} catch (NoSuchElementException e) {

    			try {
    				if (driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Silent']/../android.widget.ImageView")).isDisplayed()) {
    					System.out.println("Driver is in Silent mode");
    					driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Online']")).click();
    				}
    			} catch (NoSuchElementException e1) {
    				System.out.println("Driver is in Online mode");
    			}
    		}
    		return;
    	}
    	
    	else if ("Alternate number validation".equals(state)) {
            /* Driver alternate mobile number validation test case */
    	    try {
    	        if (driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Add Alternate Number']")).isDisplayed()) {
    	            driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Add Alternate Number']")).click();

    	            long randomNumber = (long) (Math.random() * 900000000L) + 100000000L;
    	            String phoneNumber1 = Long.toString(randomNumber);
    	            String phoneNumber = "9" + phoneNumber1;
    	            char[] mobNumber = phoneNumber.toCharArray();

    	            for (int i = 0; i < 10; i++) {
    	                driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='" + mobNumber[i] + "']")).click();
    	            }
    	            driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='0']/../../../../android.widget.LinearLayout[4]/android.widget.LinearLayout[3]/android.widget.LinearLayout/android.widget.ImageView")).click();

    	            char[] otp = { '7', '8', '9', '1' };
    	            for (int k = 0; k < 4; k++) {
    	                driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='" + otp[k] + "']")).click();
    	            }
    	            driver.findElement( AppiumBy.xpath("//android.widget.TextView[@text='0']/../../../../android.widget.LinearLayout[4]/android.widget.LinearLayout[3]/android.widget.LinearLayout/android.widget.ImageView")).click();

    	            String alternate = driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Alternate Mobile Number']/../android.widget.LinearLayout[1]/android.widget.TextView[1]")).getText();
    	            System.out.println("Added Alternative number is: " + alternate);
    	            driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Edit']")).click();
    	            driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Edit Alternate Mobile Number']/../../android.widget.LinearLayout[2]/android.widget.LinearLayout/android.widget.ImageView")).click();

    	            long randomNumber2 = (long) (Math.random() * 900000000L) + 100000000L;
    	            String phoneNumber2 = Long.toString(randomNumber2);
    	            String phoneNumberSecond = "9" + phoneNumber2;
    	            char[] mobNumberSecond = phoneNumberSecond.toCharArray();

    	            /* Enter second different random phone number */
    	            for (int i = 0; i < 10; i++) {
    	                driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='" + mobNumberSecond[i] + "']")).click();
    	            }
    	            driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='0']/../../../../android.widget.LinearLayout[4]/android.widget.LinearLayout[3]/android.widget.LinearLayout/android.widget.ImageView")).click();

    	            for (int k = 0; k < 4; k++) {
    	                driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='" + otp[k] + "']")).click();
    	            }
    	            driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='0']/../../../../android.widget.LinearLayout[4]/android.widget.LinearLayout[3]/android.widget.LinearLayout/android.widget.ImageView")).click();
    	            System.out.println("Alternative number edited");
    	            Thread.sleep(3000);
    	            
    	            driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Remove']")).click();
    	            driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Yes, Remove It']")).click();
    	            System.out.println("Alternative number removed");

    	            driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Personal Details']/../android.widget.ImageView")).click();
    	            driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Home']")).click();
    	        }
    	    } catch (NoSuchElementException e) {
    	        System.out.println("Alternative number already added");
    	    }
    	    return;
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
                .withTimeout(Duration.ofSeconds(50))
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
	    if ("google".equals(brandNames.get(DriverDeviceIndex)) || ("Android".equals(brandNames.get(DriverDeviceIndex)))) {
	        modifiedXpath += "2]"; /* Append "2]" to xpath */
	    }
        else if("POCO".equals(brandNames.get(DriverDeviceIndex)) || "Redmi".equals(brandNames.get(DriverDeviceIndex))){
            modifiedXpath = "//androidx.recyclerview.widget.RecyclerView/android.widget.LinearLayout[1]";
        }
        return modifiedXpath;
	}
	
	private void checkOverlayPermission() {
	    int androidVersion = Integer.parseInt(androidVersions.get(DriverDeviceIndex));
        String deviceResolution = resolutions.get(DriverDeviceIndex);
	    /* Check if the Android version of the second connected device is greater than 10 */
	    if ((androidVersion < 10) || ((androidVersion > 10) && ("1080x2400".equals(deviceResolution)))){
            return;
        }
        scrollToText("Namma Yatri Partner");
	}
   
	private boolean checkAutoStartPermission() {
	    /* Check if the brand name at index 1 is "google" or "Android" */
	    return (brandNames.get(DriverDeviceIndex).equals("google") || brandNames.get(DriverDeviceIndex).equals("Android"));
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
                logErrorToAllureReport(mobileNumberError, driver, user, screenStatusMap);
            }
        }
	}
    
    public void languageScroll(String screen) {
    	/* if any specific cases have to be performed */
        if ("Choose Language".equals(screen) && !"Update Language".equals(screen) && !("1080x2400".equals(resolutions.get(DriverDeviceIndex)))) {
            scrollToText("Tamil");
        }
    }
    
    public void cancelRide(String state, String xpath) throws InterruptedException {
    	/* Function for pulling the startride popup till cancel ride, to cancel to ride */
    	if("Draging bottom layout user".equals(state)){
            By buttonLayoutLocator = By.xpath(xpath);
            WebElement source = user.findElement(buttonLayoutLocator);
            ((JavascriptExecutor) user).executeScript("mobile: dragGesture", ImmutableMap.of(
                    "elementId", ((RemoteWebElement) source).getId(),
                    "endX", 447,
                    "endY", 891
                    ));
    	}
        
        else if("Draging bottom layout driver".equals(state)) {
        	By buttonLayoutLocator = By.xpath(xpath);
            WebElement source = driver.findElement(buttonLayoutLocator);
            ((JavascriptExecutor) driver).executeScript("mobile: dragGesture", ImmutableMap.of(
            		"elementId", ((RemoteWebElement) source).getId(),
            		"endX", 123,
					"endY", 891
            		));
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
    
    /* When the build is failed 
     * Attaching UI errors, API errors, screenshots, logs and the screen wise status */
    public static void logErrorToAllureReport(String errorMessage, WebDriver driver, WebDriver user, Map<String, String> screenStatusMap) throws IOException {
        ReportFlag = !ReportFlag;
        if(ReportFlag){
            return;
        }
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
        String header = String.format(format, "APP TYPE", " TEST CASES", " SCREEN", " STATE", " STATUS");
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
        String header = String.format(format, "APP TYPE ", " TEST CASES ", " SCREEN ", " STATE ", " STATUS ");
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
    public void tearDown() throws IOException {
        if (user != null) {
            user.quit();
        } else if (driver != null) {
            driver.quit();
        }
        runAllureServe();
    }
}