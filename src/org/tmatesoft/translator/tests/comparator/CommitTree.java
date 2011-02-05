package org.tmatesoft.translator.tests.comparator;

import java.util.HashMap;
import java.util.Map;


public class CommitTree {

	private CommitTreeNode myRoot;
	private IContentLoader myContentLoader;
	private Map<String, Object> myProperties;
	private Map<String, byte[]> myMetaProperties;
	
	public CommitTree(IContentLoader contentLoader) {
		myContentLoader = contentLoader;
		myProperties = new HashMap<String, Object>();
		myRoot = new CommitTreeNode(this, null, "");
	}
	
	public CommitTree copy() {
		CommitTree copy = new CommitTree(myContentLoader);
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
	
	public IContentLoader getContentLoader() {
		return myContentLoader;
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
}
