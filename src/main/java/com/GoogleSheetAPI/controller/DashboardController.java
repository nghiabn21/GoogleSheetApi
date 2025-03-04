package com.GoogleSheetAPI.controller;


import com.GoogleSheetAPI.dto.GoogleSheetDTO;
import com.GoogleSheetAPI.dto.GoogleSheetResponseDTO;
import com.GoogleSheetAPI.service.GoogleApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

//@RestController
@Controller
public class DashboardController {

	@Autowired
	private GoogleApiService googleApiService;

	@RequestMapping(value = { "/", "/index" }, method = RequestMethod.GET)
	public String index(Model model) {
		model.addAttribute("message", "Test");
		return "web";
	}

	@GetMapping("/details")
	public String categoriesList(@RequestParam String spreadsheetId , Model model) {
		model.addAttribute("categories",
				googleApiService.readDataFromGoogleSheet(spreadsheetId,"Class Data!A2:F"));
		model.addAttribute("titleInfo",
				googleApiService.getTitleSheet(spreadsheetId,"Class Data!A1:Z1"));
		return "List";
	}

	@GetMapping("/getSheets")
	public String getSheets(Model model) throws Exception {
		model.addAttribute("googleInfo", googleApiService.getGoogleSheets());
		return "google-info";

	}

//	@GetMapping("/getData")
//	public Map<Object, Object> readDataFromGoogleSheet(@RequestParam String id, @RequestParam String range) {
//		return googleApiService.readDataFromGoogleSheet(id, range);
//	}
//
//	@PostMapping("/createSheet")
//	public GoogleSheetResponseDTO createGoogleSheet(@RequestBody GoogleSheetDTO request)
//			throws GeneralSecurityException, IOException {
//		return googleApiService.createSheet(request);
//	}
}
