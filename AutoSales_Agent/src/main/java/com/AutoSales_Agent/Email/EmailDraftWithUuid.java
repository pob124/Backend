package com.AutoSales_Agent.Email;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailDraftWithUuid {
	private String uuid;
    private EmailDto email;
}
