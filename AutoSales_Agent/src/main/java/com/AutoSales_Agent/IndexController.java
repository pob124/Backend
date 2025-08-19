package com.AutoSales_Agent;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

	@GetMapping("/")
	public String index() {
		return "index";
	}
	@GetMapping("/chat")
	  public String chat() {
	    return "chat";
	}
	
	@GetMapping("/feedback-list")
	public String feedbackList() {
		return "feedback-list";
	}
}
