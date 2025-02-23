package com.GoogleSheetAPI.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GoogleSheetResponseDTO {

    private String spreadSheetId;
    private String speadSheetUrl;

}
