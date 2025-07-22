package com.AutoSales_Agent.Feedback;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback,Integer>{

	Optional<Feedback> findFirstByEmailIdOrderByCreatedAtDesc(Integer emailId);
}
