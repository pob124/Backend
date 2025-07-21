package com.AutoSales_Agent.Lead;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
public class LeadController {

	private LeadService leadService;
	
	@PostMapping("/search")
	public ResponseEntity<List<Lead>> searchLeads(@RequestBody Map<String, List<String>> body){
		List<String> names=body.get("company_names");
		List<Lead> leads=this.leadService.findByCompanyNames(names);
		return ResponseEntity.ok(leads);
	}
}
