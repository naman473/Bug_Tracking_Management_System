package com.startechies.bugtracker.service;

import com.startechies.bugtracker.model.Project;
import com.startechies.bugtracker.model.ProjectHeader;
import com.startechies.bugtracker.model.User;
import com.startechies.bugtracker.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public List<Project> findProjectsForUserEmail(String email) {
        return projectRepository.findProjectsForUserEmail(email);
    }

    public List<User> findEligibleDevelopers() {
        return projectRepository.findEligibleDevelopers();
    }

    public List<String> findEligibleTesters(String managerEmail) {
        return projectRepository.findEligibleTesters(managerEmail);
    }

    public List<User> findTeamMembersByProjectName(String projectName) {
        return projectRepository.findTeamMembersByProjectName(projectName);
    }

    /** Returns manager name and start date in a single join query. */
    public ProjectHeader findProjectHeader(String projectName) {
        return projectRepository.findProjectHeader(projectName);
    }

    /**
     * Single-row EXISTS check used by the ownership interceptor.
     * Much cheaper than loading the full project list on every request.
     */
    public boolean ownsProject(String projectName, String managerEmail) {
        return projectRepository.ownsProject(projectName, managerEmail);
    }

    public List<User> findDevelopersByProjectName(String projectName) {
        return projectRepository.findDevelopersByProjectName(projectName);
    }

    // ── Commands ─────────────────────────────────────────────────────────────

    @Transactional
    public Project createProject(String managerEmail, String projectName,
                                 String desc, LocalDate startDate, String[] teamMembers) {
        Project draft    = Project.draft(projectName, desc, startDate);
        Project created  = projectRepository.addProject(draft, managerEmail);
        // Convert array to List for the batch-update repository method
        List<String> members = teamMembers != null ? List.of(teamMembers) : List.of();
        projectRepository.assignTeamMembers(members, created.projectId());
        return created;
    }
}
