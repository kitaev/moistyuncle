package org.tmatesoft.translator.tests.comparator.git;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.tmatesoft.translator.tests.comparator.CommitTree;
import org.tmatesoft.translator.tests.comparator.CommitTreeDifference;
import org.tmatesoft.translator.tests.comparator.CommitTreeNode;
import org.tmatesoft.translator.tests.comparator.IContentLoader;
import org.tmatesoft.translator.tests.comparator.PropertiesDifference;
import org.tmatesoft.translator.tests.comparator.RepositoryComparator;
import org.tmatesoft.translator.tests.comparator.RepositoryDifference;


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
	
	public RepositoryDifference compare() throws IOException {
		
		Repository r1 = new RepositoryBuilder().setGitDir(myGitDir1).build();
		Repository r2 = new RepositoryBuilder().setGitDir(myGitDir2).build();
		
        Iterator<RevCommit> i1 = createRevWalk(r1).iterator();
        Iterator<RevCommit> i2 = createRevWalk(r2).iterator();

        RepositoryDifference repositoryDifference = new RepositoryDifference(
        		myGitDir1.getAbsolutePath().replace(File.separatorChar, '/'),
        		myGitDir2.getAbsolutePath().replace(File.separatorChar, '/'));
        
        PropertiesDifference referencesDifference = new PropertiesDifference(getReferences(r1), getReferences(r2));
        if (!referencesDifference.isEmpty()) {
        	repositoryDifference.setReferencesDifference(referencesDifference);
        }
        
        long index = 0;
        while(i1.hasNext() && i2.hasNext()) {
        	RevCommit commit1 = i1.next();
        	RevCommit commit2 = i2.next();

        	CommitTree tree1 = buildTree(r1, commit1);
        	CommitTree tree2 = buildTree(r2, commit2);
        	CommitTreeDifference diff = new CommitTreeDifference(Long.toString(index++), tree1, tree2);
        	if (!diff.isEmpty()) {
        		repositoryDifference.addCommitDifference(diff);
        	}
        }

        while(i1.hasNext()) {
        	RevCommit commit1 = i1.next();
        	
        	CommitTree tree1 = buildTree(r1, commit1);
        	CommitTreeDifference diff = new CommitTreeDifference(Long.toString(index++), tree1, null);
        	if (!diff.isEmpty()) {
        		repositoryDifference.addCommitDifference(diff);
        	}
        	
        }
        while(i2.hasNext()) {
        	RevCommit commit2 = i2.next();

        	CommitTree tree2 = buildTree(r2, commit2);
        	CommitTreeDifference diff = new CommitTreeDifference(Long.toString(index++), null, tree2);
        	if (!diff.isEmpty()) {
        		repositoryDifference.addCommitDifference(diff);
        	}
        }
	    return repositoryDifference;
	}
	
	private static CommitTree buildTree(final Repository repository, RevCommit commit) throws IOException {
		CommitTree tree = new CommitTree();
		CommitTreeNode root = tree.getRoot();
		Stack<CommitTreeNode> currentPath = new Stack<CommitTreeNode>();

		int parentCount = commit.getParentCount();
		tree.setMetaProperty("git:parentCount", Integer.toString(parentCount).getBytes("UTF-8"));
		tree.setMetaProperty("git:author", commit.getAuthorIdent() != null ? PropertiesDifference.fromString(toString(commit.getAuthorIdent())) : null);
		tree.setMetaProperty("git:comitter", commit.getCommitterIdent() != null ? PropertiesDifference.fromString(toString(commit.getCommitterIdent())) : null);
		tree.setMetaProperty("git:log", PropertiesDifference.fromString(commit.getFullMessage()));
		
		ObjectReader reader = repository.getObjectDatabase().newReader();
		TreeWalk walk = new TreeWalk(reader);
    	try {
    		walk.addTree(commit.getTree().getId());
    		int depth = walk.getDepth();
    		currentPath.push(root);
    		while(walk.next()) {
    			if (walk.getDepth() < depth) {
    				for(int i = 0; i < depth - walk.getDepth(); i++) {
    					root = currentPath.pop();
    				}
    			}
    			root = currentPath.peek();
				depth = walk.getDepth();
    			CommitTreeNode node = root.addChild(walk.getNameString());
    			final ObjectId id = walk.getObjectId(0);
    			node.setId(ObjectId.toString(id));
    			if (walk.isSubtree()) {
    				walk.enterSubtree();
    				currentPath.push(node);
    				root = node;
    			} else {
    				IContentLoader contentLoader = new IContentLoader() {
						public byte[] loadContent() {
							ObjectLoader loader = null;
							try {
								loader = repository.getObjectDatabase().open(id);
								if (loader != null && loader.getType() == Constants.OBJ_BLOB) {
									return loader.getBytes();	
								}								
							} catch (IOException e) {
							} finally {
								repository.getObjectDatabase().close();
							}
							return PropertiesDifference.fromString("Cannot load file " + ObjectId.toString(id));
						}
    					
    				};
    				node.setContent(contentLoader, node.getId());
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
	
	private static Map<String, byte[]> getReferences(Repository repository) {
		Map<String, byte[]> result = new HashMap<String, byte[]>();
		Map<String, Ref> refs = repository.getAllRefs();
        for (String refName : refs.keySet()) {
        	Ref ref = refs.get(refName);
        	if (ref.isSymbolic()) {
        		result.put(refName, PropertiesDifference.fromString("symbolic"));
        	} else if (ref.isPeeled()) {
        		result.put(refName, PropertiesDifference.fromString("peeled"));
        	} else {
        		result.put(refName, null);
        	}
        }
		return result;
	}

	private static String toString(PersonIdent ident) {
		if (ident == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		
		sb.append(ident.getName());
		if (ident.getEmailAddress() != null) {
			sb.append(", ");
			sb.append(ident.getEmailAddress());
		}
		if (ident.getTimeZone() != null) {
			sb.append(", ");
			sb.append(ident.getTimeZone());
		}
		return sb.toString();
	}
	
	private static void buildRepositoryGraph(RevCommit left, RevCommit right)
}
