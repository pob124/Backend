package com.AutoSales_Agent.Lead;

import java.time.LocalDateTime;


import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "`lead`")
public class Lead {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	//회사정보
	private String name;
	private String industry;
	private String size;
	
	@Enumerated(EnumType.STRING)
	private Language language;
	
	//메일 받을 사람
	private String contactName;
	private String contactEmail;
	
	@Column(updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;
	
}
