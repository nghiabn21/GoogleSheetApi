package com.GoogleSheetAPI.util;


import com.GoogleSheetAPI.dto.FileInfo;
import com.GoogleSheetAPI.dto.GoogleSheetDTO;
import com.GoogleSheetAPI.dto.GoogleSheetResponseDTO;
import com.GoogleSheetAPI.dto.TitleInfo;
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
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import org.springframework.stereotype.Component;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH));
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

    public List<TitleInfo> getTitleSheet(String spreadsheetId, String range){
        List<TitleInfo> titleInfos = new ArrayList<>();
        String firstSheetName = "";
        try {
            Sheets service = getSheetService();
            Spreadsheet spreadsheet = service.spreadsheets().get(spreadsheetId).execute();
            // Lấy danh sách các sheet
            List<Sheet> sheets = spreadsheet.getSheets();
            // Kiểm tra nếu có ít nhất một sheet
            if (sheets != null && !sheets.isEmpty()) {
                SheetProperties firstSheet = sheets.get(0).getProperties();
                firstSheetName = firstSheet.getTitle();
            } else {
                System.out.println("Không có sheet nào trong spreadsheet.");
                throw new RuntimeException("Error getting data from Google Sheets API");
            }
            firstSheetName = firstSheetName + "!A1:1";
            // https://sheets.googleapis.com/v4/spreadsheets/{spreadsheetId}/values/{range}
            ValueRange responseTitle = service.spreadsheets().values().get(spreadsheetId, firstSheetName).execute();

            List<List<Object>> valuesTitle = responseTitle.getValues();

            if (valuesTitle != null && !valuesTitle.isEmpty()) {
                List<Object> headers = valuesTitle.get(0); // Lấy hàng đầu tiên
                for (Object header : headers) {
                    TitleInfo titleInfo = new TitleInfo();
                    titleInfo.setTitle((String) header);
                    titleInfos.add(titleInfo);
                }
            } else {
                TitleInfo titleInfo = new TitleInfo();
                titleInfo.setTitle("Không tìm thấy dữ liệu.");
                titleInfos.add(titleInfo);
            }
        }catch (Exception e){
            LOGGER.info("Error getting data from Google Sheets API");
            throw new RuntimeException("Error getting data from Google Sheets API: " + e.getMessage());
        }
        return titleInfos;
    }

    public List<List<String>> getDataFromSheet(String spreadsheetId, String range){
        List<PersonInfo> personInfos = new ArrayList<>();
        List<List<String>> dataList = new ArrayList<>();
        SheetProperties properties = null ;
        String firstSheetName = null ;
        try {
            Sheets service = getSheetService();

            // Lấy thông tin về toàn bộ spreadsheet
            // https://sheets.googleapis.com/v4/spreadsheets/{spreadsheetId}/values/{range}
            Spreadsheet spreadsheet = service.spreadsheets().get(spreadsheetId).execute();
            // Lấy danh sách các sheet
            List<Sheet> sheets = spreadsheet.getSheets();
            // Kiểm tra nếu có ít nhất một sheet
            if (sheets != null && !sheets.isEmpty()) {
                SheetProperties firstSheet = sheets.get(0).getProperties();
                firstSheetName = firstSheet.getTitle();
            } else {
                System.out.println("Không có sheet nào trong spreadsheet.");
                throw new RuntimeException("Error getting data from Google Sheets API");
            }
            firstSheetName = firstSheetName + "!A2:F";
            ValueRange response = service.spreadsheets().values().get(spreadsheetId, firstSheetName).execute();
            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                LOGGER.info("No data found.");
                throw new RuntimeException("No data found.");
            } else {
                // Duyệt qua từng hàng dữ liệu từ hàng 2 trở đi (bỏ tiêu đề)
                for (int i = 0 ; i < values.size(); i++) {
                    List<Object> row = values.get(i);
                    List<String> rowData = row.stream()
                            .map(Object::toString)
                            .collect(Collectors.toList());
                    dataList.add(rowData);
                }
            }
        }catch (Exception e){
            LOGGER.info("Error getting data from Google Sheets API");
            throw new RuntimeException("Error getting data from Google Sheets API: " + e.getMessage());
        }
        return dataList;
    }

    public List<FileInfo> getGoogleSheets() throws Exception {
        Drive service = getDriveService();
        List<FileInfo> fileInfos = new ArrayList<>();

        FileList result = service.files().list()
                .setQ("mimeType='application/vnd.google-apps.spreadsheet'")
                .setFields("files(id, name, createdTime)") // id, name, owners, createdTime
                .execute();

        for(File file : result.getFiles()) {
            FileInfo fileInfo  = new FileInfo();
            fileInfo.setFileName(file.getName());
            fileInfo.setSpreadsheetId(file.getId());

            // Chuyển DateTime thành Instant
            Instant instant = Instant.ofEpochMilli(file.getCreatedTime().getValue());
            // Chuyển đổi sang LocalDateTime với múi giờ hệ thống
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            // Định dạng lại thành "yyyy-MM-dd HH:mm"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedDate = localDateTime.format(formatter);
            fileInfo.setDate(formattedDate);

            fileInfos.add(fileInfo);
        }

        return fileInfos;
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
//        googleApiUtil.getGoogleSheets();
        googleApiUtil.getDataFromSheet("1M4q1sPgfwR3wBXWa8ZJL3-aCcHBEcZRyKXKdrLi-6hE","Class Data!A2:Z");
//        googleApiUtil.createNewSheet("","Sheet 2");
          // ghi dữ liệu vào sheet đã có data
//        writeDataGoogleSheets(service,"Sheet 3", new ArrayList<>(Arrays.asList("Test1", "Test2", "Test3")),"");
    }
}
