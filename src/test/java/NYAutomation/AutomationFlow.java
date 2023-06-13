package NYAutomation;


import base.BaseClass;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static base.ADBDeviceFetcher.androidVersions;
import static base.ADBDeviceFetcher.brandNames;
import static base.ADBDeviceFetcher.fetchAdbDeviceProperties;


@Listeners(NYAutomation.AutomationFlow.class)
public class AutomationFlow extends BaseClass implements ITestListener{

    private String RideOtp = "";
    private static ExtentTest test; // Updated variable declaration
    ExtentReports extentReports = setupReport();
    private String[] mobileNumber;

    @Test
    /* Creating a method for overall flow of the applications */
    private void flow() throws InterruptedException, IOException, GeneralSecurityException {
        // Fetch test data from Google Sheet
    	TestDataReader.fetchTabNames();
        String[][] testData = TestDataReader.testData.toArray(new String[0][0]);
        List<String[]> mobileNumbers = TestDataReader.fetchMobileNumbers();
        int mobileNumbersCount = mobileNumbers.size();
        /* To Fetch Adb Device Properties */
        fetchAdbDeviceProperties();
        for (int i = 1; i < mobileNumbersCount; i++) {
            mobileNumber = mobileNumbers.get(i);
            System.out.println("Mobile Number: " + Arrays.toString(mobileNumber));
            boolean userFlag = true;
            boolean driverFlag = true;
            for (String[] actionParameter : testData) {
                String testCase = actionParameter[0];
                String screen = actionParameter[1];
                String state = actionParameter[2];
                String xpath = actionParameter[3];
                String sendKeysValue = actionParameter[4];
                boolean isUser = "user".equals(actionParameter[5]);
                if(userFlag && isUser) {
                    userFlag = false;
                    setup(isUser);
                }
                else if(driverFlag && !isUser) {
                    driverFlag = false;
                    setup(isUser);
                }

            /* Create a new test in the report */
                test = extentReports.createTest(screen, state);

                System.out.println("screen: " + screen + " | state: " + screen + " | XPath: " + xpath + " | SendKeys Value: " + sendKeysValue);
                checkCase(testCase, screen, state, xpath, sendKeysValue, isUser);
            }
        }
    }

