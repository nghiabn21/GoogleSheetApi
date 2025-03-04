package com.GoogleSheetAPI.service;


import com.GoogleSheetAPI.dto.FileInfo;
import com.GoogleSheetAPI.dto.GoogleSheetDTO;
import com.GoogleSheetAPI.dto.GoogleSheetResponseDTO;
import com.GoogleSheetAPI.dto.TitleInfo;
import com.GoogleSheetAPI.entity.PersonInfo;
import com.GoogleSheetAPI.util.GoogleApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

@Service
public class GoogleApiService {

	@Autowired
	private GoogleApiUtil googleApiUtil;

	public List<List<String>> readDataFromGoogleSheet(String id, String range) {
		return googleApiUtil.getDataFromSheet(id, range);
	}

	public List<TitleInfo> getTitleSheet(String id, String range) {
		return googleApiUtil.getTitleSheet(id, range);
	}

	public List<FileInfo> getGoogleSheets() throws Exception {
		return googleApiUtil.getGoogleSheets();
	}

	public GoogleSheetResponseDTO createSheet(GoogleSheetDTO request) throws GeneralSecurityException, IOException {
		return googleApiUtil.createGoogleSheet(request);
	}

}
