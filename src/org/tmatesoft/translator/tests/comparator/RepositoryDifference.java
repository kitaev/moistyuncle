package org.tmatesoft.translator.tests.comparator;

import java.util.LinkedList;
import java.util.List;

public class RepositoryDifference {
	
	private List<CommitTreeDifference> myCommitTreeDifferences;

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

}
