package com.GoogleSheetAPI.util;


import com.GoogleSheetAPI.dto.GoogleSheetDTO;
import com.GoogleSheetAPI.dto.GoogleSheetResponseDTO;
import com.GoogleSheetAPI.entity.PersonInfo;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
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
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import org.springframework.stereotype.Component;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.LogManager;
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

    static {
        try {
            LogManager.getLogManager().readConfiguration(
                    GoogleApiUtil.class.getClassLoader().getResourceAsStream("logging.properties")
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public static Drive getDriveService() throws Exception {
//        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
//                new InputStreamReader(GoogleApiUtil.class.getResourceAsStream(CREDENTIALS_FILE_PATH)));
//
//        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Collections.singleton(DriveScopes.DRIVE_READONLY))
//                .build();
//
//        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, flow.loadCredential("user"))
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//    }

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
//                        new java.io.File(System.getProperty("user.home"), TOKENS_DIRECTORY_PATH)))
                        new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH));
        DataStore<StoredCredential> dataStore = dataStoreFactory.getDataStore("StoredCredential");

        // Lấy Credential từ bộ nhớ
        StoredCredential storedCredential = dataStore.get("user");
        if (storedCredential != null) {
//            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
            System.out.println("Token create: " + storedCredential.getRefreshToken());
        }else {
            System.out.println("Token is null " );
        }


        // Nếu chưa có token trong thư mục TOKENS_DIRECTORY_PATH, chương trình sẽ hiển thị OAuth URL để bạn xác thực.
        // Nếu đã có token, nó sẽ tải token từ bộ nhớ và kiểm tra hạn sử dụng. flow.loadCredential("user")
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public List<PersonInfo> getDataFromSheet(String spreadsheetId, String range){
        Map<Object, Object> storeDataFromGoogleSheet = null;
        List<PersonInfo> personInfos = new ArrayList<>();
        try {
            Sheets service = getSheetService();
            // https://sheets.googleapis.com/v4/spreadsheets/{spreadsheetId}/values/{range}
            ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
            List<List<Object>> values = response.getValues();
            storeDataFromGoogleSheet = new HashMap<>();
            if (values == null || values.isEmpty()) {
                LOGGER.info("No data found.");
                throw new RuntimeException("No data found.");
            } else {
                for (List row : values) {
                    PersonInfo personInfo = PersonInfo.builder()
                            .name((String) row.get(0))
                            .phone((String)row.get(1))
                            .email((String)row.get(2))
                            .address((String)row.get(3))
                            .build();
                    personInfos.add(personInfo);
                }
            }
        }catch (Exception e){
            LOGGER.info("Error getting data from Google Sheets API");
            throw new RuntimeException("Error getting data from Google Sheets API: " + e.getMessage());
        }
        return personInfos;
    }

    public List<com.google.api.services.drive.model.File> getGoogleSheets() throws Exception {
        Drive service = getDriveService();

        FileList result = service.files().list()
                .setQ("mimeType='application/vnd.google-apps.spreadsheet'")
                .setFields("files(id, name, owners, createdTime)")
                .execute();

        return result.getFiles();
    }

    private Sheets getSheetService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME).build();
    }

    private Drive getDriveService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME).build();
    }

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

    // Thêm một sheet mới vào Google Sheet hiện có.
    public void createNewSheet(String existingSpreadSheetID, String newSheetTitle)
            throws IOException, GeneralSecurityException {
        Sheets service = getSheetService();

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
        service.spreadsheets().batchUpdate(existingSpreadSheetID, batchUpdateSpreadsheetRequest).execute();
    }

    public static void writeSheet(Sheets sheets, List<List<Object>> inputData, String sheetAndRange, String existingSpreadSheetID)
            throws IOException {

        @SuppressWarnings("unchecked")
//        List<List<Object>> values = Arrays.asList(inputData);
        ValueRange body = new ValueRange().setValues(inputData);

        UpdateValuesResponse result = sheets.spreadsheets().values()
                .update(existingSpreadSheetID, sheetAndRange, body)
                .setValueInputOption("RAW") // RAW là dạng string k phải kiểu tính ví dụ như = 1 + 2 là string không cộng nhu USER_ENTERED
                .execute();

        LOGGER.info("Cells updated: "  + result.getUpdatedCells());
    }

    public static void writeDataGoogleSheets(Sheets sheets, String sheetName, List<Object> data, String existingSpreadSheetID) throws IOException {
       // lấy số hành hiện có
        int nextRow = getRows(sheets, sheetName, existingSpreadSheetID) + 1;
        List<List<Object>> values = Arrays.asList(data);

        // ghi dữ liệu vào cột A (!A + nextRow), tức là bắt đầu từ cột A và dòng tiếp theo.
        writeSheet(sheets , values, "!A" + nextRow, existingSpreadSheetID);
    }

    public static int getRows(Sheets sheets, String sheetName, String existingSpreadSheetID) throws IOException {
        // Lấy danh sách dữ liệu từ sheet có tên sheetName trong Google Spreadsheet existingSpreadSheetID
        List<List<Object>> values = sheets.spreadsheets().values().get(existingSpreadSheetID, sheetName)
                .execute().getValues();
        // Nếu có dữ liệu, trả về số hàng (numRows), nếu không, trả về 0.
        int numRows = values != null ? values.size() : 0;
        LOGGER.info("rows "+ numRows + ", with sheetName: " + sheetName);
        return numRows;
    }





    /**
     * Prints the names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     */
    public static void main(String... args) throws Exception {

        GoogleApiUtil googleApiUtil = new GoogleApiUtil();
        googleApiUtil.getGoogleSheets();
//        googleApiUtil.getDataFromSheet("1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms","Class Data!A2:F15");
//        googleApiUtil.createNewSheet("","Sheet 2");
          // ghi dữ liệu vào sheet đã có data
//        writeDataGoogleSheets(service,"Sheet 3", new ArrayList<>(Arrays.asList("Test1", "Test2", "Test3")),"");
    }
}
