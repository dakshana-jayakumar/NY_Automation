package NYAutomation;



import base.BaseClass;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.TakesScreenshot;

import org.testng.annotations.Test;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Listeners;
import org.testng.ITestListener;
import org.testng.ITestResult;


@Listeners(NYAutomation.AutomationFlow.class)
public class AutomationFlow extends BaseClass implements ITestListener{

    private String RideOtp = "";
    private static ExtentTest test; // Updated variable declaration
    ExtentReports extentReports = setupReport();

    @Test
    /* Creating a method for overall flow of the applications */
    private void flow() throws InterruptedException, IOException, GeneralSecurityException {
        // Fetch test data from Google Sheet
    	TestDataReader.fetchTabNames();
        String[][] testData = TestDataReader.testData.toArray(new String[0][0]);
        for (String[] actionParameter : testData) {
            String testCase = actionParameter[0];
            String screen = actionParameter[1];
            String state = actionParameter[2];
            String xpath = actionParameter[3];
            String sendKeysValue = actionParameter[4];
            boolean isUser = "user".equals(actionParameter[5]);
  
         /* Create a new test in the report */
            test = extentReports.createTest(screen, state);

            checkCase(testCase, screen, state, xpath, sendKeysValue, isUser);
            System.out.println("screen: " + screen + " | state: " + screen + "XPath: " + xpath + " | SendKeys Value: " + sendKeysValue);
        }
    }

    /* method used to code all the types of functions to be handled */
    public void checkCase(String testCase, String screen, String state, String xpath, String sendKeysValue, boolean isUser) throws InterruptedException {
        /* Creating a wait object to wait for the user or driver */
        Wait<AndroidDriver> wait = new FluentWait<>(isUser ? user : driver)
                .withTimeout(Duration.ofSeconds(50)) /* Set the timeout duration to 50 seconds */
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
        //             Wait for the element to be visible before scrolling
        //            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        
                    // Scroll to the desired text
                    scrollToText("Tamil");
        }
        if (("Stats Dashboard".equals(screen)) || ("Logout Section".equals(screen))) {
            Thread.sleep(5000);
            // Add handling of Android back key here
            (isUser ? user : driver).pressKey(new KeyEvent(AndroidKey.BACK));
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

        File userSrcFile = ((TakesScreenshot) user).getScreenshotAs(OutputType.FILE);
        String userDestFilePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main"
                + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "screenshots"
                + File.separator + testCase + "_user_" + timestamp + ".png";
        saveScreenshot(userSrcFile, userDestFilePath);

        return userDestFilePath;
    }

    private String getScreenshotPathForDriver(String testCase) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HH");
        String timestamp = now.format(formatter);

        File driverSrcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String driverDestFilePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main"
                + File.separator + "java" + File.separator + "NYAutomation" + File.separator + "screenshots"
                + File.separator + testCase + "_driver_" + timestamp + ".png";
        saveScreenshot(driverSrcFile, driverDestFilePath);

        return driverDestFilePath;
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
                + File.separator + "java" + File.separator + "NYAutomation" + File.separator+ "Reports" + File.separator +  "report_" + System.currentTimeMillis() + ".html";
		
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

        test.addScreenCaptureFromPath(getScreenshotPathForUser(result.getMethod().getMethodName()));

        test.addScreenCaptureFromPath(getScreenshotPathForDriver(result.getMethod().getMethodName()));
    }

    /* Tear down ExtentReports */
    @AfterSuite
    public void tearDownReport() {
    	extentReports.flush();
    }
    
}