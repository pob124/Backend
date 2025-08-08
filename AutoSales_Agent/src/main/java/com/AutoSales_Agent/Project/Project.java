package com.AutoSales_Agent.Project;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Data;

@Data
@Entity
public class Project {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	private String name;
	@Lob
	@Column(columnDefinition = "TEXT")
	private String description;
	
	private String industry;
	
	@Column(updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;
}
