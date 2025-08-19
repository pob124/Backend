package com.AutoSales_Agent.Feedback;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {
	
	private final FeedbackService feedbackService;

	@GetMapping("/latest/{emailId}")
	public ResponseEntity<FeedbackDto> getLatestFeedback(@PathVariable Integer emailId) {
	    return feedbackService.getLatestFeedbackByEmailId(emailId)
	            .map(feedback -> ResponseEntity.ok(FeedbackDto.fromEntity(feedback)))
	            .orElse(ResponseEntity.noContent().build());
	}
	
	@PostMapping("/")
	public ResponseEntity<FeedbackDto> saveFeedback(@RequestBody @Valid FeedbackDto dto) {
	    Feedback saved = feedbackService.saveFeedback(dto);
	    return ResponseEntity.ok(FeedbackDto.fromEntity(saved));
	}
	
	//최근 피드백 리스트 조회
	@GetMapping("/list")
	public ResponseEntity<List<FeedbackDto>> getRecentFeedbacks() {
	    List<Feedback> feedbacks = feedbackService.getRecentFeedbacks();
	    List<FeedbackDto> dtoList = feedbacks.stream()
	        .map(FeedbackDto::fromEntity)
	        .collect(Collectors.toList());
	    return ResponseEntity.ok(dtoList);
	}
	
	//프로젝트별 피드백 리스트 조회
	@GetMapping("/list/project/{projectId}")
	public ResponseEntity<List<FeedbackDto>> getFeedbacksByProject(@PathVariable Integer projectId) {
	    List<Feedback> feedbacks = feedbackService.getFeedbacksByProject(projectId);
	    List<FeedbackDto> dtoList = feedbacks.stream()
	        .map(FeedbackDto::fromEntity)
	        .collect(Collectors.toList());
	    return ResponseEntity.ok(dtoList);
	}
	
	//리드별 피드백 리스트 조회
	@GetMapping("/list/lead/{leadId}")
	public ResponseEntity<List<FeedbackDto>> getFeedbacksByLead(@PathVariable Integer leadId) {
	    List<Feedback> feedbacks = feedbackService.getFeedbacksByLead(leadId);
	    List<FeedbackDto> dtoList = feedbacks.stream()
	        .map(FeedbackDto::fromEntity)
	        .collect(Collectors.toList());
	    return ResponseEntity.ok(dtoList);
	}
}
