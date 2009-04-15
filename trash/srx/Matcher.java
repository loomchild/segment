package srx;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import loomchild.util.Utils;

public class Matcher {

	public Matcher(LanguageRule languageRule, CharSequence text) {
		initialize(languageRule, text);
	}

	public Matcher(LanguageRule languageRule, Reader reader) throws IOException {
		CharSequence text = Utils.readAll(reader);
		initialize(languageRule, text);
	}

	public boolean find() {
		if (!hitEnd()) {
			boolean found = false;
			while ((ruleMatcherList.size() > 0) && !found) {
				RuleMatcher minMatcher = getMinMatcher();
				found = minMatcher.getRule().isBreaking();
				endPosition = minMatcher.getBreakPosition();
				moveMatchers();
			}
			if (!found) {
				endPosition = text.length();
			}
			segment = text.subSequence(startPosition, endPosition).toString();
			startPosition = endPosition;
			return true;
		} else {
			segment = null;
			return false;
		}
	}

	public boolean hitEnd() {
		return startPosition >= text.length();
	}

	public String getSegment() {
		return segment;
	}
	
	public LanguageRule getLanguageRule() {
		return languageRule;
	}
	
	private void initialize(LanguageRule languageRule, CharSequence text) {
		this.languageRule = languageRule;
		this.text = text;
		this.ruleMatcherList = new LinkedList<RuleMatcher>();
		reset();
	}
	
	private void reset() {
		ruleMatcherList.clear();
		for (Rule rule : this.languageRule.getRuleList()) {
			RuleMatcher matcher = new RuleMatcher(rule, text);
			matcher.find();
			if (!matcher.hitEnd()) {
				ruleMatcherList.add(matcher);
			}
		}
		segment = null;
		startPosition = 0;
		endPosition = text.length();
	}
	
	private void moveMatchers() {
		for (Iterator<RuleMatcher> i = ruleMatcherList.iterator(); i.hasNext();) {
			RuleMatcher matcher = i.next();
			if (matcher.getBreakPosition() <= endPosition) {
				matcher.find(endPosition + 1);
				if (matcher.hitEnd()) {
					i.remove();
				}
			}
		}
	}
	
	private RuleMatcher getMinMatcher() {
		int minPosition = Integer.MAX_VALUE;
		RuleMatcher minMatcher = null;
		for (RuleMatcher matcher : ruleMatcherList) {
			if (matcher.getBreakPosition() < minPosition) {
				minPosition = matcher.getBreakPosition();
				minMatcher = matcher;
			}
		}
		return minMatcher;
	}
	
	private LanguageRule languageRule;
	
	private CharSequence text;
	
	private String segment;

	private List<RuleMatcher> ruleMatcherList;
	
	private int startPosition, endPosition;

}
