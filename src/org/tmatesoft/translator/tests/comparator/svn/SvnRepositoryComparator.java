package org.tmatesoft.translator.tests.comparator.svn;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.translator.tests.comparator.CommitTree;
import org.tmatesoft.translator.tests.comparator.CommitTreeDifference;
import org.tmatesoft.translator.tests.comparator.RepositoryComparator;
import org.tmatesoft.translator.tests.comparator.RepositoryDifference;

public class SvnRepositoryComparator extends RepositoryComparator {
	
	private String myURL1;
	private String myURL2;

	public SvnRepositoryComparator() {
		SVNRepositoryFactoryImpl.setup();
		DAVRepositoryFactory.setup();
		FSRepositoryFactory.setup();
	}
	
	public SvnRepositoryComparator setURL1(String url1) {
		myURL1 = url1;
		return this;
	}

	public SvnRepositoryComparator setURL2(String url2) {
		myURL2 = url2;
		return this;
	}
	
	/**
	 * @return true if repositories are equal, false otherwise.
	 */	
	public RepositoryDifference compare() throws SVNException {
		SVNRepository r1 = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(myURL1));
		SVNRepository r2 = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(myURL2));
		
		SvnTreeUpdater u1 = new SvnTreeUpdater(r1);
		SvnTreeUpdater u2 = new SvnTreeUpdater(r2);
		
		RepositoryDifference repositoryDifference = new RepositoryDifference(myURL1, myURL2);
		try {
			while(u1.hasNext() && u2.hasNext()) {
				CommitTree t1 = u1.next();
				CommitTree t2 = u2.next();
				String name = Long.toString((Long) t1.getProperty("svn:revision"));
				CommitTreeDifference diff = new CommitTreeDifference(name, t1, t2);
				if (!diff.isEmpty()) {
					repositoryDifference.addCommitDifference(diff);
				}
			}
			while (u1.hasNext()) {
				CommitTree t1 = u1.next();
				String name = Long.toString((Long) t1.getProperty("svn:revision"));
				CommitTreeDifference diff = new CommitTreeDifference(name, t1, null);
				if (!diff.isEmpty()) {
					repositoryDifference.addCommitDifference(diff);
				}
			}
			while (u2.hasNext()) {
				CommitTree t2 = u2.next();
				String name = Long.toString((Long) t2.getProperty("svn:revision"));
				CommitTreeDifference diff = new CommitTreeDifference(name, null, t2);
				if (!diff.isEmpty()) {
					repositoryDifference.addCommitDifference(diff);
				}
			}
		} finally {
			u1.close();
			u2.close();
		}
		
		return repositoryDifference;
	}
}
