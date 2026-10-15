package com.startechies.bugtracker.service;

import com.startechies.bugtracker.model.*;
import com.startechies.bugtracker.repository.BugActivityRepository;
import com.startechies.bugtracker.repository.BugRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BugServiceTest {

    private BugRepository         bugRepository;
    private BugActivityRepository activityRepository;
    private BugService            bugService;

    @BeforeEach
    void setUp() {
        bugRepository      = mock(BugRepository.class);
        activityRepository = mock(BugActivityRepository.class);
        bugService         = new BugService(bugRepository, activityRepository);
    }

    // ── reportBug ─────────────────────────────────────────────────────────────

    @Test
    void reportBug_insertsAndRecordsActivity() {
        CreateBugCommand cmd = new CreateBugCommand(
                "ProjectX", "Alice", "Login broken", "Steps…", Severity.CRITICAL);
        when(bugRepository.insertBug(cmd)).thenReturn("BUG-ABC");

        String id = bugService.reportBug(cmd);

        assertThat(id).isEqualTo("BUG-ABC");
        verify(activityRepository).record(argThat(a ->
                "REPORTED".equals(a.actionType())
                        && "BUG-ABC".equals(a.bugId())
                        && "Alice".equals(a.actorName())));
    }

    // ── assignBugToDeveloper ──────────────────────────────────────────────────

    @Test
    void assignBug_whenUpdateSucceeds_recordsActivity() {
        when(bugRepository.assignBugToDeveloper("BUG-1", "Bob")).thenReturn(1);

        int result = bugService.assignBugToDeveloper("BUG-1", "Bob", "manager5");

        assertThat(result).isEqualTo(1);
        verify(activityRepository).record(argThat(a ->
                "ASSIGNED".equals(a.actionType()) && "manager5".equals(a.actorName())));
    }

    @Test
    void assignBug_whenUpdateFails_doesNotRecordActivity() {
        when(bugRepository.assignBugToDeveloper("BUG-1", "Bob")).thenReturn(0);

        int result = bugService.assignBugToDeveloper("BUG-1", "Bob", "manager5");

        assertThat(result).isZero();
        verifyNoInteractions(activityRepository);
    }

    // ── markForClose ─────────────────────────────────────────────────────────

    @Test
    void markForClose_whenSucceeds_recordsActivity() {
        when(bugRepository.markForClose("BUG-1")).thenReturn(1);

        int result = bugService.markForClose("BUG-1", "manager5");

        assertThat(result).isEqualTo(1);
        verify(activityRepository).record(argThat(a ->
                "MARKED_FOR_CLOSE".equals(a.actionType())));
    }

    @Test
    void markForClose_whenAlreadyClosed_doesNotRecordActivity() {
        when(bugRepository.markForClose("BUG-1")).thenReturn(0);

        int result = bugService.markForClose("BUG-1", "manager5");

        assertThat(result).isZero();
        verifyNoInteractions(activityRepository);
    }

    // ── closeBug ─────────────────────────────────────────────────────────────

    @Test
    void closeBug_whenMarkedForClose_recordsActivity() {
        when(bugRepository.closeBug("BUG-1", "manager5")).thenReturn(1);

        int result = bugService.closeBug("BUG-1", "manager5");

        assertThat(result).isEqualTo(1);
        verify(activityRepository).record(argThat(a ->
                "CLOSED".equals(a.actionType()) && "manager5".equals(a.actorName())));
    }

    @Test
    void closeBug_whenNotMarkedForClose_doesNotRecordActivity() {
        when(bugRepository.closeBug("BUG-1", "manager5")).thenReturn(0);

        assertThat(bugService.closeBug("BUG-1", "manager5")).isZero();
        verifyNoInteractions(activityRepository);
    }

    // ── editBug ───────────────────────────────────────────────────────────────

    @Test
    void editBug_whenOwnershipMatches_recordsActivity() {
        UpdateBugCommand cmd = new UpdateBugCommand(
                "BUG-1", "Alice", "New title", "Desc", Severity.MAJOR);
        when(bugRepository.updateBugDetails(cmd)).thenReturn(1);

        int result = bugService.editBug(cmd, "Alice");

        assertThat(result).isEqualTo(1);
        verify(activityRepository).record(argThat(a -> "EDITED".equals(a.actionType())));
    }

    @Test
    void editBug_whenOwnershipMismatch_doesNotRecordActivity() {
        UpdateBugCommand cmd = new UpdateBugCommand(
                "BUG-1", "Eve", "Injected title", "Desc", Severity.MAJOR);
        when(bugRepository.updateBugDetails(cmd)).thenReturn(0);

        assertThat(bugService.editBug(cmd, "Eve")).isZero();
        verifyNoInteractions(activityRepository);
    }
}
