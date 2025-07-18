package com.AutoSales_Agent;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Email {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	private Project project_id;
	
	@ManyToOne
	private Lead lead_id;
	
	private String subject;
	private String body;
	
	@Column(updatable = false)
	@CreationTimestamp
	private LocalDateTime created_at;
}
