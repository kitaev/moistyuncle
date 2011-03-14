package org.tmatesoft.translator.tests.comparator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CommitTreeDifference {
	
	private List<CommitTreeNodeDifference> myNodeDifferences;
	private PropertiesDifference myMetaPropertiesDifference;

	private CommitTree myLeftTree;
	private CommitTree myRightTree;
	private String myCommitName;
	
	private List<CommitTreeDifference> myParents;

	public CommitTreeDifference(String commitName, CommitTree left, CommitTree right) {
		myLeftTree = left;
		myRightTree = right;
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
	
	public int getParentsCount() {
		return myParents == null ? 0 : myParents.size();
	}
	
	public CommitTreeDifference getParent(int nth) {
		return myParents == null || nth >= myParents.size() ? null : myParents.get(nth);
	}
	
	public CommitTreeDifference[] getParents() {
		return myParents == null ? new CommitTreeDifference[0] : myParents.toArray(new CommitTreeDifference[myParents.size()]);
	}
	
	public void addParent(CommitTreeDifference parent) {
		if (myParents == null) {
			myParents = new ArrayList<CommitTreeDifference>();
		}
		myParents.add(parent);
	}
	
	private void compute() {
		if (myNodeDifferences != null) {
			return;
		}
		myNodeDifferences = new LinkedList<CommitTreeNodeDifference>();
		CommitTreeWalker walker = new CommitTreeWalker(getLeftTree(), getRightTree());
		while(walker.getCurrentPath() != null) {
			CommitTreeNode leftNode = walker.getLeftNode();
			CommitTreeNode rightNode = walker.getRightNode();
			CommitTreeNodeDifference diff = new CommitTreeNodeDifference(leftNode, rightNode);
			if (!diff.isEmpty()) {
				myNodeDifferences.add(diff);
				if (diff.isStructuralDifference()) {
					walker.skipChildren();
				} else {
					walker.next();
				}
			} else {
				walker.next();
			}
		}
		
		PropertiesDifference propertiesDiff = new PropertiesDifference(
				getLeftTree() != null ? getLeftTree().getMetaProperties() : null, 
				getRightTree() != null ? getRightTree().getMetaProperties() : null);
		
		if (!propertiesDiff.isEmpty()) {
			myMetaPropertiesDifference = propertiesDiff;
		}
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