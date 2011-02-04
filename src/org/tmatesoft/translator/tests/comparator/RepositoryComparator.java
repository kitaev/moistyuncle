package org.tmatesoft.translator.tests.comparator;

import java.util.List;

import org.tmatesoft.translator.tests.git.GitRepositoryComparator;
import org.tmatesoft.translator.tests.svn.SvnRepositoryComparator;

public abstract class RepositoryComparator {
	
	public static RepositoryComparator create(String r1, String r2) {
		if (r1.endsWith(".git") && r2.endsWith(".git")) {
			return new GitRepositoryComparator().setGitDir1(r1).setGitDir2(r2);
		} else {
			return new SvnRepositoryComparator().setURL1(r1).setURL2(r2);
		}
		
	}
	
	public abstract List<CommitTreeDifference> compare() throws Exception;

}
