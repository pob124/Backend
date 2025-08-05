package com.AutoSales_Agent.ProjectLeadMap;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

public interface ProjectLeadMapRepository extends JpaRepository<ProjectLeadMap, Integer>{
	boolean existsByProjectIdAndLeadId(Integer projectId, Integer leadId);
	List<ProjectLeadMap> findByProjectId(Integer projectId);
	List<ProjectLeadMap> findByLeadId(Integer leadId);
	
	@Query("SELECT p.project.id FROM ProjectLeadMap p WHERE p.lead.id = :leadId")
	List<Integer> findProjectIdsByLeadId(@Param("leadId") Integer leadId);
}
