package org.tmatesoft.translator.tests.svn;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;

import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.internal.util.SVNDate;
import org.tmatesoft.svn.core.io.diff.SVNDeltaProcessor;

public class SvnTree {

	private long myRevision;
	private SVNDeltaProcessor myDeltaProcessor;
	private SvnTreeNode myRoot;
	private SVNLogEntry myLogEntry;
	
	public SvnTree() {
		setRevision(0);
		setRoot(new SvnTreeNode(this, null, ""));
	}
	
	private void setRoot(SvnTreeNode root) {
		myRoot = root;
	}

	public long getRevision() {
		return myRevision;
	}
	
	public void setRevision(long revision) {
		myRevision = revision;
	}
	
	public SvnTreeNode getRoot() {
		return myRoot;
	}
	
	public SVNDeltaProcessor getDeltaProcessor() {
		if (myDeltaProcessor == null) {
			myDeltaProcessor = new SVNDeltaProcessor();
		}
		return myDeltaProcessor;
	}

	public void println(PrintStream out) {
		out.printf("=== Tree, revision %d by %s at %s\n", getRevision(), myLogEntry.getAuthor(), SVNDate.formatHumanDate(myLogEntry.getDate(), null));
		out.printf("=== %s\n\n", myLogEntry.getMessage());
		@SuppressWarnings("rawtypes")
		Map changedPaths = myLogEntry.getChangedPaths();
		for(@SuppressWarnings("rawtypes")
		Iterator paths = changedPaths.keySet().iterator(); paths.hasNext();) {
			String path = (String) paths.next();
			SVNLogEntryPath logEntryPath = (SVNLogEntryPath) changedPaths.get(path);
			out.printf("%s %s", logEntryPath.getType(), logEntryPath.getPath());
			if (logEntryPath.getCopyPath() != null) {
				out.printf(" copied from %s@%d\n", logEntryPath.getCopyPath(), logEntryPath.getCopyRevision());
			} else {
				out.println();
			}
		}
		out.println();
		out.println("===");
		getRoot().println(out);
	}

	public void setLog(SVNLogEntry logEntry) {
		myLogEntry = logEntry;
	}
	
	public SVNLogEntry getLog() {
		return myLogEntry;
	}
	
	public void compareTo(SvnTree otherTree, PrintStream out) {
		if (otherTree == null) {
			out.printf("Expected some tree, but was NULL\n");
		}
		// 1. compare message, author and copyfrom information on paths.
		compare(getLog(), otherTree.getLog(), out);
		
		// 2. compare trees, reporting what is missing, what exist when should not, and what differs and how.
		getRoot().compareTo(otherTree.getRoot(), out);
	}
	
	private void compare(SVNLogEntry log1, SVNLogEntry log2, PrintStream out) {
		if (log1 == log2) {
			return;
		} else if (log1 == null) {
			out.printf("Log entry differs:\nexpected: 'NULL'\nactual'NOT NULL'\n");
		} else if (log2 == null) {
			out.printf("Log entry differs:\nexpected: 'NOT NULL'\nactual'NULL'\n");
		} else {
			String m1 = log1.getMessage();
			String m2 = log2.getMessage();
			if (!compare(m1, m2)) {
				out.printf("Log message differs:\nexpected: '%s'\nactual'%s'\n", m1, m2);
			}
	
			String a1 = log1.getAuthor();
			String a2 = log2.getAuthor();
			if (!compare(a1, a2)) {
				out.printf("Author differs:\nexpected: '%s'\nactual'%s'\n", a1, a2);
			}
			
			@SuppressWarnings({ "rawtypes" })
			Map ps1 = log1.getChangedPaths();
			@SuppressWarnings({ "rawtypes" })
			Map ps2 = log2.getChangedPaths();
			
			for(@SuppressWarnings("rawtypes") Iterator paths = ps1.keySet().iterator(); paths.hasNext();) {
				String path = (String) paths.next();
				SVNLogEntryPath lep1 = (SVNLogEntryPath) ps1.get(path);
				SVNLogEntryPath lep2 = (SVNLogEntryPath) ps2.get(path);
				if (lep2 == null) {
					out.printf("Path is missing in the expected log entry: '%s'\n", path);
					continue;
				} 
				if (lep2.getType() != lep1.getType()) {
					out.printf("Change type of '%s' differs:\nexpected: '%s'\nactual: '%s'\n", path, lep1.getType(), lep2.getType());
				} 
				if (lep1.getCopyPath() != null) {
					if (!compare(lep1.getCopyPath(), lep2.getCopyPath())) {
						out.printf("Copy origin of '%s' differs:\nexpected: '%s'\nactual: '%s'\n", path, lep1.getCopyPath(), lep2.getCopyPath());
					}
					if (lep1.getCopyRevision() != lep2.getCopyRevision()) {
						out.printf("Copy origin revision of '%s' differs:\nexpected: %d\nactual: %d\n", path, lep1.getCopyRevision(), lep2.getCopyRevision());
					}
				}
			}
			for(@SuppressWarnings("rawtypes") Iterator paths = ps2.keySet().iterator(); paths.hasNext();) {
				String path = (String) paths.next();
				SVNLogEntryPath lep1 = (SVNLogEntryPath) ps1.get(path);
				if (lep1 == null) {
					out.printf("Path is missing in the actual log entry: '%s'\n", path);
					continue;
				} 
			}
		}
	}

	private static boolean compare(String m1, String m2) {
		if (m1 == m2) {
			return true;
		}
		if (m1 == null || m2 == null) {
			return false;
		}
		return m1.equals(m2);
	}
}
