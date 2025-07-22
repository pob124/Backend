package com.AutoSales_Agent.Project;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {

	private final ProjectRepository projectRepository;
	
	public List<Project> findAll(){
		return this.projectRepository.findAll();
	}
	
	public Project findById(Integer id) {
		return this.projectRepository.findById(id)
				.orElseThrow(()->new RuntimeException("사업을 찾을 수 없습니다"));
	}
	
	public List<Project> findByIds(List<Integer> ids){
		return this.projectRepository.findAllById(ids);
	}
	
	public List<Project> findByNames(List<String> names){
		return this.projectRepository.findAllByNameIn(names);
	}
	
	public List<Project> findByNameContaining(String name){
		return this.projectRepository.findAllByNameContaining(name);
	}

	public Project save(ProjectDto projectDto) {
		
		Project project=new Project();
		project.setName(projectDto.getName());
		project.setDescription(projectDto.getDescription());
		
		return this.projectRepository.save(project);
	}

	public Project update(Integer id, Map<String, Object> updates) {
		
		Project project=this.projectRepository.findById(id)
				.orElseThrow(()->new RuntimeException("사업이 없습니다"));
		
		if(updates.containsKey("name")) {
			project.setName((String)updates.get("name"));
		}
		if(updates.containsKey("description")) {
			project.setDescription((String)updates.get("description"));
		}
		
		return this.projectRepository.save(project);
	}
	
	public Project delete(Integer id) {
		Project target=this.projectRepository.findById(id)
				.orElseThrow(()->new RuntimeException("사업을 찾을 수 없습니다"));
		this.projectRepository.deleteById(id);
		return target;
	}
	
}
