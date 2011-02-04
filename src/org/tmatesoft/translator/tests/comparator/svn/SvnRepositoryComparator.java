package org.tmatesoft.translator.tests.comparator.svn;

import java.util.LinkedList;
import java.util.List;

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
	public List<CommitTreeDifference> compare() throws SVNException {
		SVNRepository r1 = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(myURL1));
		SVNRepository r2 = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(myURL2));
		
		SvnTreeUpdater u1 = new SvnTreeUpdater(r1);
		SvnTreeUpdater u2 = new SvnTreeUpdater(r2);
		
		List<CommitTreeDifference> diffs = new LinkedList<CommitTreeDifference>();
		try {
			while(u1.hasNext() && u2.hasNext()) {
				CommitTree t1 = u1.next();
				CommitTree t2 = u2.next();
				CommitTreeDifference diff = new CommitTreeDifference(t1, t2);
				if (!diff.isEmpty()) {
					diffs.add(diff);
				}
			}
			while (u1.hasNext()) {
				CommitTreeDifference diff = new CommitTreeDifference(u1.next(), null);
				diff.compute();
				diffs.add(diff);
			}
			while (u2.hasNext()) {
				CommitTreeDifference diff = new CommitTreeDifference(null, u2.next());
				diff.compute();
				diffs.add(diff);
			}
		} finally {
			u1.close();
			u2.close();
		}
		
		return diffs;
	}
}
