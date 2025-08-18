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
		Lead lead = this.leadRepository.findById(id)
				.orElseThrow(()->new RuntimeException("기업을 찾을 수 없습니다"));
		
		if(updates.containsKey("name")) {
			lead.setName((String)updates.get("name"));
		}
		if(updates.containsKey("industry")) {
			lead.setIndustry((String)updates.get("industry"));
		}
		if(updates.containsKey("contactEmail")) {
			lead.setContactEmail((String)updates.get("contactEmail"));
		}
		if(updates.containsKey("contactName")) {
			lead.setContactName((String)updates.get("contactName"));
		}
		if(updates.containsKey("size")) {
			lead.setSize((String)updates.get("size"));
		}
		if(updates.containsKey("language")) {
			lead.setLanguage((Language)updates.get("language"));
		}
		
		return this.leadRepository.save(lead);
	}
	
	// 중복 이메일 삭제 메서드
	public int removeDuplicateEmails() {
		// 중복된 이메일 찾기
		List<Lead> allLeads = leadRepository.findAll();
		Map<String, List<Lead>> emailGroups = allLeads.stream()
			.filter(lead -> lead.getContactEmail() != null && !lead.getContactEmail().trim().isEmpty())
			.collect(Collectors.groupingBy(Lead::getContactEmail));
		
		int deletedCount = 0;
		
		// 중복된 이메일 그룹에서 최신 ID만 남기고 나머지 삭제
		for (Map.Entry<String, List<Lead>> entry : emailGroups.entrySet()) {
			List<Lead> leads = entry.getValue();
			if (leads.size() > 1) {
				// ID 기준으로 정렬 (최신이 뒤로)
				leads.sort((l1, l2) -> Integer.compare(l1.getId(), l2.getId()));
				
				// 첫 번째(가장 오래된) 것만 남기고 나머지 삭제
				for (int i = 1; i < leads.size(); i++) {
					leadRepository.delete(leads.get(i));
					deletedCount++;
				}
				
				System.out.println("중복 이메일 처리: " + entry.getKey() + " - " + (leads.size() - 1) + "개 삭제");
			}
		}
		
		return deletedCount;
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
