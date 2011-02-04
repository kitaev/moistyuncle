package org.tmatesoft.translator.tests.comparator;

public class CommitTreeWalker {
	
	private CommitTreeIterator myLeftIterator;
	private CommitTreeIterator myRightIterator;
	
	private CommitTreeNode myLeftNode;
	private CommitTreeNode myRightNode;
	
	public CommitTreeWalker(CommitTree left, CommitTree right) {
		myLeftIterator = new CommitTreeIterator(left);
		myRightIterator = new CommitTreeIterator(right);
		
		myLeftNode = myLeftIterator.next();
		myRightNode = myRightIterator.next();
	}
	
	public boolean next() {
		// paths are equal, call next for another time, exit.
		int level = 0;
		if (myLeftNode == null && myRightNode == null) {
			return false;
		} else if (myLeftNode == null) {
			level = 1;
		} else if (myRightNode == null) {
			level = -1;
		} else {
			level = myRightNode.getPath().compareTo(myLeftNode.getPath()); 
		}		
		if (level == 0) {
			myLeftNode = myLeftIterator.next();
			myRightNode = myRightIterator.next();
		} else if (level > 0) {
			myRightNode = myRightIterator.next();
		} else if (level < 0) {
			myLeftNode = myLeftIterator.next();
		}
		return myLeftNode != null || myRightNode != null;
	}
	
	public boolean skipChildren() {
		String path = getCurrentPath();
		myRightIterator.skipChildren(path + "/");
		myLeftIterator.skipChildren(path + "/");
		
		if (myLeftNode != null && (myLeftNode.getPath().startsWith(path + "/") || myLeftNode.getPath().equals(path))) {
			myLeftNode = myLeftIterator.next();
		}
		if (myRightNode != null && (myRightNode.getPath().startsWith(path + "/") || myRightNode.getPath().equals(path))) {
			myRightNode = myRightIterator.next();
		}
		return myRightNode != null || myLeftNode != null;
	}
	
	public CommitTreeNode getLeftNode() {
		if (myLeftNode == null) {
			return null;
		}
		if (myRightNode == null || myLeftNode.getPath().compareTo(myRightNode.getPath()) >= 0) {
			return myLeftNode;
		}
		return null;
	}
	
	public CommitTreeNode getRightNode() {
		if (myRightNode == null) {
			return null;
		}
		if (myLeftNode == null || myRightNode.getPath().compareTo(myLeftNode.getPath()) >= 0) {
			return myRightNode;
		}
		return null;
	}
	
	public String getCurrentPath() {
		return getLeftNode() != null ? getLeftNode().getPath() : (getRightNode() != null ? getRightNode().getPath() : null); 
	}
	
}
