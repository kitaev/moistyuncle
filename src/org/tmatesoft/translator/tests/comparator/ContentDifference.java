package org.tmatesoft.translator.tests.comparator;

public class ContentDifference {

	private IContentLoader myLeftContentLoader;
	private IContentLoader myRightContentLoader;
	private boolean myIsEmpty;

	public ContentDifference(IContentLoader leftLoader, IContentLoader rightLoader) {
		myLeftContentLoader = leftLoader;
		myRightContentLoader = rightLoader;
		myIsEmpty = false;
	}
	
	public byte[] getLeftContent() {
		return myLeftContentLoader != null ? myLeftContentLoader.loadContent() : null;
	}
	
	public byte[] getRightContent() {
		return myRightContentLoader != null ? myRightContentLoader.loadContent() : null;
	}

	public boolean isEmpty() {
		return myIsEmpty;
	}

	public static boolean compare(byte[] bs, byte[] bs2) {
		if (bs == bs2) {
			return true;
		}
		if (bs == null || bs2 == null) {
			return false;
		}
		if (bs.length != bs2.length) {
			return false;
		}
		for (int i = 0; i < bs2.length; i++) {
			if (bs[i] != bs2[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "content identical";
		} else {
			return "content differs";
		}
	}

}
