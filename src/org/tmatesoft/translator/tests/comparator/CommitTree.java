package org.tmatesoft.translator.tests.comparator;

import java.util.HashMap;
import java.util.Map;


public class CommitTree {

	private CommitTreeNode myRoot;
	private IContentLoader myContentLoader;
	private Map<String, Object> myProperties;
	
	public CommitTree(IContentLoader contentLoader) {
		myContentLoader = contentLoader;
		myProperties = new HashMap<String, Object>();
		myRoot = new CommitTreeNode(this, null, "");
	}

	public CommitTreeNode getRoot() {
		return myRoot;
	}
	
	public IContentLoader getContentLoader() {
		return myContentLoader;
	}
	
	public void setProperty(String name, Object value) {
		myProperties.put(name, value);
	}
	
	public Object getProperty(String name) {
		return myProperties.get(name);
	}
}
