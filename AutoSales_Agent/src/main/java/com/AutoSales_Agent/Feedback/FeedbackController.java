package com.AutoSales_Agent.Feedback;

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
}
