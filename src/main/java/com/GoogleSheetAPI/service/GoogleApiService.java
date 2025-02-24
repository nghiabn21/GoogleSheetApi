package com.GoogleSheetAPI.service;


import com.GoogleSheetAPI.dto.GoogleSheetDTO;
import com.GoogleSheetAPI.dto.GoogleSheetResponseDTO;
import com.GoogleSheetAPI.util.GoogleApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

@Service
public class GoogleApiService {

	@Autowired
	private GoogleApiUtil googleApiUtil;

	public Map<Object, Object> readDataFromGoogleSheet(String id, String range) {
		return googleApiUtil.getDataFromSheet(id, range);
	}

	public GoogleSheetResponseDTO createSheet(GoogleSheetDTO request) throws GeneralSecurityException, IOException {
		return googleApiUtil.createGoogleSheet(request);
	}

}
