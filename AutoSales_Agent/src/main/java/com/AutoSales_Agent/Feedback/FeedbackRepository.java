package com.AutoSales_Agent.Feedback;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback,Integer>{

	Optional<Feedback> findFirstByMail_IdOrderByCreatedAtDesc(Integer emailId);
	
	// ✅ 최근 피드백 조회 (최신 50개)
	List<Feedback> findTop50ByOrderByCreatedAtDesc();
	
	// ✅ 프로젝트별 피드백 조회
	List<Feedback> findByProject_IdOrderByCreatedAtDesc(Integer projectId);
	
	// ✅ 리드별 피드백 조회
	List<Feedback> findByLead_IdOrderByCreatedAtDesc(Integer leadId);
	
	// ✅ 중복 처리 방지: 동일한 리드와 프로젝트에서 최근 1시간 내 동일한 내용의 피드백 확인
	List<Feedback> findByLead_IdAndProject_IdAndOriginalTextAndCreatedAtAfter(
		Integer leadId, Integer projectId, String originalText, java.time.LocalDateTime after);
}
