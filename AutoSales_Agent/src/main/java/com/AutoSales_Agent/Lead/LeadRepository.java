package com.AutoSales_Agent.Lead;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead,Integer>{

	List<Lead> findByCompanyNameInIgnoreCase(List<String> names);
}
