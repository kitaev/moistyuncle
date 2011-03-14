package org.tmatesoft.translator.tests.comparator.git;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.tmatesoft.translator.tests.comparator.CommitTreeDifference;

public class GitRepositoryGraph {
	
	private Map<ObjectId, CommitTreeDifference> myLeftCommits;
	private Map<ObjectId, CommitTreeDifference> myRightCommits;

	public GitRepositoryGraph() {
		myLeftCommits = new HashMap<ObjectId, CommitTreeDifference>();
		myRightCommits = new HashMap<ObjectId, CommitTreeDifference>();
	}
	
	public void build(RevCommit leftRoot, RevCommit rightRoot) {
		LinkedList<RevCommit> leftPath = buildCommitsList(leftRoot, myLeftCommits);
		LinkedList<RevCommit> rightPath = buildCommitsList(leftRoot, myRightCommits);
		
		// match commits in the lists and build difference objects.
		// commits in the lists are not yet matched.
		leftPath.
		for(RevCommit commit: leftPath.listIterator().)
		
		Map<String, RevCommit> leftPathMap = new LinkedHashMap<String, RevCommit>();
		Map<String, RevCommit> rightPathMap = new LinkedHashMap<String, RevCommit>();
	}
	
	private LinkedList<RevCommit> buildCommitsList(RevCommit commit, Map<ObjectId, CommitTreeDifference> existing) {
		LinkedList<RevCommit> result = new LinkedList<RevCommit>();

		while (commit != null) {
			ObjectId id = commit.getId();
			if (existing.containsKey(id)) {
				break;
			}
			result.add(commit);
			commit = commit.getParent(0);
		}
		return result;
	}
	
	
}
