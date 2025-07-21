package com.AutoSales_Agent.Lead;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeadService {
	
	private LeadRepository leadRepository;
	
	public List<Lead> findByCompanyNames(List<String> names){
		return this.leadRepository.findByCompanyNameInIgnoreCase(names);
	}
}
