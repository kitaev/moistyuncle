package org.tmatesoft.translator.tests.svn;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

public class SvnRepositoryComparator {
	
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
	public boolean compare(StringBuffer result) throws SVNException {
		SVNRepository r1 = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(myURL1));
		SVNRepository r2 = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(myURL2));
		SvnTreeUpdater u1 = new SvnTreeUpdater(r1);
		SvnTreeUpdater u2 = new SvnTreeUpdater(r2);
		try {
			while(u1.hasNext() && u2.hasNext()) {
				SvnTree t1 = u1.next();
				SvnTree t2 = u2.next();
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream out = new PrintStream(baos);
				StringBuffer report = new StringBuffer();
				try {
					t1.compareTo(t2, out);
				} finally {
					out.flush();
					out.close();
					
					try {
						report.append(baos.toString("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						report.append(e.getMessage());
					}
					if (report.length() > 0) {
						result.append(report);
						return false;
					}
 				}
			}

			if (u1.hasNext()) {
				result.append("More revisions expected than present, expected: " + r1.getLatestRevision() +", present: " + r2.getLatestRevision() + "\n");
				return false;
			}
			
			if (u2.hasNext()) {
				result.append("Less revisions expected than present, expected: " + r1.getLatestRevision() +", present: " + r2.getLatestRevision() + "\n");
				return false;
			}
		} finally {
			u1.close();
			u2.close();
		}
		
		return true;
	}
}
