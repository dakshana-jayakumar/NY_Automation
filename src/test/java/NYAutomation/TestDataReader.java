package NYAutomation;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestDataReader {
    private static final String APPLICATION_NAME = "NYAutomation";
    private static final String SPREADSHEET_ID = "1jsnDPbPfXiTCylcD1mJ-mqs7WDJhOrb3nJ-SjD1jS1I";
    public static final String SERVICE_ACCOUNT_KEY_PATH = "src/main/java/NYAutomation/resources/credentials.json";
    public static List<String[]> testData = new ArrayList<>();
    
    /*  To run only this file then enable main method */
    //  public static void main(String[] args) {
    //      fetchTabNames();
    //  }

    /**
     * Fetches the names of the tabs (worksheets) in the spreadsheet and fetches data from each tab.
     */
    public static void fetchTabNames() {
        try {
            GoogleCredential credential = getServiceAccountCredential();
            credential.refreshToken();

            // Build the Sheets service
            Sheets sheetsService = buildSheetsService(credential);
            String range = "TestCases!A1:A";
            ValueRange response = getSheetValues(sheetsService, range);

            List<List<Object>> values = response.getValues();
            if (values != null) {
                for (List<Object> row : values) {
                    for (Object cell : row) {
                        String sheetName = cell.toString();
                        fetchTestData(sheetName, sheetsService);
                    }
                }
            } else {
                System.out.println("No tabs found in this sheet");
            }
        } catch (IOException | GeneralSecurityException e) {
            // Handle exceptions
        }
    }

    /**
     * Fetches test data from the specified test type (worksheet) in the spreadsheet.
     * Recursively calls itself if the test type contains "Case" in its name.
     *
     * @param testType      The name of the test type (worksheet)
     * @param sheetsService The Sheets service instance
     */
    public static void fetchTestData(String testType, Sheets sheetsService) {
        try {
            String range = testType + "!A1:Z1001";
            ValueRange response = getSheetValues(sheetsService, range);
            List<List<Object>> values = response.getValues();
            if (values.isEmpty()) {
                System.out.println("Value not found");
            } else {
                for (List<Object> row : values) {
                    String[] rowData = new String[row.size()];
                    String rowAsString = row.toString();
                    if (rowAsString.contains("Case")) {
                        fetchTestData(rowAsString, sheetsService); // Recursive call to fetch data from nested test cases
                    } else {
                        if (rowAsString.contains("Flow")) {
                            // Logic for handling Flow rows
                        } else {
                            for (int i = 0; i < row.size(); i++) {
                                rowData[i] = row.get(i).toString();
                            }
                            testData.add(rowData); // Add the row data to the data list
                        }
                    }
                }
            }
            // System.out.println("-----------Array Values--------------");
            // // Print the fetched data
            // for (String[] rowData : data) {
            //     // Process each row of data
            //     System.out.println(String.join(", ", rowData));
            // }
        } catch (IOException e) {
            // Handle exceptions
        }
    }

    /**
     * Builds the Sheets service using the provided Google Credential.
     *
     * @param credential The Google Credential
     * @return The initialized Sheets service
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private static Sheets buildSheetsService(GoogleCredential credential) throws IOException, GeneralSecurityException {
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                credential).setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * Retrieves the values from the specified range in the spreadsheet.
     *
     * @param sheetsService The Sheets service instance
     * @param range         The range to retrieve values from
     * @return The ValueRange containing the retrieved values
     * @throws IOException
     */
    private static ValueRange getSheetValues(Sheets sheetsService, String range) throws IOException {
        return sheetsService.spreadsheets().values().get(SPREADSHEET_ID, range).execute();
    }

    /**
     * Retrieves the Google Credential for the service account from the specified key file.
     *
     * @return The Google Credential
     * @throws IOException
     */
    private static GoogleCredential getServiceAccountCredential() throws IOException {
        FileInputStream serviceAccountFile = new FileInputStream(SERVICE_ACCOUNT_KEY_PATH);
        return GoogleCredential.fromStream(serviceAccountFile)
                .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS));
    }
}
