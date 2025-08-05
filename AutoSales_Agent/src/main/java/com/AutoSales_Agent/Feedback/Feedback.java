package com.AutoSales_Agent.Feedback;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.AutoSales_Agent.Email.Email;
import com.AutoSales_Agent.Lead.Lead;
import com.AutoSales_Agent.Project.Project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Feedback {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	private Project project;
	
	@ManyToOne
	private Lead lead;
	
	@ManyToOne
	private Email mail;
	
	private String responseSummary;
	@Lob
	@Column(columnDefinition = "TEXT")
	private String originalText;
	private String responsetype;
  
    
    @Column(updatable = false)
	@CreationTimestamp
    private LocalDateTime createdAt;
}
