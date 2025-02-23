package com.GoogleSheetAPI.util;


import com.GoogleSheetAPI.dto.GoogleSheetDTO;
import com.GoogleSheetAPI.dto.GoogleSheetResponseDTO;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import org.springframework.stereotype.Component;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Logger;

@Component
public class GoogleApiUtil {

    private static final Logger LOGGER = Logger.getLogger(GoogleApiUtil.class.getName());

    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    //    private static final String TOKENS_DIRECTORY_PATH = "tokens/path";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    /**
     * Global instance of the scopes required by this quickstart. If modifying these
     * scopes, delete your previously saved tokens/ folder.
     * SPREADSHEETS_READONLY: không có quyền insert
     */
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GoogleApiUtil.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(
                        new java.io.File(System.getProperty("user.home"), TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public Map<Object, Object> getDataFromSheet() throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        final String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
        final String range = "Class Data!A2:F";
        Sheets service = getSheetService();
        ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
        List<List<Object>> values = response.getValues();
        Map<Object, Object> storeDataFromGoogleSheet = new HashMap<>();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (List row : values) {
                storeDataFromGoogleSheet.put(row.get(0), row.get(5));
            }
        }
        return storeDataFromGoogleSheet;
    }

    private Sheets getSheetService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME).build();
    }

