package com.AutoSales_Agent.Feedback;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback,Integer>{

	Optional<Feedback> findFirstByMail_IdOrderByCreatedAtDesc(Integer emailId);
	
	List<Feedback> findTop50ByOrderByCreatedAtDesc();
	
	List<Feedback> findByProject_IdOrderByCreatedAtDesc(Integer projectId);
	
	List<Feedback> findByLead_IdOrderByCreatedAtDesc(Integer leadId);
	
	List<Feedback> findByLead_IdAndProject_IdAndOriginalTextAndCreatedAtAfter(
			Integer leadId, Integer projectId, String originalText, java.time.LocalDateTime after);
}
