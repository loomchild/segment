package net.sourceforge.segment.srx.legacy;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents {@link MatchResult} that uses {@link ReaderCharSequence} as
 * a text - it takes care of exceptions that are thrown by it. 
 * @author loomchild
 */
public class ReaderMatcher implements MatchResult {
	
	private Matcher matcher;
	
	private CharSequence text;
	
	private int oldLength;
	
	public ReaderMatcher(Pattern pattern, CharSequence text) {
		this.text = text;
		this.oldLength = text.length();
		this.matcher = pattern.matcher(text);
	}

	public ReaderMatcher appendReplacement(StringBuffer sb, String replacement) {
		matcher.appendReplacement(sb, replacement);
		return this;
	}
	
	public StringBuffer appendTail(StringBuffer sb) {
		return matcher.appendTail(sb);
	}

	public int end() {
		return matcher.end();
	}

	public int end(int group) {
		return matcher.end(group);
	}
	
	public boolean find() {
		boolean result = false;
		int end = getEnd();
		try {
			result = matcher.find();
		} catch (IndexOutOfBoundsException e) {
		}
		if (lengthChanged()) {
			int regionStart = Math.max(end, matcher.regionStart());
			int regionEnd = Math.min(text.length(), matcher.regionEnd());
			matcher.reset(text);
			matcher.region(regionStart, regionEnd);
			result = matcher.find();
		}
		return result;
	}

	public boolean find(int start) {
		boolean result = false;
		try {
			result = matcher.find(start);
		} catch (IndexOutOfBoundsException e) {
		}
		if (lengthChanged()) {
			int regionStart = matcher.regionStart();
			int regionEnd = Math.min(text.length(), matcher.regionEnd());
			matcher.reset(text);
			matcher.region(regionStart, regionEnd);
			result = matcher.find(start);
		}
		return result;
	}

	public String group() {
		return matcher.group();
	}

	public String group(int group) {
		return matcher.group(group);
	}

	public int groupCount() {
		return matcher.groupCount();
	}

	public boolean hasAnchoringBounds() {
		return matcher.hasAnchoringBounds();
	}

	public boolean hasTransparentBounds() {
		return matcher.hasTransparentBounds();
	}
	
	public boolean hitEnd() {
		return matcher.hitEnd();
	}
	
	public boolean lookingAt() {
		boolean result = false;
		try {
			result = matcher.lookingAt();
		} catch (IndexOutOfBoundsException e) {
		}
		if (lengthChanged()) {
			int regionStart = matcher.regionStart();
			int regionEnd = Math.min(text.length(), matcher.regionEnd());
			matcher.reset(text);
			matcher.region(regionStart, regionEnd);
			result = matcher.lookingAt();
		}
		return result;
	}

	public boolean matches() {
		boolean result = false;
		try {
			result = matcher.matches();
		} catch (IndexOutOfBoundsException e) {
		}
		if (lengthChanged()) {
			int regionStart = matcher.regionStart();
			int regionEnd = Math.min(text.length(), matcher.regionEnd());
			matcher.reset(text);
			matcher.region(regionStart, regionEnd);
			result = matcher.matches();
		}
		return result;
	}

	public Pattern pattern() {
		return matcher.pattern();
	}
	
	public ReaderMatcher region(int start, int end) {
		matcher.region(start, end);
		return this;
	}
	
	public int regionEnd() {
		return matcher.regionEnd();
	}

	public int regionStart() {
		return matcher.regionStart();
	}
	
	public String replaceAll(String replacement) {
		String result = null;
		try {
			result = matcher.replaceAll(replacement);
		} catch (IndexOutOfBoundsException e) {
		}
		if (lengthChanged()) {
			// No need to set region because replaceAll resets matcher first.
			matcher.reset(text);
			result = matcher.replaceAll(replacement);
		}
		return result;
	}

	public String replaceFirst(String replacement) {
		String result = null;
		try {
			result = matcher.replaceFirst(replacement);
		} catch (IndexOutOfBoundsException e) {
		}
		if (lengthChanged()) {
			matcher.reset(text);
			// No need to set region because replaceFirst resets matcher first.
			result = matcher.replaceFirst(replacement);
		}
		return result;
	}

	public boolean requireEnd() {
		return matcher.requireEnd();
	}
	
	public ReaderMatcher reset() {
		matcher.reset();
		return this;
	}

	public ReaderMatcher reset(CharSequence input) {
		matcher.reset(input);
		return this;
	}
	
	public int start() {
		return matcher.start();
	}

	public int start(int group) {
		return matcher.start(group);
	}

	public MatchResult toMatchResult() {
		return matcher.toMatchResult();
	}

	public String toString() {
		return "ReaderMatcher: " + matcher.toString();
	}

	public ReaderMatcher useAnchoringBounds(boolean b) {
		matcher.useAnchoringBounds(b);
		return this;
	}

	public ReaderMatcher usePattern(Pattern newPattern) {
		matcher.usePattern(newPattern);
		return this;
	}

	public ReaderMatcher useTransparentBounds(boolean b) {
		matcher.useTransparentBounds(b);
		return this;
	}
	
	private int getEnd() {
		try {
			return matcher.end();
		} catch (IllegalStateException e) {
			return 0;
		}
	}

	private boolean lengthChanged() {
		if (text.length() < oldLength) {
			oldLength = text.length();
			return true;
		} else {
			return false;
		}
	}
	
}
