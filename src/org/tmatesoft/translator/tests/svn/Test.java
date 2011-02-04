package org.tmatesoft.translator.tests.svn;

import java.io.IOException;
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
import org.tmatesoft.translator.tests.git.GitRepositoryComparator;

public class Test {

	/**
	 * @param args
	 * @throws SVNException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws SVNException, IOException {
		
		GitRepositoryComparator gitComparator = new GitRepositoryComparator().setGitDir1("C:/users/alex/workspace/moistyuncle2/.git").setGitDir2("C:/users/alex/workspace/moistyuncle/.git");
		List<CommitTreeDifference> diffs = gitComparator.compare();
		for (CommitTreeDifference diff : diffs) {
			System.out.println(diff);
		}
		System.exit(0);
		
		SVNRepositoryFactoryImpl.setup();
		DAVRepositoryFactory.setup();
		FSRepositoryFactory.setup();
		
		SVNURL url = SVNURL.parseURIEncoded("http://svn.svnkit.com/repos/svnkit/");
		SVNRepository repository1 = SVNRepositoryFactory.create(url);
		SVNRepository repository2 = SVNRepositoryFactory.create(url);
		SvnTreeUpdater updater1 = new SvnTreeUpdater(repository1);
		SvnTreeUpdater updater2 = new SvnTreeUpdater(repository2);

		CommitTree t1 = updater1.next();
		
		CommitTree t2 = updater2.next();
		t2 = updater2.next();
		t2 = updater2.next();
		t2 = updater2.next();
		t2 = updater2.next();
		t2 = updater2.next();
		
		CommitTreeDifference diff = new CommitTreeDifference(t2, t1);
		diff.compute();
		System.out.println(diff);
	}
}