package org.tmatesoft.translator.tests.comparator;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PropertiesDifference {

	private Map<String, byte[]> myLeftProperties;
	private Map<String, byte[]> myRightProperties;
	
	private Set<String> myUniqueRightProperties;
	private Set<String> myUniqueLeftProperties;
	private Set<String> myDifferentProperties;

	public PropertiesDifference(Map<String, byte[]> left, Map<String, byte[]> right) {
		myLeftProperties = left;
		myRightProperties = right;
	}
	
	public Map<String, byte[]> getLeftProperties() {
		return myLeftProperties;
	}
	
	public Map<String, byte[]> getRightProperties() {
		return myRightProperties;
	}
	
	/**
	 * @return Set of property names that are on the left, but are missing on the right.
	 */
	public Set<String> getUniqueLeftProperties() {
		compute();
		return myUniqueLeftProperties;
	}

	/**
	 * @return Set of property names that are on the right, but are missing on the left.
	 */
	public Set<String> getUniqueRightProperties() {
		compute();
		return myUniqueRightProperties;
	}
	
	/**
	 * @return Set of property names which has different values on right and left.
	 */
	public Set<String> getDifferentProperties() {
		compute();
		return myDifferentProperties;
	}

	public boolean isEmpty() {
		return getDifferentProperties().isEmpty() && getUniqueLeftProperties().isEmpty() && getUniqueRightProperties().isEmpty();
	}
	
	private void compute() {
		if (myDifferentProperties != null) {
			return;
		}
		
		myDifferentProperties = new TreeSet<String>();
		myUniqueLeftProperties = new TreeSet<String>();
		myUniqueRightProperties = new TreeSet<String>();
		
		if (getLeftProperties() == null && getRightProperties() == null) {
			return;
		} else if (getLeftProperties() == null) {
			myUniqueRightProperties.addAll(getRightProperties().keySet());
		} else if (getRightProperties() == null) {
			myUniqueLeftProperties.addAll(getLeftProperties().keySet());
		} else {
			for (String name : getLeftProperties().keySet()) {
				byte[] leftValue = getLeftProperties().get(name);
				byte[] rightValue = getRightProperties().get(name);
				if (!getRightProperties().containsKey(name)) {
					myUniqueLeftProperties.add(name);
				} else if (!ContentDifference.compare(leftValue, rightValue)) {
					myDifferentProperties.add(name);
				}
			}
			for (String name : getRightProperties().keySet()) {
				if (!getLeftProperties().containsKey(name)) {
					myUniqueRightProperties.add(name);
				} 
			}
		}
	}

	@Override
	public String toString() {
		if (myDifferentProperties == null) {
			return "not yet computed";
		}
		if (isEmpty()) {
			return "properties are identical";
		}
		StringBuffer result = new StringBuffer();
		for (String name : getUniqueRightProperties()) {
			result.append("+ ");
			result.append(name);
			result.append('\n');
		}
		for (String name : getUniqueLeftProperties()) {
			result.append("- ");
			result.append(name);
			result.append('\n');
		}
		for (String name : getDifferentProperties()) {
			result.append("* ");
			result.append(name);
			result.append("\nleft: [");
			result.append(toString(getLeftProperties().get(name)));
			result.append("]\nright: [");
			result.append(toString(getRightProperties().get(name)));
			result.append("]\n");
		}
		return result.toString();
	}
	
	public static String toString(byte[] value) {
		if (value == null) {
			return "null";
		}
		try {
			return new String(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return new String(value);
		}
	}

	public static byte[] fromString(String value) {
		if (value == null) {
			return null;
		}
		try {
			return value.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			return value.getBytes();
		}
	}
}
