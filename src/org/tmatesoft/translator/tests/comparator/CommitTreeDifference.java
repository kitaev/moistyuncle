package org.tmatesoft.translator.tests.comparator;

import java.util.LinkedList;
import java.util.List;

public class CommitTreeDifference {
	
	private CommitTreeWalker myCommitTreeWalker;
	
	private List<CommitTreeNodeDifference> myNodeDifferences;
	private PropertiesDifference myMetaPropertiesDifference;

	private CommitTree myLeftTree;
	private CommitTree myRightTree;
	private String myCommitName;

	public CommitTreeDifference(String commitName, CommitTree left, CommitTree right) {
		myLeftTree = left;
		myRightTree = right;
		myCommitTreeWalker = new CommitTreeWalker(left, right);
		myCommitName = commitName;
	}
	
	public String getCommitName() {
		return myCommitName;
	}
	
	public CommitTree getLeftTree() {
		return myLeftTree;
	}
	
	public CommitTree getRightTree() {
		return myRightTree;
	}
	
	public boolean isEmpty() {
		compute();
		return !hasMetaPropertiesDifference() && !hasNodeDifferences();
	}
	
	public List<CommitTreeNodeDifference> getNodeDifferences() {
		compute();
		return myNodeDifferences;
	}
	
	public PropertiesDifference getMetaPropertiesDifference() {
		compute();
		return myMetaPropertiesDifference;
	}
	
	public boolean hasMetaPropertiesDifference() {
		return getMetaPropertiesDifference() != null;
	}
	
	public boolean hasNodeDifferences() {
		return getNodeDifferences() != null && !getNodeDifferences().isEmpty();
	}
	
	private void compute() {
		if (myNodeDifferences != null) {
			return;
		}
		myNodeDifferences = new LinkedList<CommitTreeNodeDifference>();
		while(myCommitTreeWalker.getCurrentPath() != null) {
			CommitTreeNode leftNode = myCommitTreeWalker.getLeftNode();
			CommitTreeNode rightNode = myCommitTreeWalker.getRightNode();
			CommitTreeNodeDifference diff = new CommitTreeNodeDifference(leftNode, rightNode);
			if (!diff.isEmpty()) {
				myNodeDifferences.add(diff);
				if (diff.isStructuralDifference()) {
					myCommitTreeWalker.skipChildren();
				} else {
					myCommitTreeWalker.next();
				}
			} else {
				myCommitTreeWalker.next();
			}
			
		}
		
		PropertiesDifference propertiesDiff = new PropertiesDifference(
				getLeftTree() != null ? getLeftTree().getMetaProperties() : null, 
				getRightTree() != null ? getRightTree().getMetaProperties() : null);
		
		if (!propertiesDiff.isEmpty()) {
			myMetaPropertiesDifference = propertiesDiff;
		}
		myCommitTreeWalker = null;
	}

	@Override
	public String toString() {
		if (myNodeDifferences == null) {
			return "not computed yet";
		}
		if (isEmpty()) {
			return getCommitName() + ": commits are identical";
		}
		
		if (getLeftTree() == null) {
			return "+ commit: " + getCommitName() + "\n";
		} else if (getRightTree() == null) {
			return "- commit: " + getCommitName() + "\n";
		}

		StringBuffer result = new StringBuffer();
		result.append("commit: ");
		result.append(getCommitName());
		result.append(":\n");
		if (hasMetaPropertiesDifference()) {
			result.append("meta properties:\n");
			result.append(getMetaPropertiesDifference());
		} 
		if (hasNodeDifferences()) {
			result.append("tree:\n");
			for (CommitTreeNodeDifference diff : getNodeDifferences()) {
				result.append(diff);
				result.append('\n');
			}
		}
		return result.toString();
	}
}