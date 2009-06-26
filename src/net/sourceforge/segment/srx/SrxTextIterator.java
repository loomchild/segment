package net.sourceforge.segment.srx;


import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.segment.AbstractTextIterator;
import net.sourceforge.segment.srx.legacy.RuleMatcher;

/**
 * Represents a splitter splitting text according to rules in SRX file.
 *
 * @author loomchild
 */
public class SrxTextIterator extends AbstractTextIterator {
	
	public static final int DEFAULT_BUFFER_SIZE = 65536;
	
	public static final int DEFAULT_MARGIN = 128;
	

	private SrxDocument document;

	private String segment;

	private int start, end;
	
	private TextManager textManager;
	
	private RuleManager ruleManager;
	
	private List<RuleMatcher> ruleMatcherList;
	
	private int margin;
	
	
	private SrxTextIterator(SrxDocument document, String languageCode, 
			TextManager textManager, int margin) {

		if (textManager.getBufferSize() > 0 &&
				textManager.getBufferSize() <= margin) {
			throw new IllegalArgumentException("Margin: " + margin +
					" must be smaller than buffer itself: " + 
					textManager.getBufferSize() + ".");
		}
		
		this.document = document;
		this.segment = null;
		this.start = 0;
		this.end = 0;
		this.textManager = textManager;
		this.ruleManager = new RuleManager(document, languageCode);
		this.margin = margin;
	}
		
	/**
	 * Creates text iterator that obtains language rules form given document
	 * using given language code. To retrieve language rules calls
	 * {@link SrxDocument#getLanguageRuleList(String)}.
	 * 
	 * @param document
	 *            Document containing language rules.
	 * @param languageCode
	 *            Language code to select the rules.
	 * @param text
	 *            Text.
	 */
	public SrxTextIterator(SrxDocument document, String languageCode, 
			CharSequence text) {
		this(document, languageCode, new TextManager(text), 0);
	}

	public SrxTextIterator(SrxDocument document, String languageCode, 
			Reader reader, int bufferSize, int margin) {
		this(document, languageCode, new TextManager(reader, bufferSize), margin);
	}

	public SrxTextIterator(SrxDocument document, String languageCode, 
			Reader reader, int bufferSize) {
		this(document, languageCode, reader, bufferSize, DEFAULT_MARGIN);
	}
		
	public SrxTextIterator(SrxDocument document, String languageCode, 
			Reader reader) {
		this(document, languageCode, reader, DEFAULT_BUFFER_SIZE);
	}
		
	/**
	 * Finds the next segment.
	 * @return Returns next segment or null if it doesn't exist.
	 */
	public String next() {
		if (hasNext()) {

			// Initialize matchers before first search.
			if (segment == null) {
				initMatchers();
			}
			
			boolean found = false;
			
			while (!found) {
				
				RuleMatcher minMatcher = getMinMatcher();
				
				if (minMatcher == null && !textManager.hasMoreText()) {

					found = true;
					end = textManager.getText().length();
				
				} else {
					
					if (textManager.hasMoreText() && 
							(minMatcher == null || 
							minMatcher.getBreakPosition() > 
							textManager.getBufferSize() - margin)) {
						
						if (start == 0) {
							throw new IllegalStateException("Buffer too short");
						}
						
						textManager.readText(start);
						start = 0;
						initMatchers();
						minMatcher = getMinMatcher();						

					}
					
					end = minMatcher.getBreakPosition();

					if (end > start) {
						found = isBreaking(minMatcher);
						if (found) {
							cutMatchers();
						}
					}
					
				}
				
				moveMatchers();
			}
			
			segment = textManager.getText().subSequence(start, end).toString();
			start = end;
			return segment;
			
		} else {
			
			return null;
			
		}
	}

	/**
	 * @return Returns true if there are more segments.
	 */
	public boolean hasNext() {
		return (textManager.hasMoreText() || 
				start < textManager.getText().length());
	}
	
	/**
	 * Initializes matcher list according to rules from ruleManager and 
	 * text from textManager.
	 */
	private void initMatchers() {
		this.ruleMatcherList = new LinkedList<RuleMatcher>();
		for (Rule rule : ruleManager.getBreakingRuleList()) {
			RuleMatcher matcher = 
				new RuleMatcher(document, rule, textManager.getText());
			matcher.find();
			if (!matcher.hitEnd()) {
				ruleMatcherList.add(matcher);
			}
		}
	}

	/**
	 * Moves all matchers to the next position if needed.
	 */
	private void moveMatchers() {
		for (Iterator<RuleMatcher> i = ruleMatcherList.iterator(); i.hasNext();) {
			RuleMatcher matcher = i.next();
			while (matcher.getBreakPosition() <= end) {
				matcher.find();
				if (matcher.hitEnd()) {
					i.remove();
					break;
				}
			}
		}
	}

	/**
	 * Move matchers that start before previous segment end.
	 */
	private void cutMatchers() {
		for (Iterator<RuleMatcher> i = ruleMatcherList.iterator(); i.hasNext();) {
			RuleMatcher matcher = i.next();
			if (matcher.getStartPosition() < end) {
				matcher.find(end);
				if (matcher.hitEnd()) {
					i.remove();
				}
			}
		}
	}

	/**
	 * @return Returns first matcher in the text or null if there are no matchers.
	 */
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
	
	/**
	 * Returns true if there are no non breaking rules preventing given
	 * rule matcher from breaking the text.
	 * @param ruleMatcher Rule matcher.
	 * @return Returns true if rule matcher breaks the text.
	 */
	private boolean isBreaking(RuleMatcher ruleMatcher) {
		
		Pattern pattern = 
			ruleManager.getNonBreakingPattern(ruleMatcher.getRule());
		
		if (pattern != null) {
			Matcher matcher = pattern.matcher(textManager.getText());
			matcher.useTransparentBounds(true);
			matcher.region(ruleMatcher.getBreakPosition(), 
					textManager.getText().length());
			return !matcher.lookingAt();
		} else {
			return true;
		}

	}
	
}
