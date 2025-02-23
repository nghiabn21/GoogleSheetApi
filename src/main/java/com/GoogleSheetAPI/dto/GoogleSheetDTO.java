package com.GoogleSheetAPI.dto;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GoogleSheetDTO {

	private String sheetName;

	private List<List<Object>> dataToBeUpdated;

	private List<String> emails;

}
