package com.AutoSales_Agent;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Feedback {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	private Project project_id;
	
	@ManyToOne
	private Lead lead_id;
	
	@ManyToOne
	private Email mail_id;
	
	private String response_summary;
	public enum response_type{
        positive, neutral, negative
    }; 
    
    @Enumerated(EnumType.STRING)
    private response_type response_type;
    
    @Column(updatable = false)
	@CreationTimestamp
    private LocalDateTime created_at;
}
