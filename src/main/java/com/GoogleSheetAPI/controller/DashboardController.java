package com.GoogleSheetAPI.controller;


import com.GoogleSheetAPI.service.GoogleApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

@RestController
public class DashboardController {

	@Autowired
	private GoogleApiService googleApiService;

	@GetMapping("/getData")
	public Map<Object, Object> readDataFromGoogleSheet() throws GeneralSecurityException, IOException {
		return googleApiService.readDataFromGoogleSheet();
	}

//	@PostMapping("/createSheet")
//	public GoogleSheetResponseDTO createGoogleSheet(@RequestBody GoogleSheetDTO request)
//			throws GeneralSecurityException, IOException {
//		return googleApiService.createSheet(request);
//	}
}