    /* method used to code all the types of functions to be handled */
    public void checkCase(String testCase, String screen, String state, String xpath, String sendKeysValue, boolean isUser) throws InterruptedException {
        /* Creating a wait object to wait for the user or driver */
        Wait<AndroidDriver> wait = new FluentWait<>(isUser ? user : driver)
                .withTimeout(Duration.ofSeconds(30)) /* Set the timeout duration to 30 seconds */
                .pollingEvery(Duration.ofMillis(1000)) /* Set the polling interval to 1000 milliseconds */
                .ignoring(Exception.class); /* Ignore exceptions during the wait */

        if (state.equals("Fetch Otp")) {
            /* Fetching OTP digits */
        	for (int i = 1; i <= 4; i++) {
                RideOtp = RideOtp + wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath + "/../android.widget.LinearLayout/android.widget.LinearLayout[" + i + "]/android.widget.TextView"))).getText();
            }
            System.out.println("Ride Otp = " + RideOtp);

            char[] otp = RideOtp.toCharArray();
            /* Entering OTP digits */
            for (int i = 0; i < 4; i++) {
                char digit = otp[i];
                System.out.println("Otp Digit = " + digit);
                String xpath2 = "//android.widget.TextView[@text='Please ask the customer for the OTP']/../../../android.widget.LinearLayout[2]/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.TextView[@text='" + digit + "']";
                driver.findElement(AppiumBy.xpath(xpath2)).click();
            }
            System.out.println("Ride Otp = " + RideOtp);
            
        	return;
        }
        
         /* if any specific cases have to be performed */
        if ("Choose Language".equals(screen) && !"Update Language".equals(screen)) {
                //     Wait for the element to be visible before scrolling
                //    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        
                    // Scroll to the desired text
                    scrollToText("Tamil");
        }
        if (("Stats Dashboard".equals(screen)) || ("Logout Section".equals(screen))) {
            Thread.sleep(5000);
            // Add handling of Android back key here
            (isUser ? user : driver).pressKey(new KeyEvent(AndroidKey.BACK));
            return;
        }
        if ("Enter Mobile Number".equals(state) && isUser) {
            // If the state is "Enter Mobile Number" and the user is a user
            // Set sendKeysValue to the first value in the mobileNumber array
            sendKeysValue = Arrays.toString(mobileNumber);
            sendKeysValue = sendKeysValue.substring(1, sendKeysValue.length() - 1).split(",")[0].trim();
        } else if ("Enter Mobile Number".equals(state) && !isUser) {
            // If the state is "Enter Mobile Number" and the user is not a user
            // Set sendKeysValue to the second value in the mobileNumber array
            sendKeysValue = Arrays.toString(mobileNumber);
            sendKeysValue = sendKeysValue.substring(1, sendKeysValue.length() - 1);
            String[] sendKeysArray = sendKeysValue.split(", ");
            String secondNumber = "";
            if (sendKeysArray.length > 1) {
                secondNumber = sendKeysArray[1].trim();
            }
            sendKeysValue = secondNumber;
        } else if ("Location Permission".equals(state)) {
            // If the state is "Location Permission"
            // Call the checkLocationPermission method to modify the xpath value
            xpath = checkLocationPermission(xpath);
        } else if ("Select Namma Yatri Partner".equals(state) && checkOverlayPermission()) {
            // If the state is "Select Namma Yatri Partner" and checkOverlayPermission is true
            // Return from the current method or function
            return;
        } else if ("Allow Battery Optimization".equals(state)) {
            // If the state is "Allow Battery Optimization"
            // Call the checkBatteryPermission method to modify the xpath value
            xpath = checkBatteryPermission(xpath);
        } else if ("AutoStart Screen Back Icon".equals(state) && checkAutoStartPermission()) {
            // If the state is "AutoStart Screen Back Icon" and checkAutoStartPermission is true
            // Return from the current method or function
            return;
        }
        

        /* Button layout locator */
        By buttonLayoutLocator = By.xpath(xpath);

        /* Performing action based on input */
        performAction(wait, buttonLayoutLocator, sendKeysValue);
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
    
    /* Scrolls to the specified text */
    public void scrollToText(String text) {

		driver.findElement(new AppiumBy.ByAndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true).instance(0))"
				+ ".scrollIntoView(new UiSelector()" + ".textMatches(\"" + text + "\").instance(0))"));
	}
          
        
    /* Captures a screenshot and replaces the existing screenshot with the latest one */
    private String getScreenshotPathForUser(String testCase) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HH");
        String timestamp = now.format(formatter);

        if (user != null) {
	        File userSrcFile = ((TakesScreenshot) user).getScreenshotAs(OutputType.FILE);
            String userDestFilePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main"
                    + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "screenshots"
                    + File.separator + testCase + "_user_" + timestamp + ".png";
            saveScreenshot(userSrcFile, userDestFilePath);
            return userDestFilePath;
        	} else {
	        	System.out.println("User is not initialized!");
	            return null;
	        }
        }

    public String getScreenshotPathForDriver(String testCase) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HH");
        String timestamp = now.format(formatter);

        if (driver != null) {
            File driverSrcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String driverDestFilePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main"
                    + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "screenshots"
                    + File.separator + testCase + "_driver_" + timestamp + ".png";
            saveScreenshot(driverSrcFile, driverDestFilePath);

            return driverDestFilePath;
        } else {
            System.out.println("Driver is not initialized!");
            return null;
        }
    }

    public void saveScreenshot(File srcFile, String destFilePath) {
        // Replace the existing screenshot file with the latest one
        try {
            Files.copy(srcFile.toPath(), new File(destFilePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        
        
    /* Setup ExtentReports */
    public ExtentReports setupReport() {
        String path = System.getProperty("user.dir") + File.separator + File.separator + "src" + File.separator + "main"
                + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "Reports" + File.separator + "report_" + System.currentTimeMillis() + ".html";

        ExtentSparkReporter reporter = new ExtentSparkReporter(path);
        reporter.config().setReportName("Namma Yatri Test Report");
        reporter.config().setDocumentTitle("Test Results");

        extentReports = new ExtentReports();
        extentReports.attachReporter(reporter);
        return extentReports;
    }
    
    @Override
  public void onTestSuccess(ITestResult result) {
          test.log(Status.PASS, "Test Passed");
      }
    
    @Override
    public void onTestFailure(ITestResult result) {
        test.fail(result.getThrowable());

        String userScreenshotPath = getScreenshotPathForUser(result.getMethod().getMethodName());
        String driverScreenshotPath = getScreenshotPathForDriver(result.getMethod().getMethodName());
        
        if (userScreenshotPath != null && !userScreenshotPath.isEmpty()) {
            test.info("User Screenshot:", MediaEntityBuilder.createScreenCaptureFromPath(userScreenshotPath).build());
        } 
        

        if (driverScreenshotPath != null && !driverScreenshotPath.isEmpty()) {
            test.info("Driver Screenshot:", MediaEntityBuilder.createScreenCaptureFromPath(driverScreenshotPath).build());
        }
    }

    private String checkLocationPermission(String modifiedXpath) {
        // Check if any of the first two connected devices has Android version < 10
        if ((Integer.parseInt(androidVersions.get(0)) < 10) || (Integer.parseInt(androidVersions.get(1)) < 10)) {
            modifiedXpath = modifiedXpath + "2]"; // Append "2]" to xpath
        } else {
            modifiedXpath = modifiedXpath + "1]"; // Append "1]" to xpath
        }
        System.out.println("Modified Xpath: " + modifiedXpath);
        return modifiedXpath;
    }
    
    private String checkBatteryPermission(String modifiedXpath) {
        // Check if the brand name at index 1 is "google" or "Android"
        if ("google".equals(brandNames.get(1)) || ("Android".equals(brandNames.get(1)))) {
            modifiedXpath = modifiedXpath + "2]"; // Append "2]" to xpath
        }
        return modifiedXpath;
    }
    
    private boolean checkOverlayPermission() {
        int androidVersion = Integer.parseInt(androidVersions.get(1));
        // Check if the Android version of the second connected device is greater than 10
        if (androidVersion > 10) {
            scrollToText("Namma Yatri Partner");
            return false;
        }
        return true;
    }
    
    private boolean checkAutoStartPermission() {
        // Check if the brand name at index 1 is "google" or "Android"
        return (brandNames.get(1).equals("google") || brandNames.get(1).equals("Android"));
    }

    /* Tear down ExtentReports */
    @AfterSuite
    public void tearDownReport() {
    	extentReports.flush();
    }
}