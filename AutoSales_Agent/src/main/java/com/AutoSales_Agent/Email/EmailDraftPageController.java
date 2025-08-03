package com.AutoSales_Agent.Email;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/emails")
public class EmailDraftPageController {

	@GetMapping("/drafts")
    public String draftPage() {
        return "drafts"; // static 폴더 기준 상대 경로
    }
}
