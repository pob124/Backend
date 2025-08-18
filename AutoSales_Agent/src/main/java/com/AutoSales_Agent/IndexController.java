package com.AutoSales_Agent;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	// ✅ 피드백 리스트 페이지
	@GetMapping("/feedback-list")
	public String feedbackList() {
		return "feedback-list";
	}
}
