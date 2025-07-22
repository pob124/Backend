package com.AutoSales_Agent.Project;

import java.util.List;
import java.util.Map;

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
	
	@PostMapping("")
	public ResponseEntity<Project> createProject(@RequestBody ProjectDto projectDto){
		Project project=this.projectService.save(projectDto);
		return ResponseEntity.ok(project);
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
