package com.AutoSales_Agent.Email;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/emails")
public class EmailDraftController {
	
	private final EmailDraftStorage draftStorage;
	
	@GetMapping("/drafts")
	public String openDraftListUI(Model model,HttpSession session) {
		List<EmailDto> emails = (List<EmailDto>) session.getAttribute("emails");
		System.out.println("📦 draftList from session: " + emails);
		System.out.println("📦 email count: " + (emails != null ? emails.size() : "null"));
		
		if (emails == null || emails.isEmpty()) {
			emails = draftStorage.getStoredEmails();
			if (emails != null && !emails.isEmpty()) {
				session.setAttribute("emails", emails);
				System.out.println("🔄 draftStorage에서 복원된 emails: " + emails.size());
			}
		}
		
	    model.addAttribute("emails", emails != null ? emails : List.of());
	    return "draftList";
	}

	@PostMapping("/drafts")
	public ResponseEntity<String> showDraftList(@RequestBody List<EmailDto> emails, HttpSession session) {
		System.out.println("📨 POST /drafts 호출됨 - emails: " + emails.size());
		System.out.println("📨 session ID: " + session.getId());
		session.setAttribute("emails", emails);
		draftStorage.storeEmails(emails);
		return ResponseEntity.ok("success");
	}
}
