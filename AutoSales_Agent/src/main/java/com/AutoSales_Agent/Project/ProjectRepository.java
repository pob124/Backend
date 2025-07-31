package com.AutoSales_Agent.Project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project,Integer>{

	List<Project> findAllById(Iterable<Integer> ids);
	List<Project> findAllByNameIn(List<String> names);
	List<Project> findAllByNameContaining(String name);
	List<Project> findAll();
	Optional<Project> findById(Integer id);
	
	Optional<Project> findByName(String name);
}
