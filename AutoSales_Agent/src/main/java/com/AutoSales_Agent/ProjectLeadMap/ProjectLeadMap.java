package com.AutoSales_Agent.ProjectLeadMap;

import java.time.LocalDateTime;

import com.AutoSales_Agent.Lead.Lead;
import com.AutoSales_Agent.Project.Project;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class ProjectLeadMap {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	private Project project;
	
	@ManyToOne
	private Lead lead;
	
	private LocalDateTime createdAt;
}
