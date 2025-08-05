package com.AutoSales_Agent.Project;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.AutoSales_Agent.Email.EmailRepository;
import com.AutoSales_Agent.Lead.Lead;
import com.AutoSales_Agent.Lead.LeadRepository;
import com.AutoSales_Agent.ProjectLeadMap.ProjectLeadMap;
import com.AutoSales_Agent.ProjectLeadMap.ProjectLeadMapRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final ProjectLeadMapRepository projectLeadMapRepository;
	private final LeadRepository leadRepository;
	private final EmailRepository emailRepository;
	
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
		project.setIndustry(projectDto.getIndustry());
		return this.projectRepository.save(project);
	}
	
    public List<Lead> autoConnectLeads(Integer projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("프로젝트 없음"));

        String industry = project.getIndustry();
        List<Lead> leads = leadRepository.findByIndustry(industry);

        for (Lead lead : leads) {
            if (!projectLeadMapRepository.existsByProjectIdAndLeadId(projectId, lead.getId())) {
                ProjectLeadMap map = new ProjectLeadMap();
                map.setProject(project);
                map.setLead(lead);
                map.setCreatedAt(LocalDateTime.now());
                projectLeadMapRepository.save(map);
            }
        }
        return leads;
    }
    
    public List<Lead> autoConnectLeadsByProjectName(String projectName) {
        Project project = projectRepository.findByName(projectName)
            .orElseThrow(() -> new IllegalArgumentException("해당 이름의 프로젝트가 존재하지 않습니다: " + projectName));
        return autoConnectLeads(project.getId());
    }
    
    public List<Lead> connectLeadsByName(String projectName, List<String> leadNames) {
        Project project = projectRepository.findByName(projectName)
            .orElseThrow(() -> new IllegalArgumentException("해당 이름의 프로젝트가 존재하지 않습니다: " + projectName));

        List<Lead> leads = leadRepository.findAllByNameIn(leadNames);
        List<Lead> connected = new ArrayList<>();

        for (Lead lead : leads) {
            if (!projectLeadMapRepository.existsByProjectIdAndLeadId(project.getId(), lead.getId())) {
                ProjectLeadMap map = new ProjectLeadMap();
                map.setProject(project);
                map.setLead(lead);
                map.setCreatedAt(LocalDateTime.now());
                projectLeadMapRepository.save(map);
                connected.add(lead);
            }
        }

        return connected;
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
	
	public List<Project> getProjectsByLeadId(Integer leadId) {
	    List<ProjectLeadMap> mappings = projectLeadMapRepository.findByLeadId(leadId);
	    return mappings.stream()
	            .map(ProjectLeadMap::getProject)
	            .distinct()
	            .collect(Collectors.toList());
	}
	
	//feedback분석 용 projectid 검색 메소드
	public Integer findProjectForFeedback(Integer leadId) {
		List<Integer> projectIds = this.projectLeadMapRepository.findProjectIdsByLeadId(leadId);
		
		if (projectIds == null || projectIds.isEmpty()) {
	        throw new RuntimeException("해당 리드와 연결된 프로젝트가 없음");
	    }
		
		if (projectIds.size() == 1) {
	        return projectIds.get(0);
	    }
		
		List<Integer> recentProjectIds = emailRepository.findRecentProjectIdsByLeadId(leadId);
	    for (Integer projectId : recentProjectIds) {
	        if (projectIds.contains(projectId)) {
	            return projectId;
	        }
	    }
	    throw new RuntimeException("해당 리드와 연결된 프로젝트 중 최근 메일 보낸 프로젝트 없음");
	}
}
