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

	@RequestMapping(value = {"/categoriesList"}, method = RequestMethod.GET)
	public String categoriesList(Model model) {
		model.addAttribute("categories",
				googleApiService.readDataFromGoogleSheet("1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms","Class Data!A2:F15"));
		return "List";
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
