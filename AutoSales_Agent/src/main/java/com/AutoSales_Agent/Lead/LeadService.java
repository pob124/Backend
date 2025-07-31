package com.AutoSales_Agent.Lead;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.AutoSales_Agent.ProjectLeadMap.ProjectLeadMap;
import com.AutoSales_Agent.ProjectLeadMap.ProjectLeadMapRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeadService {
	
	private final LeadRepository leadRepository;
	private final ProjectLeadMapRepository projectLeadMapRepository;
	
    public List<Lead> findAll() {
        return this.leadRepository.findAll();
    }

    public Lead findById(Integer id) {
        return this.leadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("기업을 찾을 수 없습니다."));
    }

    public List<Lead> findByIds(List<Integer> ids) {
        return this.leadRepository.findAllById(ids);
    }

    public List<Lead> findByCompanyNames(List<String> names) {
        return this.leadRepository.findAllByNameIn(names);
    }

    public List<Lead> findByCompanyNameContaining(String name) {
        return this.leadRepository.findAllByNameContaining(name);
    }

	public Lead save(LeadDto leadDto) {
		
		Lead lead=new Lead();
		lead.setName(leadDto.getName());
		lead.setIndustry(leadDto.getIndustry());
		lead.setContactEmail(leadDto.getContactEmail());
		lead.setLanguage(leadDto.getLanguage());
		lead.setContactName(leadDto.getContactName());
		lead.setSize(leadDto.getSize());
		
		return leadRepository.save(lead);
	}
	
	public Lead update(Integer id, Map<String, Object> updates) {
	    Lead lead = leadRepository.findById(id)
	        .orElseThrow(() -> new RuntimeException("기업이 없습니다"));

	    // 필요한 필드만 업데이트
	    if (updates.containsKey("name")) {
	        lead.setName((String) updates.get("name"));
	    }
	    if (updates.containsKey("industry")) {
	        lead.setIndustry((String) updates.get("industry"));
	    }
	    if (updates.containsKey("contactEmail")) {
	        lead.setContactEmail((String) updates.get("contactEmail"));
	    }
	    if (updates.containsKey("language")) {
	    	Object lang = updates.get("language");
	    	if (lang != null) {
	            try {
	                lead.setLanguage(Language.valueOf(lang.toString().toUpperCase()));
	            } catch (IllegalArgumentException e) {
	                throw new RuntimeException("지원하지 않는 언어입니다: " + lang);
	            }
	        }
	    }

	    return this.leadRepository.save(lead);
	}
	
	public Lead delete(Integer id) {
		Lead target = leadRepository.findById(id)
		        .orElseThrow(() -> new RuntimeException("해당 기업을 찾을 수 없습니다."));
		this.leadRepository.deleteById(id);
		return target;
	}
	
	public List<Lead> getLeadsByProjectId(Integer projectId) {
	    List<ProjectLeadMap> mappings = projectLeadMapRepository.findByProjectId(projectId);
	    return mappings.stream()
	            .map(ProjectLeadMap::getLead)
	            .distinct()
	            .collect(Collectors.toList());
	}
}
