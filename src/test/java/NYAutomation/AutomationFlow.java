package NYAutomation;


import base.BaseClass;


import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

@Listeners(NYAutomation.AutomationFlow.class)
public class AutomationFlow extends BaseClass implements ITestListener {

    private String RideOtp = "";

    @Test
    /* Creating a method for overall flow of the applications */
    public void flow() throws InterruptedException, IOException {
    	/* Fetch test data from Google Sheet */
        TestDataReader.fetchTabNames();
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

            if (userFlag && isUser) {
                userFlag = false;
                setup(isUser);
            } else if (driverFlag && !isUser) {
                driverFlag = false;
                setup(isUser);
            }

            checkCase(testCase, screen, state, xpath, sendKeysValue, isUser);
            System.out.println("screen: " + screen + " | state: " + screen + " | XPath: " + xpath + " | SendKeys Value: " + sendKeysValue);
        }
    }

    /* method used to code all the types of functions to be handled */
    public void checkCase(String testCase, String screen, String state, String xpath, String sendKeysValue, boolean isUser) throws InterruptedException {
    	/* Creating a wait object to wait for the user or driver */
        Wait<AndroidDriver> wait = new FluentWait<>(isUser ? user : driver)
                .withTimeout(Duration.ofSeconds(20))
                .pollingEvery(Duration.ofMillis(1000))
                .ignoring(Exception.class);

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
            scrollToText("Tamil");
        }
        if (("Stats Dashboard".equals(screen)) || ("Logout Section".equals(screen))) {
            Thread.sleep(5000);
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
    
    
    /* Method saves the screenshot as an attachment in Allure report */
    @Attachment(value = "Screenshot", type = "image/png")
    public byte[] saveScreenshot(byte[] screenshot) {
        return screenshot;
    }

    /* Method retrieves the name of the test method that is currently being executed */
    private static String getTestMethodName(ITestResult iTestResult) {
        return iTestResult.getMethod().getConstructorOrMethod().getName();
    }
    /* Method is executed at the start of the test suite and logs the name of the test suite */
    @Override
    public void onStart(ITestContext iTestContext) {
        log("Test Suite started: " + iTestContext.getName());
        iTestContext.setAttribute("WebDriver", BaseClass.getDriver());
    }

    /* Method is executed at the end of the test suite and logs the name of the test suite */
    @Override
    public void onFinish(ITestContext iTestContext) {
        log("Test Suite finished: " + iTestContext.getName());
    }

    /* Method is executed at the start of each test case and logs the name of the test case */
    @Override
    public void onTestStart(ITestResult iTestResult) {
        log("Test case started: " + getTestMethodName(iTestResult));
    }

    /* Method is executed when a test case succeeds and logs the name of the test case */
    @Override
    public void onTestSuccess(ITestResult iTestResult) {
        log("Test case succeeded: " + getTestMethodName(iTestResult));
    }
    
    /* Method is executed when a test case fails. It logs the name of the failed test case and captures a screenshot using the WebDriver instance and attaches it to the Allure report */
    @Override
    public void onTestFailure(ITestResult iTestResult) {
        log("Test case failed: " + getTestMethodName(iTestResult));
        Object testClass = iTestResult.getInstance();
        WebDriver driver = BaseClass.getDriver();
        if (driver instanceof WebDriver) {
            log("Capturing screenshot for failed test case: " + getTestMethodName(iTestResult));
            try {
                captureScreenshotAndAttach(driver, getTestMethodName(iTestResult));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /* Method is executed when a test case is skipped and logs the name of the skipped test case */
    @Override
    public void onTestSkipped(ITestResult iTestResult) {
        log("Test case skipped: " + getTestMethodName(iTestResult));
    }

    /* Method is executed when a test case fails within the defined success percentage and logs the name of the failed test case */
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
        log("Test case failed but within success percentage: " + getTestMethodName(iTestResult));
    }

    /* Method captures a screenshot using the WebDriver instance and returns it as a byte array */
    @Attachment(value = "Screenshot", type = "image/png")
    public byte[] captureScreenshot(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    /* Method logs a message using Allure step and also prints it to the console */
    @Step("{0}")
    public void log(String message) {
        Allure.step(message);
        System.out.println(message);
    }

    /* Method captures a screenshot using the WebDriver instance, converts it to a byte array, and attaches it to the Allure report with a specified name */
    public void captureScreenshotAndAttach(WebDriver driver, String screenshotName) throws IOException {
        byte[] screenshot = captureScreenshot(driver);
        Allure.addAttachment(screenshotName, new ByteArrayInputStream(screenshot));
    }

    /* Method reads the byte array from the specified screenshot file and attaches it to the Allure report with a specified name */
    @Attachment(value = "{0}", type = "image/png")
    public byte[] attachScreenshot(String attachmentName, String screenshotPath) throws IOException {
        return Files.readAllBytes(Paths.get(screenshotPath));
    }

    /* Method is executed after the test suite finishes and quits the WebDriver instances for both user and driver */
    @AfterSuite
    public void tearDown() {
    	user.quit();
    	driver.quit();
    }
    
}