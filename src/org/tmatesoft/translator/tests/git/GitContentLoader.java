package org.tmatesoft.translator.tests.git;

import java.io.IOException;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.tmatesoft.translator.tests.comparator.IContentLoader;

public class GitContentLoader implements IContentLoader {
	
	private Repository myRepository;

	public GitContentLoader(Repository repository) {
		myRepository = repository;
	}

	public boolean hasContent(String id) {
		if (id == null) {
			return false;
		}
		ObjectId objectId = ObjectId.fromString(id);
		ObjectLoader loader = null;
		try {
			loader = myRepository.getObjectDatabase().open(objectId);
			if (loader != null) {
				return loader.getType() == Constants.OBJ_BLOB;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return false;
	}

	public byte[] loadContent(String id) {
		if (id == null) {
			return null;
		}
		ObjectId objectId = ObjectId.fromString(id);
		ObjectLoader loader = null;
		try {
			loader = myRepository.getObjectDatabase().open(objectId);
			if (loader != null) {
				return loader.getBytes();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return null;
	}

}
