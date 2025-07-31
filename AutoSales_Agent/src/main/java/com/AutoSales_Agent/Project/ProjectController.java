package com.AutoSales_Agent.Project;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
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

import com.AutoSales_Agent.Lead.Lead;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

	private final ProjectService projectService;
	
	@GetMapping("")
	public ResponseEntity<List<Project>> findProjects(
			@RequestParam(value = "ids", required = false) List<Integer> ids,
			@RequestParam(value = "names", required = false) List<String> names,
			@RequestParam(value = "name", required = false) String name
	){
		if(ids!=null && !ids.isEmpty()) {
			return ResponseEntity.ok(this.projectService.findByIds(ids));
		}
		if(names!=null && !names.isEmpty()) {
			return ResponseEntity.ok(this.projectService.findByNames(names));
		}
		if(name!=null && !name.isEmpty()) {
			return ResponseEntity.ok(this.projectService.findByNameContaining(name));
		}
		return ResponseEntity.ok(this.projectService.findAll());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Project> findById(@PathVariable("id") Integer id){
		return ResponseEntity.ok(this.projectService.findById(id));
	}
	
	@GetMapping("/lead/{leadId}/projects")
	public ResponseEntity<List<Project>> getProjectsByLead(@PathVariable Integer leadId) {
	    List<Project> projects = projectService.getProjectsByLeadId(leadId);
	    return ResponseEntity.ok(projects);
	}
	
	@PostMapping("")
	public ResponseEntity<Project> createProject(@RequestBody ProjectDto projectDto){
		Project project=this.projectService.save(projectDto);
		return ResponseEntity.ok(project);
	}
	
	@PostMapping("/{projectId}/auto-connect")
	public ResponseEntity<List<Lead>> autoConnectLeads(@PathVariable Integer projectId) {
	    try {
	        List<Lead> leads = projectService.autoConnectLeads(projectId);
	        return ResponseEntity.ok(leads);
	    } catch (Exception e) {
	        return ResponseEntity.badRequest().build();
	    }
	}
	
	@PostMapping("/auto-connect-by-name")
	public ResponseEntity<?> autoConnectLeadsByProjectName(@RequestBody Map<String, String> body) {
	    String name = body.get("projectName");
	    if (name == null || name.trim().isEmpty()) {
	        return ResponseEntity.badRequest().body("projectName 파라미터가 필요합니다.");
	    }

	    try {
	        List<Lead> connectedLeads = projectService.autoConnectLeadsByProjectName(name);
	        return ResponseEntity.ok(connectedLeads);
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	    }
	}
	
	@PostMapping("/auto-connect-by-name-with-leads")
	public ResponseEntity<?> connectSpecificLeadsByProjectName(@RequestBody Map<String, Object> body) {
	    String projectName = (String) body.get("projectName");
	    List<String> leadNames = (List<String>) body.get("leadNames");

	    if (projectName == null || leadNames == null || leadNames.isEmpty()) {
	        return ResponseEntity.badRequest().body("projectName과 leadNames가 모두 필요합니다.");
	    }

	    try {
	        List<Lead> connectedLeads = projectService.connectLeadsByName(projectName, leadNames);
	        return ResponseEntity.ok(connectedLeads);
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	    }
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Project> updateProject(
			@PathVariable("id") Integer id,
			@RequestBody Map<String, Object> updates
	){
		Project updated=this.projectService.update(id, updates);
		return ResponseEntity.ok(updated);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Project> deleteLead(@PathVariable("id") Integer id){
		Project deleted=this.projectService.delete(id);
		return ResponseEntity.ok(deleted);
	}
}
