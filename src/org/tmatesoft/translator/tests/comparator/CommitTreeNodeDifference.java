package org.tmatesoft.translator.tests.comparator;

import java.util.Map;

public class CommitTreeNodeDifference {

	private CommitTreeNode myLeftNode;
	private CommitTreeNode myRightNode;
	
	private boolean myIsStructuralDifference;
	private ContentDifference myContentDifference;
	private PropertiesDifference myPropertiesDifference;
	private String myPath;

	public CommitTreeNodeDifference(CommitTreeNode left, CommitTreeNode right) {
		myLeftNode = left;
		myRightNode = right;
	}
	
	public String getPath() {
		compute();
		return myPath;
	}
	
	public CommitTreeNode getLeftNode() {
		return myLeftNode;
	}
	
	public CommitTreeNode getRightNode() {
		return myRightNode;
	}
	
	public boolean isStructuralDifference() {
		compute();
		return myIsStructuralDifference;
	}
	
	public ContentDifference getContentDifference() {
		compute();
		return myContentDifference;
	}
	
	public PropertiesDifference getPropertiesDifference() {
		compute();
		return myPropertiesDifference;
	}
	
	public boolean hasContentDifference() {
		return getContentDifference() != null;
	}

	public boolean hasPropertiesDifference() {
		return getPropertiesDifference() != null;
	}
	
	public boolean isEmpty() {
		compute();
		return !isStructuralDifference() && !hasContentDifference() && !hasPropertiesDifference();
	}
	
	private void compute() {
		if (myPath != null) {
			return;
		}
		if (getLeftNode() == null || getRightNode() == null || getLeftNode().isDirectory() != getRightNode().isDirectory()) {
			myIsStructuralDifference = true;
		} else {
			if (!getLeftNode().isDirectory()) {
				if (getLeftNode().hasContentId() && getRightNode().hasContentId() && 
						getLeftNode().getContentId().equals(getRightNode().getContentId())) {
					// no difference.
				} else {
					ContentDifference contentDifference = new ContentDifference(getLeftNode().getContent(), getRightNode().getContent());
					if (!contentDifference.isEmpty()) {
						myContentDifference = contentDifference;
					}
				}
			}
			Map<String, byte[]> leftProperties = getLeftNode().getProperties();
			Map<String, byte[]> rightProperties = getLeftNode().getProperties();
			PropertiesDifference propertiesDifference = new PropertiesDifference(leftProperties, rightProperties);
			if (!propertiesDifference.isEmpty()) {
				myPropertiesDifference = propertiesDifference;
			}
		}
		myPath = myLeftNode == null ? (myRightNode == null ? "?" : myRightNode.getPath()) : myLeftNode.getPath();
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "   " + getPath() + " (no difference)";
		}
		StringBuffer result = new StringBuffer();
		
		if (isStructuralDifference()) {
			if (getLeftNode() == null) {
				result.append("+  ");
				result.append(getPath());
			} else if (getRightNode() == null) {
				result.append("-  ");
				result.append(getPath());
			} else {
				result.append("R  ");
				result.append(getPath());
			}
		} else {
			result.append("*  ");
			result.append(getPath());
			result.append("\n");
			if (hasPropertiesDifference()) {
				result.append("   properties:\n");
				result.append(getPropertiesDifference());
			}
			if (hasContentDifference()) {
				result.append("   content:\n");
				result.append(getContentDifference());
			}
		}
		return result.toString();
	}
	
}
