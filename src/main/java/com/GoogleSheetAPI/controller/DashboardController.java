package com.GoogleSheetAPI.controller;


import com.GoogleSheetAPI.dto.GoogleSheetDTO;
import com.GoogleSheetAPI.dto.GoogleSheetResponseDTO;
import com.GoogleSheetAPI.service.GoogleApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

@RestController
public class DashboardController {

	@Autowired
	private GoogleApiService googleApiService;

	@GetMapping("/getData")
	public Map<Object, Object> readDataFromGoogleSheet(@RequestParam String id, @RequestParam String range) {
		return googleApiService.readDataFromGoogleSheet(id, range);
	}

	@PostMapping("/createSheet")
	public GoogleSheetResponseDTO createGoogleSheet(@RequestBody GoogleSheetDTO request)
			throws GeneralSecurityException, IOException {
		return googleApiService.createSheet(request);
	}
}
