package org.tmatesoft.translator.tests.comparator;

import java.util.LinkedList;
import java.util.List;

public class RepositoryDifference {
	
	private List<CommitTreeDifference> myCommitTreeDifferences;
	private PropertiesDifference myReferencesDifference;
	private String myLeftName;
	private String myRightName;
	
	public RepositoryDifference(String leftName, String rightName) {
		myLeftName = leftName;
		myRightName = rightName;
	}
	
	public String getLeftName() {
		return myLeftName;
	}
	
	public String getRightName() {
		return myRightName;
	}

	public void addCommitDifference(CommitTreeDifference commitTreeDifference) {
		getCommitDifferences().add(commitTreeDifference);
	}
	
	public List<CommitTreeDifference> getCommitDifferences() {
		if (myCommitTreeDifferences == null) {
			myCommitTreeDifferences = new LinkedList<CommitTreeDifference>();
		}
		return myCommitTreeDifferences;
	}
	
	public boolean hasCommitTreeDifferences() {
		return myCommitTreeDifferences != null && !myCommitTreeDifferences.isEmpty();
	}
	
	public PropertiesDifference getReferencesDifference() {
		return myReferencesDifference;
	}
	
	public void setReferencesDifference(PropertiesDifference difference) {
		myReferencesDifference = difference;
	}
	
	public boolean hasReferencesDifference() {
		return myReferencesDifference != null && !myReferencesDifference.isEmpty();
	}
	
	public boolean isEmpty() {
		return !hasReferencesDifference() && !hasCommitTreeDifferences();
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(getLeftName());
		result.append(" vs ");
		result.append(getRightName());
		if (isEmpty()) {
			result.append(": repositories are identical");
			return result.toString();
		}
		result.append(":\n");
		if (hasReferencesDifference()) {
			result.append("references:\n");
			result.append(getReferencesDifference());
		}
		if (hasCommitTreeDifferences()) {
			result.append("commits:\n");
			for (CommitTreeDifference diff : getCommitDifferences()) {
				result.append(diff);
			}
		}
		return result.toString();
	}

}
