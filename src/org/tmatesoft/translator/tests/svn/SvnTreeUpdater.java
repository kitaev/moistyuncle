package org.tmatesoft.translator.tests.svn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Stack;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.ISVNReporter;
import org.tmatesoft.svn.core.io.ISVNReporterBaton;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.diff.SVNDeltaProcessor;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;
import org.tmatesoft.translator.tests.comparator.CommitTree;
import org.tmatesoft.translator.tests.comparator.CommitTreeNode;

public class SvnTreeUpdater implements ISVNEditor, ISVNReporterBaton {

	private SVNRepository myRepository;
	private long myLatestRevision;
	private long myCurrentRevision;
	private CommitTree myTree;
	
	private Stack<CommitTreeNode> myCurrentPath;
	private SVNDeltaProcessor myDeltaProcessor;
	private ByteArrayOutputStream myNewContents;
	
	public SvnTreeUpdater(SVNRepository repository) throws SVNException {
		myRepository = repository;
		myLatestRevision = myRepository.getLatestRevision();
		myCurrentRevision = 0;
		myTree = new CommitTree(null);
		myCurrentPath = new Stack<CommitTreeNode>();
		myDeltaProcessor = new SVNDeltaProcessor();
	}
	
	public void close() {
		if (myRepository != null) {
			myRepository.closeSession();
		}
	}
	
	public boolean hasNext() {
		return myLatestRevision > myCurrentRevision;
	}
	
	public CommitTree next() throws SVNException {
		final SVNLogEntry[] logEntry = new SVNLogEntry[1];
		myRepository.log(null, myCurrentRevision + 1,  myCurrentRevision, true, true, 1, false, null, new ISVNLogEntryHandler() {
			public void handleLogEntry(SVNLogEntry entry) throws SVNException {
				logEntry[0] = entry;
			}
		});
		myRepository.update(myCurrentRevision + 1, null, SVNDepth.INFINITY, false, this, this);
		myCurrentRevision = (Long) myTree.getProperty("svn:revision");
		return myTree;
	}

	public void report(ISVNReporter reporter) throws SVNException {
		reporter.setPath("", null, myCurrentRevision, SVNDepth.INFINITY, false);
		reporter.finishReport();
	}

	public void applyTextDelta(String path, String baseChecksum) throws SVNException {
		// save current contents.
		byte[] content = getCurrentNode().getContent();
		if (content == null) {
			content = new byte[0];
		}
		myNewContents = new ByteArrayOutputStream();
		myDeltaProcessor.applyTextDelta(new ByteArrayInputStream(content), myNewContents, true);
	}

	public OutputStream textDeltaChunk(String path, SVNDiffWindow diffWindow) throws SVNException {
		return myDeltaProcessor.textDeltaChunk(diffWindow);
	}

	public void textDeltaEnd(String path) throws SVNException {
		String contentChecksum = myDeltaProcessor.textDeltaEnd();
		SVNFileUtil.closeFile(myNewContents);

		getCurrentNode().setContent(myNewContents.toByteArray(), contentChecksum);
		myNewContents = null;
	}

	public void targetRevision(long revision) throws SVNException {
		myTree.setProperty("svn:revision", new Long(revision));
	}

	public void openRoot(long revision) throws SVNException {
		myCurrentPath.push(myTree.getRoot());
	}

	public void deleteEntry(String path, long revision) throws SVNException {
		getCurrentNode().removeChild(getNodeName(path));
	}

	public void addDir(String path, String copyFromPath, long copyFromRevision)	throws SVNException {
		CommitTreeNode newDir = getCurrentNode().addChild(getNodeName(path));
		myCurrentPath.push(newDir);
	}

	public void openDir(String path, long revision) throws SVNException {
		CommitTreeNode dir = getCurrentNode().getChild(getNodeName(path));
		myCurrentPath.push(dir);
	}

	public void changeDirProperty(String name, SVNPropertyValue value) throws SVNException {
		if (SVNProperty.isWorkingCopyProperty(name) || SVNProperty.isEntryProperty(name)) {
			return;
		}
		if (value != null) {
			try {
				getCurrentNode().setProperty(name, value.isBinary() ? value.getBytes() : value.getString().getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			getCurrentNode().setProperty(name, null);
		}
	}

	public void closeDir() throws SVNException {
		myCurrentPath.pop();
	}

	public void addFile(String path, String copyFromPath, long copyFromRevision) throws SVNException {
		addDir(path, copyFromPath, copyFromRevision);
	}

	public void openFile(String path, long revision) throws SVNException {
		openDir(path, revision);
	}

	public void changeFileProperty(String path, String propertyName, SVNPropertyValue propertyValue) throws SVNException {
		changeDirProperty(propertyName, propertyValue);
	}

	public void closeFile(String path, String textChecksum) throws SVNException {
		closeDir();
	}

	public SVNCommitInfo closeEdit() throws SVNException {
		myCurrentPath.clear();
		return null;
	}

	public void abortEdit() throws SVNException {
	}

	public void absentDir(String path) throws SVNException {
	}

	public void absentFile(String path) throws SVNException {
	}
	
	private CommitTreeNode getCurrentNode() {
		return myCurrentPath.peek();
	}

	private String getNodeName(String path) {
		return SVNPathUtil.tail(path);
	}
}
