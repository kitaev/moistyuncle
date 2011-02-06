package org.tmatesoft.translator.tests.comparator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CommitTreeNode {

	private CommitTree myTree;
	private Map<String, byte[]> myProperties;
	private byte[] myContent;
	private Map<String,CommitTreeNode> myChildren;

	private String myName;
	private CommitTreeNode myParent;
	private String myId;
	private String myContentChecksum;
	private IContentLoader myContentLoader;

	public CommitTreeNode(CommitTree tree, CommitTreeNode parent, String name) {
		myTree = tree;
		myParent = parent;
		myName = name;
	}

	public CommitTreeNode copy(CommitTree copyTree, CommitTreeNode copyParent) {
		CommitTreeNode copy = new CommitTreeNode(copyTree, copyParent, myName);
		copy.myId = myId;
		copy.myContentChecksum = myContentChecksum;
		copy.myContent = myContent;
		if (myProperties != null) {
			copy.myProperties = new HashMap<String, byte[]>(myProperties);
		}
		if (myChildren != null) {
			copy.myChildren = new HashMap<String, CommitTreeNode>();
			for (CommitTreeNode child : myChildren.values()) {
				copy.myChildren.put(child.getName(), child.copy(copyTree, copy));
			}
		}
		return copy;
	}
	
	public void setProperty(String name, byte[] value) {
		if (value == null) {
			if (myProperties != null) {
				myProperties.remove(name);
			}
		} else {
			if (myProperties == null) {
				myProperties = new HashMap<String, byte[]>();
			}
			myProperties.put(name, value);
		}
	}
	
	public IContentLoader getContentLoader() {
		return myContentLoader;
	}
	
	public CommitTreeNode getParent() {
		return myParent;
	}
	
	public String getPath() {
		String path = getName();
		CommitTreeNode node = getParent();
		while(node != null) {
			path = node.getName() + "/" + path;
			node = node.getParent();
		}
		return "".equals(path) ? "/" : path;
	}
	
	public boolean isDirectory() {
		if (getContentId() != null) {
			return false;
		}
		return getContentLoader() == null;
	}
	
	public CommitTreeNode addChild(String name) {
		if (myChildren == null) {
			myChildren = new HashMap<String, CommitTreeNode>();
		}
		CommitTreeNode child = new CommitTreeNode(getTree(), this, name);
		myChildren.put(name, child);
		return child;
	}
	
	public void setId(String id) {
		myId = id;
		myContentChecksum = id;
	}
	
	public void removeChild(String name) {
		if (myChildren != null) {
			myChildren.remove(name);
		}
	}

	private CommitTree getTree() {
		return myTree;
	}
	
	private String getName() {
		return myName;
	}

	public CommitTreeNode getChild(String nodeName) {
		if (myChildren != null) {
			return myChildren.get(nodeName);
		}
		return null;
	}
	
	public Map<String, CommitTreeNode> getChildren() {
		if (myChildren == null) {
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(myChildren);
	}
	
	public Map<String, byte[]> getProperties() {
		return myProperties;
	}

	public boolean hasId() {
		return myId != null;
	}
	
	public String getId() {
		return myId;
	}
	
	public String getContentId() {
		return myContentChecksum;
	}
	
	public boolean hasContentId() {
		return getContentId() != null;
	}

	public void setContent(IContentLoader loader, String contentChecksum) {
		myContentLoader = loader;
		myContentChecksum = contentChecksum;
	}

	@Override
	public String toString() {
		return getPath();
	}
	
}
