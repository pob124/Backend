package com.AutoSales_Agent.Email;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.AutoSales_Agent.Lead.Lead;
import com.AutoSales_Agent.Project.Project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Email {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	private Project project;
	
	@ManyToOne
	private Lead lead;
	
	private String subject;
	
	@Lob
	@Column(columnDefinition = "TEXT")
	private String body;
	
	@Column(nullable = false)
	private boolean sent = false;
	
	@Column(updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;
}
