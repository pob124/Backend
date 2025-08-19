package com.AutoSales_Agent.Email;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailDraftWithUuid {
	private String uuid;
    private EmailDto email;
    private boolean isCancelled;
    
    public EmailDraftWithUuid(String uuid, EmailDto email) {
        this.uuid = uuid;
        this.email = email;
        this.isCancelled = false;
    }
}
