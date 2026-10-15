package com.startechies.bugtracker.model;

import java.time.LocalDate;

/**
 * Mutable bug report. Kept as a JavaBean (no-arg constructor + setters) because
 * Spring's {@link org.springframework.jdbc.core.JdbcTemplate} row mappers need
 * to set fields individually after construction.
 *
 * <p>Business logic that transitions state belongs in
 * {@link com.startechies.bugtracker.service.BugService} — this class is a
 * pure data carrier.
 */
public class Bug {

    private String    bugId;
    private String    bugTitle;
    private String    bugDesc;
    private String    projectName;
    private String    createdBy;
    private LocalDate openDate;
    private String    assignedTo;
    private boolean   markedForClosing;
    private String    closedBy;
    private BugStatus bugStatus;
    private Severity  severity;

    public Bug() {}

    // ── Accessors ────────────────────────────────────────────────────────────

    public String    getBugId()                        { return bugId; }
    public void      setBugId(String v)                { this.bugId = v; }

    public String    getBugTitle()                     { return bugTitle; }
    public void      setBugTitle(String v)             { this.bugTitle = v; }

    public String    getBugDesc()                      { return bugDesc; }
    public void      setBugDesc(String v)              { this.bugDesc = v; }

    public String    getProjectName()                  { return projectName; }
    public void      setProjectName(String v)          { this.projectName = v; }

    public String    getCreatedBy()                    { return createdBy; }
    public void      setCreatedBy(String v)            { this.createdBy = v; }

    public LocalDate getOpenDate()                     { return openDate; }
    public void      setOpenDate(LocalDate v)          { this.openDate = v; }

    public String    getAssignedTo()                   { return assignedTo; }
    public void      setAssignedTo(String v)           { this.assignedTo = v; }

    public boolean   isMarkedForClosing()              { return markedForClosing; }
    public void      setMarkedForClosing(boolean v)    { this.markedForClosing = v; }

    public String    getClosedBy()                     { return closedBy; }
    public void      setClosedBy(String v)             { this.closedBy = v; }

    public BugStatus getBugStatus()                    { return bugStatus; }
    public void      setBugStatus(BugStatus v)         { this.bugStatus = v; }

    public Severity  getSeverity()                     { return severity; }
    public void      setSeverity(Severity v)           { this.severity = v; }

    @Override
    public String toString() {
        return "Bug{id=" + bugId + ", title=" + bugTitle
                + ", status=" + bugStatus + ", severity=" + severity + "}";
    }
}
