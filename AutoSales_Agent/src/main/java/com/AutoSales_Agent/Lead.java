package com.AutoSales_Agent;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
	private String company_name;
	private String industry;
	private String company_size;
	private String language;
	
	//메일 받을 사람
	private String contact_name;
	private String contact_email;
	
	@Column(updatable = false)
	@CreationTimestamp
	private LocalDateTime created_at;
}
