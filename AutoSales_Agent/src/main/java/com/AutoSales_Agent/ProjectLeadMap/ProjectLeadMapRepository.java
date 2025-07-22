package com.AutoSales_Agent.ProjectLeadMap;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectLeadMapRepository extends JpaRepository<ProjectLeadMap, Integer>{
	boolean existsByProjectIdAndLeadId(Integer projectId, Integer leadId);
	List<ProjectLeadMap> findByProjectId(Integer projectId);
	List<ProjectLeadMap> findByLeadId(Integer leadId);
}
