package com.AutoSales_Agent.Email;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

public interface EmailRepository extends JpaRepository<Email,Integer>{

	List<Email> findAll();
	Optional<Email> findById(Integer id);
	List<Email> findAllByProjectId(Integer projectId);
	List<Email> findAllByLeadId(Integer leadId);
	List<Email> findAllByProjectIdAndLeadId(Integer projectId, Integer leadId);
	List<Email> findByLeadId(Integer leadId);
	List<Email> findByLeadIdAndProjectId(Integer leadId, Integer projectId);
	
	@Query("SELECT e.project.id FROM Email e WHERE e.lead.id = :leadId ORDER BY e.createdAt DESC")
	List<Integer> findRecentProjectIdsByLeadId(@Param("leadId") Integer leadId);
}
