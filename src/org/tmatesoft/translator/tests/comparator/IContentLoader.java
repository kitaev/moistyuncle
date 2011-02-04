package org.tmatesoft.translator.tests.comparator;

public interface IContentLoader {
	
	public boolean hasContent(String id);
	
	public byte[] loadContent(String id);

}
