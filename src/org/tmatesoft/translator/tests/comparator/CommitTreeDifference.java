package org.tmatesoft.translator.tests.comparator;

import java.util.Map;
import java.util.TreeMap;

public class CommitTreeDifference {
	
	private CommitTreeWalker myCommitTreeWalker;
	private Map<String, Pair> myDifference;
	
	public static class Pair {

		public static final int TYPE = 1;
		public static final int PROPERTIES = 2;
		public static final int CONTENT = 4;

		
		final CommitTreeNode left;
		final CommitTreeNode right;
		final int mask;
		
		private Pair(CommitTreeNode l, CommitTreeNode r, int m) {
			left = l;
			right = r;
			mask = m;
		}
		
		public String toString() {
			if (left == null) {
				return "A  " + right.getPath();
			} else if (right == null) {
				return "D  " + left.getPath();
			} else {
				if ((mask & TYPE) != 0) {
					return "R  " + left.getPath();
				} else {
					String m = ((mask & CONTENT) != 0) ? "M" : " ";
					m += ((mask & PROPERTIES) != 0) ? "M " : "  ";
					return m + left.getPath();
				}
			}
		}
	}

	public CommitTreeDifference(CommitTree left, CommitTree right) {
		myCommitTreeWalker = new CommitTreeWalker(left, right);
	}
	
	public Map<String, Pair> compute() {
		if (myDifference != null) {
			return myDifference;
		}
		myDifference = new TreeMap<String, CommitTreeDifference.Pair>();
		while(myCommitTreeWalker.getCurrentPath() != null) {
			CommitTreeNode leftNode = myCommitTreeWalker.getLeftNode();
			CommitTreeNode rightNode = myCommitTreeWalker.getRightNode();
			
			if (leftNode == null) {
				myDifference.put(myCommitTreeWalker.getCurrentPath(), new Pair(null, rightNode, 0));
				myCommitTreeWalker.skipChildren();
			} else if (rightNode == null) {
				myDifference.put(myCommitTreeWalker.getCurrentPath(), new Pair(leftNode, null, 0));
				myCommitTreeWalker.skipChildren();
			} else  {
				int mask = 0;
				if (leftNode.hasTypeDifference(rightNode)) {
					mask |= Pair.TYPE;
					myCommitTreeWalker.skipChildren();
				}  else {
					if (leftNode.hasPropertyDifference(rightNode)) {
						mask |= Pair.PROPERTIES;
					}
					if (leftNode.hasContentsDifference(rightNode)) {
						mask |= Pair.CONTENT;
					}
					myCommitTreeWalker.next();
				}
				if (mask != 0) {
					myDifference.put(leftNode.getPath(), new Pair(leftNode, rightNode, mask));
				} 
			}
		}
		myCommitTreeWalker = null;
		return myDifference;
	}
	
	
	public boolean isEmpty() {
		return compute().isEmpty();
	}
	
	public String toString() {
		if (myDifference == null) {
			return "<not computed yet>";
		}
		if (isEmpty()) {
			return "<empty>";
		}
		StringBuffer buffer = new StringBuffer();
		Map<String, Pair> diff = compute();
		for (String path : diff.keySet()) {
			Pair p = diff.get(path);
			buffer.append(p.toString());
			buffer.append("\n");
		}
		return buffer.toString();
	}
	
	
}
