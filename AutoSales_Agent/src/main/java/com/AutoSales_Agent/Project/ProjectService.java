package com.AutoSales_Agent.Project;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {

	private final ProjectRepository projectRepository;
	
	public Project findByKeyword(String keyword) {
		return this.projectRepository.findFirstByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
	}
}
