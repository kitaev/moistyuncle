package org.tmatesoft.translator.tests.comparator.git;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.tmatesoft.translator.tests.comparator.CommitTree;
import org.tmatesoft.translator.tests.comparator.CommitTreeDifference;
import org.tmatesoft.translator.tests.comparator.CommitTreeNode;
import org.tmatesoft.translator.tests.comparator.RepositoryComparator;


public class GitRepositoryComparator extends RepositoryComparator {

	private File myGitDir2;
	private File myGitDir1;

	public GitRepositoryComparator setGitDir1(String path1) {
		myGitDir1 = new File(path1);
		return this;
	}

	public GitRepositoryComparator setGitDir2(String path2) {
		myGitDir2 = new File(path2);
		return this;
	}
	
	public List<CommitTreeDifference> compare() throws IOException {
		
		Repository r1 = new RepositoryBuilder().setGitDir(myGitDir1).build();
		Repository r2 = new RepositoryBuilder().setGitDir(myGitDir2).build();
		
        Iterator<RevCommit> i1 = createRevWalk(r1).iterator();
        Iterator<RevCommit> i2 = createRevWalk(r2).iterator();

        List<CommitTreeDifference> diffs = new LinkedList<CommitTreeDifference>();
        while(i1.hasNext() && i2.hasNext()) {
        	RevCommit commit1 = i1.next();
        	RevCommit commit2 = i2.next();

        	CommitTree tree1 = buildTree(r1, commit1);
        	CommitTree tree2 = buildTree(r2, commit2);
        	CommitTreeDifference diff = new CommitTreeDifference(tree1, tree2);
        	if (!diff.isEmpty()) {
        		diffs.add(diff);
        	}
        }
        while(i1.hasNext()) {
        	RevCommit commit1 = i1.next();

        	CommitTree tree1 = buildTree(r1, commit1);
        	CommitTreeDifference diff = new CommitTreeDifference(tree1, null);
        	diff.compute();
        	diffs.add(diff);
        	
        }
        while(i2.hasNext()) {
        	RevCommit commit2 = i2.next();

        	CommitTree tree2 = buildTree(r2, commit2);
        	CommitTreeDifference diff = new CommitTreeDifference(null, tree2);
        	diff.compute();
        	diffs.add(diff);
        }
	    return diffs;
	}
	
	private static CommitTree buildTree(Repository repository, RevCommit commit) throws IOException {
		CommitTree tree = new CommitTree(new GitContentLoader(repository));
		CommitTreeNode root = tree.getRoot();
		Stack<CommitTreeNode> currentPath = new Stack<CommitTreeNode>();

		ObjectReader reader = repository.getObjectDatabase().newReader();
		TreeWalk walk = new TreeWalk(reader);
    	try {
    		walk.addTree(commit.getTree().getId());
    		int depth = walk.getDepth();
    		currentPath.push(root);
    		while(walk.next()) {
    			if (walk.getDepth() < depth) {
    				for(int i = 0; i < depth - walk.getDepth() + 1; i++) {
    					root = currentPath.pop();
    				}
    			}
				depth = walk.getDepth();
    			CommitTreeNode node = root.addChild(walk.getNameString());
    			node.setId(ObjectId.toString(walk.getObjectId(0)));
    			if (walk.isSubtree()) {
    				walk.enterSubtree();
    				currentPath.push(node);
    				root = node;
    			}
    		}
    	} finally {
    		walk.release();
    		repository.getObjectDatabase().close();
    	}
		return tree;
	}

	private static RevWalk createRevWalk(Repository repository) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		Map<String, Ref> refs = repository.getAllRefs();
        Collection<RevCommit> commits = new HashSet<RevCommit>();
        
        RevWalk revWalk = new RevWalk(repository);
        for (String refName : refs.keySet()) {
            Ref ref = refs.get(refName);
            ObjectId commitId = null;
            if (ref != null && ref.isSymbolic()) {
                ref = ref.getLeaf();
            }
            if (ref == null) {
                continue;
            }
            if (ref.isPeeled()) {
                commitId = ref.getPeeledObjectId();
            } else {
                commitId = ref.getObjectId();
            }
            RevCommit revCommit = null;
            if (commitId != null) {
                try {
                    revCommit = revWalk.parseCommit(commitId);
                } catch (IOException e) {
                }
            }
            if (revCommit != null) {
                commits.add(revCommit);
            }
        }
        revWalk.markStart(commits);
        revWalk.setRetainBody(false);
        
        return revWalk;
	}

}
