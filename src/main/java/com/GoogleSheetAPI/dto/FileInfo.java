package com.GoogleSheetAPI.dto;

import com.google.api.client.util.DateTime;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo {

    private String fileName;
    private String spreadsheetId;
    private String date;
}
