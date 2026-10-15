package net.motikun.jeihistory.history;

/** Internal persistence intent; only deliberate reuse may resurrect a deleted query. */
public enum CommitIntent { AUTOMATIC, EDITED, EXPLICIT }
