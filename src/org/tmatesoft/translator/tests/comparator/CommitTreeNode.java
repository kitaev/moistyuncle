package org.tmatesoft.translator.tests.comparator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CommitTreeNode {

	private CommitTree myTree;
	private Map<String, byte[]> myProperties;
	private byte[] myContents;
	private Map<String,CommitTreeNode> myChildren;

	private String myName;
	private CommitTreeNode myParent;
	private String myId;
	private String myContentChecksum;

	public CommitTreeNode(CommitTree tree, CommitTreeNode parent, String name) {
		this(tree, parent, name, null);
	}
	
	public CommitTreeNode(CommitTree tree, CommitTreeNode parent, String name, String id) {
		myTree = tree;
		myParent = parent;
		myName = name;
		myProperties = new TreeMap<String, byte[]>();
		myChildren = new TreeMap<String, CommitTreeNode>();
		myId = id;
	}
	
	public void setProperty(String name, byte[] value) {
		if (value == null) {
			myProperties.remove(name);
		} else {
			myProperties.put(name, value);
		}
	}
	
	public byte[] getContent() {
		if (myContents == null && hasId() && getTree().getContentLoader() != null) {
			if (getTree().getContentLoader().hasContent(getId())) {
				myContents = getTree().getContentLoader().loadContent(getId());
			}
		}
		return myContents;
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
		if (myContentChecksum != null) {
			return false;
		}
		if (getTree().getContentLoader() != null && hasId()) {
			return !getTree().getContentLoader().hasContent(getId());
		}
		return getContent() == null;
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

	public void setContent(byte[] contents, String contentChecksum) {
		myContents = contents;
		myContentChecksum = contentChecksum;
	}
	
	public boolean hasPropertyDifference(CommitTreeNode node) {
		Map<String, byte[]> other = node.getProperties();
		for (String name : getProperties().keySet()) {
			byte[] otherValue = other.get(name);
			if (!compare(getProperties().get(name), otherValue)) {
				return true;
			}
		}
		for (String name : other.keySet()) {
			byte[] value = getProperties().get(name);
			if (!compare(other.get(name), value)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasContentsDifference(CommitTreeNode node) {
		if (myContentChecksum != null && node.myContentChecksum != null &&
				myContentChecksum.equals(node.myContentChecksum)) {
			return false;
		}
		return !compare(getContent(), node.getContent());
	}
	
	public boolean hasTypeDifference(CommitTreeNode node) {
		return isDirectory() != node.isDirectory();
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

	@Override
	public String toString() {
		return getPath();
	}
	
}
