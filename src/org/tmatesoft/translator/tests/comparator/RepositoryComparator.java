package org.tmatesoft.translator.tests.comparator;

import org.tmatesoft.translator.tests.comparator.git.GitRepositoryComparator;
import org.tmatesoft.translator.tests.comparator.svn.SvnRepositoryComparator;

public abstract class RepositoryComparator {
	
	public static RepositoryComparator create(String r1, String r2) {
		if (r1.endsWith(".git") && r2.endsWith(".git")) {
			return new GitRepositoryComparator().setGitDir1(r1).setGitDir2(r2);
		} else {
			return new SvnRepositoryComparator().setURL1(r1).setURL2(r2);
		}
		
	}
	
	public abstract RepositoryDifference compare() throws Exception;

}
