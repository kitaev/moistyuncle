package org.tmatesoft.translator.tests.svn;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;

public class Test {

	/**
	 * @param args
	 * @throws SVNException 
	 */
	public static void main(String[] args) throws SVNException {
		SVNRepositoryFactoryImpl.setup();
		DAVRepositoryFactory.setup();
		FSRepositoryFactory.setup();

		SvnRepositoryComparator c = new SvnRepositoryComparator();
		StringBuffer sb = new StringBuffer();
		c = c.setURL1("svn://localhost/repos2").setURL2("svn://localhost/repos2");
		if (!c.compare(sb)) {
			System.out.println(sb);
		} else {
			System.out.println("all ok");
		}
	}
}