//    private Drive getDriveService() throws GeneralSecurityException, IOException {
//        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        return new Drive.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
//                .setApplicationName(APPLICATION_NAME).build();
//    }

    public GoogleSheetResponseDTO createGoogleSheet(GoogleSheetDTO request)
            throws GeneralSecurityException, IOException {

        Spreadsheet createdResponse = null;
        GoogleSheetResponseDTO googleSheetResponseDTO = null;
        try {
            Sheets service = getSheetService();

            // tạo trang tính
            SpreadsheetProperties spreadsheetProperties = new SpreadsheetProperties();
            spreadsheetProperties.setTitle(request.getSheetName());
            // thuộc tính
            SheetProperties sheetProperties = new SheetProperties();
            sheetProperties.setTitle(request.getSheetName());

            Sheet sheet = new Sheet().setProperties(sheetProperties);

            Spreadsheet spreadsheet = new Spreadsheet().setProperties(spreadsheetProperties)
                    .setSheets(Collections.singletonList(sheet));

            // tạo sheet
            createdResponse = service.spreadsheets().create(spreadsheet).execute();

            // add data to sheet
            writeSheet(service, request.getDataToBeUpdated(),"A1", createdResponse.getSpreadsheetId());

//            ValueRange valueRange = new ValueRange().setValues(request.getDataToBeUpdated());
//            service.spreadsheets().values()
//                    .update(createdResponse.getSpreadsheetId(), "A1", valueRange)
//                    .setValueInputOption("RAW")  // RAW là dạng string k phải kiểu tính ví dụ như = 1 + 2 là string không cộng nhu USER_ENTERED
//                    .execute();

            googleSheetResponseDTO = new GoogleSheetResponseDTO();
            googleSheetResponseDTO.setSpreadSheetId(createdResponse.getSpreadsheetId());
            googleSheetResponseDTO.setSpeadSheetUrl(createdResponse.getSpreadsheetUrl());

            // gán quyền và gửi thông báo
//        Drive driveService = getDriveService();
//        if (!request.getEmails().isEmpty()) {
//            for (String list : request.getEmails()) {
//                Permission permission = new Permission().setType("user").setRole("writer").setEmailAddress(list);
//                try {
//                    driveService.permissions().create(createdResponse.getSpreadsheetId(), permission)
//                            .setSendNotificationEmail(true).setEmailMessage("Google Sheet Permission testing");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            request.getEmails().forEach(emailAddress -> {
//                Permission permission = new Permission().setType("user").setRole("writer").setEmailAddress(emailAddress);
//                try {
//                    driveService.permissions().create(createdResponse.getSpreadsheetId(), permission)
//                            .setSendNotificationEmail(true).setEmailMessage("Google Sheet Permission testing");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });

        } catch (GoogleJsonResponseException e) {
            GoogleJsonError googleJsonError = e.getDetails();
            if (googleJsonError.getCode() == 404) {
                LOGGER.info("SpreadSheet not found with id: " + createdResponse.getSpreadsheetId());
            } else {
                throw e;
            }
        }
        return googleSheetResponseDTO;
    }

    public void createNewSheet(String existingSpreadSheetID, String newSheetTitle)
            throws IOException, GeneralSecurityException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME).build();

        Sheets.Spreadsheets spreadsheet = service.spreadsheets();
        // Create a new AddSheetRequest
        AddSheetRequest addSheetRequest = new AddSheetRequest();
        SheetProperties sheetProperties = new SheetProperties();
        sheetProperties.setIndex(0);

        // Add the sheetName to the sheetProperties
        addSheetRequest.setProperties(sheetProperties);
        sheetProperties.setTitle(newSheetTitle);

        // Create batchUpdateSpreadsheetRequest
        BatchUpdateSpreadsheetRequest batchUpdateSpreadsheetRequest = new BatchUpdateSpreadsheetRequest();

        // Create requestsList and set it on the batchUpdateSpreadsheetRequest
        List<Request> requestsList = new ArrayList<>();
        batchUpdateSpreadsheetRequest.setRequests(requestsList);

        // Create a new request containing the addSheetRequest and add it to the requestList
        Request request = new Request();
        request.setAddSheet(addSheetRequest);
        requestsList.add(request);

        // Add the requestList to the batchUpdateSpreadsheetRequest
        batchUpdateSpreadsheetRequest.setRequests(requestsList);

        // Call the sheets API to execute the batchUpdate
          spreadsheet.batchUpdate(existingSpreadSheetID, batchUpdateSpreadsheetRequest).execute();
    }

    public static void writeSheet(Sheets sheets, List<List<Object>> inputData, String sheetAndRange, String existingSpreadSheetID)
            throws IOException {

        @SuppressWarnings("unchecked")
//        List<List<Object>> values = Arrays.asList(inputData);
        ValueRange body = new ValueRange().setValues(inputData);

        UpdateValuesResponse result = sheets.spreadsheets().values()
                .update(existingSpreadSheetID, sheetAndRange, body)
                .setValueInputOption("RAW")
                .execute();

        System.out.printf("%d cells updated.\n", result.getUpdatedCells());
    }

    public static void writeDataGoogleSheets(Sheets sheets, String sheetName, List<Object> data, String existingSpreadSheetID) throws IOException {
        int nextRow = getRows(sheets, sheetName, existingSpreadSheetID) + 1;
        List<List<Object>> values = Arrays.asList(data);
        writeSheet(sheets , values, "!A" + nextRow, existingSpreadSheetID);
    }

    public static int getRows(Sheets sheets, String sheetName, String existingSpreadSheetID) throws IOException {
        List<List<Object>> values = sheets.spreadsheets().values().get(existingSpreadSheetID, sheetName)
                .execute().getValues();
        int numRows = values != null ? values.size() : 0;
        System.out.printf("%d rows retrieved in \"%s\"\n", numRows, sheetName);
        return numRows;
    }





    /**
     * Prints the names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     */
    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
        final String range = "Class Data!A2:E";
        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            System.out.println("Name, Major");
            for (List row : values) {
                // Print columns A and E, which correspond to indices 0 and 4.
                System.out.printf("%s, %s\n", row.get(0), row.get(4));
            }
        }

        GoogleApiUtil googleApiUtil = new GoogleApiUtil();
        googleApiUtil.createNewSheet("","Sheet 2");

        writeDataGoogleSheets(service,"Sheet 3", new ArrayList<>(Arrays.asList("Test1", "Test2", "Test3")),"");
    }
}
