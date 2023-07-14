package base;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.jetty.SlackAppServer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class SlackBotIntegration {

    public static final String BOT_TOKEN = "";
    public static String SLACK_SIGNING_SECRET = "";
    public static String UPLOAD_CUSTOMER = "/customer_apk_file";
    public static String UPLOAD_DRIVER = "/driver_apk_file";
    public static String CUSTOMER_FILE = "/Customer/customer.apk";
    public static String DRIVER_FILE = "/Driver/driver.apk";
    public static String PATH = "/Users/jaiprasathm/Documents/Automation/NY_Automation/src/main/java/NYAutomation/resources/BotApk";
    private static ExecutorService executorService = Executors.newFixedThreadPool(1);
  

    public static void main(String[] args) throws Exception {
	    //   boolean startExecution = false;
        // Create a new instance of the Slack Bolt App
        AppConfig appConfig = AppConfig.builder()
                .singleTeamBotToken((BOT_TOKEN))
                .signingSecret((SLACK_SIGNING_SECRET))
                .build();
        System.out.println("Build Completed");
        App app = new App(appConfig);

        app.command("/startbot", (req, ctx) -> {
            System.out.println("/start Completed");
            String message = String.format("Hello %s, you can use this bot to upload apk", req.getPayload().getUserName());
            ctx.say(message);
            return ctx.ack();
        });

        app.command(UPLOAD_DRIVER, (req, ctx) -> {
            System.out.println(UPLOAD_DRIVER + " command started");
            String fileNameString = req.getPayload().getText();
            System.out.println("Payload :: " + req.getPayload());
            System.out.println("fileName :: " + fileNameString);
            ctx.say("fileName :: " + fileNameString);
            return ctx.ack();
        });

        app.command(UPLOAD_CUSTOMER, (req, ctx) -> {
            System.out.println(UPLOAD_CUSTOMER + " command started");
            String fileId = req.getPayload().getText();
            String message = uploadResponseHandler(fileId);
            ctx.say(message);
            return ctx.ack();
        });
    
        app.command("/startautomation", (req, ctx) -> {
            String instructionMessage = "To start automation, please choose one of the following commands:\n"
                    + "`/yesss` - Delete existing report files and start automation.\n"
                    + "`/nooo` - Keep existing report files and start automation.";
            ctx.say(instructionMessage);
            return ctx.ack();
        });
        
        app.command("/yes", (req, ctx) -> {
            System.out.println("/yes getting executed");
            
            String startMessage = String.format("Hello %s, The existing report files are deleted and the automation starts for the APKs you have provided", req.getPayload().getUserName());
            ctx.say(startMessage);
            
            String alterMessage = String.format("Hey Users, Don't perform any actions now. Automation is on the progress for %s 's APKs", req.getPayload().getUserName());
            ctx.say(alterMessage);

            SlackBotIntegration slackBotIntegration = new SlackBotIntegration();
            try {
                slackBotIntegration.executeYesMavenCommand();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            String endMessage = String.format("Hey Users, Can upload the APKs now to perfom Automation", req.getPayload().getUserName());
            ctx.say(endMessage);

            return ctx.ack();
        });

        app.command("/no", (req, ctx) -> {
            System.out.println("/no getting executed");
            
            String startMessage = String.format("Hello %s, The existing report files will not be deleted and the automation starts for the APKs you have provided", req.getPayload().getUserName());
            ctx.say(startMessage);
            
            String alterMessage = String.format("Hey Users, Don't perform any actions now. Automation is on the progress for %s 's APKs", req.getPayload().getUserName());
            ctx.say(alterMessage);

            // Execute the Maven command asynchronously in a background thread
            SlackBotIntegration slackBotIntegration = new SlackBotIntegration();
            try {
                slackBotIntegration.executeNoMavenCommand();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            String endMessage = String.format("Hey Users, Can upload the APKs now to perfom Automation", req.getPayload().getUserName());
            ctx.say(endMessage);

            return ctx.ack();
        });

        // Create the Slack App server and start it
        int port = 8000;
        SlackAppServer slackAppServer = new SlackAppServer(app, port);
        System.out.println("Slack App Server started on port :: " + port);
        slackAppServer.start();
    }

    private static String uploadResponseHandler(String fileId) throws IOException {
        String fileUrl = "slack file url" + "fileId";
        String CUSTOMER_PATH = PATH + CUSTOMER_FILE;
//        downloadFile(fileUrl, CUSTOMER_PATH);
        System.out.println(CUSTOMER_FILE + " is uploaded successfully");
        String message = String.format("%s is uploaded successfully... Thankyou for uploading..!", CUSTOMER_FILE);
        return message;
    }
  
  
    private void executeMavenCommand(boolean confirmation) throws ExecutionException, TimeoutException {
	    try {
	        // Set the working directory
	        String workingDirectory = System.getProperty("user.dir");

	        // Determine the confirmation argument based on the boolean value
	        String confirmationArg = confirmation ? "yes" : "no";

	        // Execute the Maven command using ProcessBuilder
	        String mvnExecutable = "/opt/homebrew/bin/mvn"; // Replace '/path/to/mvn' with the actual path to the 'mvn' executable
	        ProcessBuilder processBuilder = new ProcessBuilder(mvnExecutable, "test", "-Dtest=NYAutomation.AutomationFlow#flow", "-Dconfirmation=" + confirmationArg);
	        processBuilder.directory(new File(workingDirectory));

	        Process process = processBuilder.start();

	        // Read the output of the process
	        try (InputStream inputStream = process.getInputStream();
	             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

	            String line;
	            while ((line = bufferedReader.readLine()) != null) {
	                System.out.println(line);
	            }
	        }

	        // Wait for the process to complete with timeout
	        int exitCode = waitForProcessWithTimeout(process, 30, TimeUnit.MINUTES); // Set the timeout value as needed

	        // Check if the process completed successfully
	        if (exitCode == 0) {
	            System.out.println("Maven command executed successfully");
	        } else {
	            System.out.println("Failed to execute Maven command. Exit code: " + exitCode);
	        }
	    } catch (IOException | InterruptedException e) {
	        System.out.println("An error occurred while executing the Maven command: " + e.getMessage());
	    }
	}

	private void executeYesMavenCommand() throws ExecutionException, TimeoutException {
	    executeMavenCommand(true);
	}

	private void executeNoMavenCommand() throws ExecutionException, TimeoutException {
	    executeMavenCommand(false);
    }
  
    private int waitForProcessWithTimeout(Process process, long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        Future<Integer> future = executorService.submit(() -> process.waitFor());
        try {
              return future.get(timeout, timeUnit);
        } finally {
              future.cancel(true);
        }
    }

}