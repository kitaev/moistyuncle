package org.tmatesoft.translator.tests.comparator;

import java.util.Iterator;
import java.util.LinkedList;

public class CommitTreeIterator {
	
	private CommitTree myTree;
	private CommitTreeNode myCurrentNode;
	
	private LinkedList<CommitTreeNode> myNodesToVisit;
	
	public CommitTreeIterator(CommitTree tree) {
		myTree = tree;
		myNodesToVisit = new LinkedList<CommitTreeNode>();
		if (myTree != null) {
			myNodesToVisit.add(myTree.getRoot());
		}
		advance();
	}

	public CommitTreeNode next() {
		if (myCurrentNode == null) {
			advance();
		}
		try {
			return myCurrentNode;
		} finally {
			myCurrentNode = null;
		}
	}
	
	private void advance() {
		if (myNodesToVisit.isEmpty()) {
			return;
		}
		myCurrentNode = myNodesToVisit.removeLast();
		for (String name : myCurrentNode.getChildren().keySet()) {
			myNodesToVisit.addLast(myCurrentNode.getChild(name));
		}
	}

	public void skipChildren(String pathPrefix) {
		for(Iterator<CommitTreeNode> nodes = myNodesToVisit.iterator(); nodes.hasNext();) {
			CommitTreeNode node = nodes.next();
			if (node.getPath().startsWith(pathPrefix)) {
				nodes.remove();
			}
		}
	}
}
