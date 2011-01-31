package org.tmatesoft.translator.tests.svn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.diff.SVNDeltaProcessor;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;

public class SvnTreeNode {

	private SvnTree myTree;
	private Map<String, byte[]> myProperties;
	private byte[] myContents;
	private Map<String,SvnTreeNode> myChildren;
	
	private SVNDeltaProcessor myDeltaProcessor;
	private ByteArrayOutputStream myTarget;
	private String myName;
	private SvnTreeNode myParent;
	
	public SvnTreeNode(SvnTree tree, SvnTreeNode parent, String name) {
		myTree = tree;
		myParent = parent;
		myName = name;
		myProperties = new HashMap<String, byte[]>();
		myChildren = new HashMap<String, SvnTreeNode>();
	}
	
	public void setProperty(String name, byte[] value) {
		if (value == null) {
			myProperties.remove(name);
		} else {
			myProperties.put(name, value);
		}
	}
	
	public byte[] getContents() {
		return myContents;
	}
	
	public SvnTreeNode getParent() {
		return myParent;
	}
	
	public String getPath() {
		String path = "";
		SvnTreeNode node = this;
		while(node != null) {
			path = "/" + node.getName() + path;
			node = node.getParent();
		}
		return path;
	}
	
	public void updateContents(SVNDiffWindow diffWindow) throws SVNException {
		if (myContents == null) {
			myContents = new byte[0];
		}
		if (diffWindow == null) {
			if (myDeltaProcessor != null) {
				myDeltaProcessor.textDeltaEnd();
				myContents = myTarget.toByteArray();
				myTarget = null;
				myDeltaProcessor = null;
			}			
		} else {
			if (myDeltaProcessor == null) {
				myDeltaProcessor = getTree().getDeltaProcessor();
				myTarget = new ByteArrayOutputStream();
				myDeltaProcessor.applyTextDelta(new ByteArrayInputStream(getContents()), myTarget, false);
			}
			myDeltaProcessor.textDeltaChunk(diffWindow);
		}
	}
	
	public boolean isDirectory() {
		return myContents == null;
	}
	
	public SvnTreeNode addChild(String name) {
		if (myChildren == null) {
			myChildren = new HashMap<String, SvnTreeNode>();
		}
		SvnTreeNode child = new SvnTreeNode(getTree(), this, name);
		myChildren.put(name, child);
		return child;
	}
	
	public void removeChild(String name) {
		if (myChildren != null) {
			myChildren.remove(name);
		}
	}

	private SvnTree getTree() {
		return myTree;
	}
	
	private String getName() {
		return myName;
	}

	public SvnTreeNode getChild(String nodeName) {
		if (myChildren != null) {
			return myChildren.get(nodeName);
		}
		return null;
	}

	public void println(PrintStream out) {
		println(0, out);
	}
	public void println(int level, PrintStream out) {
		if (level > 0) {
			StringBuffer buffer = new StringBuffer();
			for(int i = 0; i < level; i++) {
				buffer.append(' ');
			}
			out.print(buffer);
		}
		out.print(getName());
		
		if (myContents != null) {
			out.printf("@%d\n", myContents.length);
		} else {
			out.println('/');
		}
		
		if (myChildren != null) {
			for (String name : myChildren.keySet()) {
				SvnTreeNode node = getChild(name);
				node.println(level + 1, out);
			}
		}
		
	}
	
	private Map<String, byte[]> getProperties() {
		return myProperties;
	}

	public void compareTo(SvnTreeNode node, PrintStream out) {
		if (node == null) {
			out.printf("tree: expected tree node at '%s', but was NULL\n", getPath());
			return;
		}
		// compare type.
		if (isDirectory() != node.isDirectory()) {
			out.printf("tree: '%s' type differs, expected '%s' but was '%s'\n", getPath(), isDirectory() ? "dir" : "file", node.isDirectory() ? "dir" :"file");
		}
		
		// compare properties
		Map<String, byte[]> p1 = getProperties();
		Map<String, byte[]> p2 = node.getProperties();
		for (String name1 : p1.keySet()) {
			String v1 = toString(p1.get(name1));
			if (!p2.containsKey(name1)) {
				out.printf("tree: '%s' properties differs, expected: '%s'=='%s', actual missing\n", getPath(), name1, v1);
			} else if (!compare(p1.get(name1), p2.get(name1))) {
				String v2 = toString(p2.get(name1));
				out.printf("tree: '%s' properties differs, expected: '%s'=='%s', actual: '%s'=='%s'\n", getPath(), name1, v1, name1, v2);
			}
		}
		for (String name2 : p2.keySet()) {
			if (!p1.containsKey(name2)) {
				String v2 = toString(p2.get(name2));
				out.printf("tree: '%s' properties differs, expected nothig, actual: '%s'=='%s'\n", getPath(), name2, v2);
			}
		}
		// compare contents
		if (!isDirectory() && !node.isDirectory() && !compare(getContents(), node.getContents())) {
			String c1 = toString(getContents());
			String c2 = toString(node.getContents());
			out.printf("tree: '%s' contents differs, expected '%s', actual: '%s'\n", getPath(), c1, c2);

		}

		// compare children
		Map<String, SvnTreeNode> ch1 = myChildren;
		Map<String, SvnTreeNode> ch2 = node.myChildren;
		Map<String, SvnTreeNode[]> children = new HashMap<String, SvnTreeNode[]>();
		for (String name : ch1.keySet()) {
			SvnTreeNode child1 = ch1.get(name);
			SvnTreeNode child2 = ch2.get(name);
			if (child2 == null) {
				out.printf("tree: '%s' children differs, expected child '%s', but it is missing\n", getPath(), name);
			} else {
				children.put(name, new SvnTreeNode[] {child1, child2});
			}
		}

		for (String name : ch2.keySet()) {
			SvnTreeNode child1 = ch1.get(name);
			if (child1 == null) {
				out.printf("tree: '%s' children differs, unexpected child '%s' present\n", getPath(), name);
			} 
		}
		// recurse.
		for (String name : children.keySet()) {
			children.get(name)[0].compareTo(children.get(name)[1], out);
		}
	}

	private String toString(byte[] c) {
		String c1 = "";
		try {
			c1 = new String(c, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			c1 = e.getMessage();
		}
		return c1;
	}

	private static boolean compare(byte[] bs, byte[] bs2) {
		if (bs == bs2) {
			return true;
		}
		if (bs == null || bs2 == null) {
			return false;
		}
		if (bs.length != bs2.length) {
			return false;
		}
		for (int i = 0; i < bs2.length; i++) {
			if (bs[i] != bs2[i]) {
				return false;
			}
		}
		return true;
	}
}
