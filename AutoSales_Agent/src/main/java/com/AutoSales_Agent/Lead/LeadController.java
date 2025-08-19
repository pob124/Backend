package com.AutoSales_Agent.Lead;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
public class LeadController {

	private final LeadService leadService;
	
	@GetMapping("")
    public ResponseEntity<List<Lead>> findLeads(
            @RequestParam(value = "ids", required = false) List<Integer> ids,
            @RequestParam(value = "names", required = false) List<String> names,
            @RequestParam(value = "name", required = false) String name
    ) {
        // 1. id 리스트로 조회
        if (ids != null && !ids.isEmpty()) {
            return ResponseEntity.ok(leadService.findByIds(ids));
        }
        // 2. 이름 리스트(정확히 일치)
        if (names != null && !names.isEmpty()) {
            return ResponseEntity.ok(leadService.findByCompanyNames(names));
        }
        // 3. 부분검색 (삼성 → 삼성전자, 삼성SDI 등)
        if (name != null && !name.isEmpty()) {
            return ResponseEntity.ok(leadService.findByCompanyNameContaining(name));
        }
        // 4. 전체 리스트
        return ResponseEntity.ok(leadService.findAll());
    }
	
	@GetMapping("/{id}")
	public ResponseEntity<Lead> findById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(leadService.findById(id));
    }
	
	@GetMapping("/project/{projectId}/leads")
	public ResponseEntity<List<Lead>> getLeadsByProject(@PathVariable Integer projectId) {
	    List<Lead> leads = leadService.getLeadsByProjectId(projectId);
	    return ResponseEntity.ok(leads);
	}
	
	@GetMapping("/")
	public ResponseEntity<List<LeadDto>> getAllLeads() {
		List<Lead> leads = leadService.findAll();
		List<LeadDto> leadDtos = leads.stream()
			.map(LeadDto::fromEntity)
			.collect(Collectors.toList());
		return ResponseEntity.ok(leadDtos);
	}

	
	@PostMapping("")
	public ResponseEntity<Lead> createLead(@RequestBody LeadDto leadDto) {
	    Lead lead = leadService.save(leadDto);
	    return ResponseEntity.ok(lead);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Lead> updateLead(
			@PathVariable("id") Integer id,
			@RequestBody Map<String, Object> updates
	){
		Lead updated=this.leadService.update(id, updates);
		return ResponseEntity.ok(updated);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Lead> deleteLead(@PathVariable("id") Integer id){
		Lead deleted=this.leadService.delete(id);
		return ResponseEntity.ok(deleted);
	}
	
}
