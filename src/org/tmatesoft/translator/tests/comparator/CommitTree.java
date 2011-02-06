package org.tmatesoft.translator.tests.comparator;

import java.util.HashMap;
import java.util.Map;


public class CommitTree {

	private CommitTreeNode myRoot;
	private Map<String, Object> myProperties;
	private Map<String, byte[]> myMetaProperties;
	
	public CommitTree() {
		myProperties = new HashMap<String, Object>();
		myRoot = new CommitTreeNode(this, null, "");
	}
	
	public CommitTree copy() {
		CommitTree copy = new CommitTree();
		copy.myRoot = getRoot().copy(copy, null);
		if (myProperties != null) {
			copy.myProperties = new HashMap<String, Object>(myProperties);
		}
		if (myMetaProperties != null) {
			copy.myMetaProperties = new HashMap<String, byte[]>(myMetaProperties);
		}
		
		return copy;
	}

	public CommitTreeNode getRoot() {
		return myRoot;
	}
	
	public Map<String, byte[]> getMetaProperties() {
		return myMetaProperties;
	}
	
	public void setMetaProperty(String name, byte[] value) {
		if (myMetaProperties == null) {
			myMetaProperties = new HashMap<String, byte[]>();
		}
		myMetaProperties.put(name, value);
	}
	
	public void clearMetaProperties() {
		myMetaProperties = null;
	}
	
 	public void setProperty(String name, Object value) {
		myProperties.put(name, value);
	}
	
	public Object getProperty(String name) {
		return myProperties.get(name);
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("tree:\n");
		if (getMetaProperties() != null) {
			result.append("meta properties:\n");
			for (String name : getMetaProperties().keySet()) {
				result.append(name);
				result.append(" = [");
				result.append(PropertiesDifference.toString(getMetaProperties().get(name)));
				result.append("]\n");
			}
		}
		if (myProperties != null) {
			result.append("properties:\n");
			for (String name : myProperties.keySet()) {
				result.append(name);
				result.append(" = [");
				result.append(myProperties.get(name));
				result.append("]\n");
			}
		}
		if (getRoot() != null) {
			result.append("paths:\n");
			CommitTreeIterator iterator = new CommitTreeIterator(this);
			CommitTreeNode root = iterator.next();
			while(root != null) {
				result.append(root);
				result.append('\n');
				root = iterator.next();
			}
		}
		
		return result.toString();
	}
	
	
}
