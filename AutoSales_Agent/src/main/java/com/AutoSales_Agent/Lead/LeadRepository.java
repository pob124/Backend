package com.AutoSales_Agent.Lead;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead,Integer>{

	List<Lead> findAllById(Iterable<Integer> ids);
	List<Lead> findAllByNameIn(List<String> names);
	List<Lead> findAllByNameContaining(String name);
	List<Lead> findAll();
	Optional<Lead> findByName(String name);
	Optional<Lead> findById(Integer id);
	List<Lead> findByIndustry(String industry);
	Optional<Lead> findByContactEmail(String contactEmail);
	List<Lead> findAllByContactEmail(String contactEmail);
}
