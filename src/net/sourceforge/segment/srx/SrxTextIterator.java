package net.sourceforge.segment.srx;


import static net.sourceforge.segment.util.Util.getParameter;

import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.segment.AbstractTextIterator;
import net.sourceforge.segment.util.IORuntimeException;

/**
 * Represents text iterator splitting text according to rules in SRX file.
 * 
 * The algorithm idea is as follows:
 * 1. Rule matcher list is created based on SRX file and language. Each rule 
 *    matcher is responsible for matching before break and after break regular 
 *    expressions of one break rule.
 * 2. Each rule matcher is matched to the text. If the rule was not found the 
 *    rule matcher is removed from the list. 
 * 3. First rule matcher in terms of its break position in text is selected.
 * 4. List of exception rules corresponding to break rule is retrieved. 
 * 5. If none of exception rules is matching in break position then 
 *    the text is marked as split and new segment is created. In addition 
 *    all rule matchers are moved so they start after the end of new segment 
 *    (which is the same as break position of the matched rule). 
 * 6. All the rules that have break position behind last matched rule 
 *    break position are moved until they pass it.
 * 7. If segment was not found the whole process is repeated.
 *
 * In streaming version of this algorithm character buffer is searched. 
 * When the end of it is reached or break position is in the margin 
 * (break position > buffer size - margin) and there is more text, 
 * the buffer is moved in the text until it starts after last found segment. 
 * If this happens rule matchers are reinitialized and the text is searched again.
 * Streaming version has a limitation that read buffer must be at least as long 
 * as any segment in the text.
 * 
 * As this algorithm uses lookbehind extensively but Java does not permit
 * infinite regular expressions in lookbehind, so some patterns are finitized. 
 * For example a* pattern will be changed to something like a{0,100}.
 *
 * @author loomchild
 */
public class SrxTextIterator extends AbstractTextIterator {
	
	/**
	 * Margin size. Used in streaming splitter.
	 * If rule is matched but its position is in the margin 
	 * (position > bufferLength - margin) then the matching is ignored, 
	 * and more text is read and rule is matched again.
	 */
	public static final String MARGIN_PARAMETER = "margin";
	
	/**
	 * 	Reader buffer size. Segments cannot be longer than this value.
	 */
	public static final String BUFFER_LENGTH_PARAMETER = "bufferLength";

	/**
	 * Maximum length of a regular expression construct that occurs in lookbehind. 
	 */
	public static final String MAX_LOOKBEHIND_CONSTRUCT_LENGTH_PARAMETER = 
		"maxLookbehindConstructLength";
	
	/**
	 * Default margin size. 
	 */
	public static final int DEFAULT_MARGIN = 128;
	
	/**
	 * Default size of read buffer when using streaming version of this class.
	 * Any segment cannot be longer than buffer size.
	 */
	public static final int DEFAULT_BUFFER_LENGTH = 64 * 1024;

	/** 
	 * Default max lookbehind construct length parameter.
	 */
	public static final int DEFAULT_MAX_LOOKBEHIND_CONSTRUCT_LENGTH = 100;
	
	private SrxDocument document;

	private String segment;

	private int start, end;
	
	private TextManager textManager;
	
	private RuleManager ruleManager;
	
	private List<RuleMatcher> ruleMatcherList;
	
	private int margin;
	
	
	/**
	 * Creates text iterator that obtains language rules form given document
	 * using given language code. This constructor version is not streaming 
	 * because it receives whole text as a string. 
	 * Supported parameters: {@link #MAX_LOOKBEHIND_CONSTRUCT_LENGTH_PARAMETER}.
	 * 
	 * @param document SRX document
	 * @param languageCode text language code of text used to retrieve the rules
	 * @param text
	 * @param parameterMap additional segmentation parameters
	 */
	public SrxTextIterator(SrxDocument document, String languageCode, 
			CharSequence text, Map<String, Object> parameterMap) {
		parameterMap.put(MARGIN_PARAMETER, 0);
		init(document, languageCode, new TextManager(text), parameterMap);
	}

	/**
	 * Creates text iterator with no additional parameters.
	 * @see #SrxTextIterator(SrxDocument, String, CharSequence, Map)
	 * @param document SRX document
	 * @param languageCode text language code of text used to retrieve the rules
	 * @param text
	 */
	public SrxTextIterator(SrxDocument document, String languageCode, 
			CharSequence text) {
		this(document, languageCode, text, new HashMap<String, Object>());
	}

	/**
	 * Creates text iterator that obtains language rules from given document 
	 * using given language code. This is streaming constructor - it reads
	 * text from reader using buffer with given size and margin. Single
	 * segment cannot be longer than buffer size.
	 * If rule is matched but its position is in the margin 
	 * (position > bufferLength - margin) then the matching is ignored, 
	 * and more text is read and rule is matched again.
	 * This is needed because incomplete rule can be located at the end of the 
	 * buffer and never matched. 
	 * Supported parameters: {@link #BUFFER_LENGTH_PARAMETER}, 
	 * {@link #MARGIN_PARAMETER}, 
	 * {@link #MAX_LOOKBEHIND_CONSTRUCT_LENGTH_PARAMETER}.
	 * 
	 * @param document SRX document
	 * @param languageCode text language code of text used to retrieve the rules
	 * @param reader reader from which read the text
	 * @param parameterMap additional segmentation parameters
	 */
	public SrxTextIterator(SrxDocument document, String languageCode, 
			Reader reader, Map<String, Object> parameterMap) {
		int bufferLength = getParameter(parameterMap.get(BUFFER_LENGTH_PARAMETER), 
				DEFAULT_BUFFER_LENGTH);
		init(document, languageCode, new TextManager(reader, bufferLength), parameterMap);
	}

	/**
	 * Creates streaming text iterator with no additional parameters.
	 * @see SrxTextIterator#SrxTextIterator(SrxDocument, String, Reader, Map)
	 * @param document SRX document
	 * @param languageCode text language code of text used to retrieve the rules
	 * @param reader reader from which read the text
	 */
	public SrxTextIterator(SrxDocument document, String languageCode, 
			Reader reader) {
		this(document, languageCode, reader, new HashMap<String, Object>());
	}

	/**
	 * Finds the next segment in the text and returns it.
	 * 
	 * @return next segment or null if it doesn't exist
	 * @throws IllegalStateException if buffer is too small to hold the segment
	 * @throws IORuntimeException if IO error occurs when reading the text
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
							textManager.getBufferLength() - margin)) {
						
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
						found = isException(minMatcher);
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
	 * @return true if there are more segments
	 */
	public boolean hasNext() {
		return (textManager.hasMoreText() || 
				start < textManager.getText().length());
	}
	
	/**
	 * Initializes splitter.
	 *  
	 * @param document SRX document
	 * @param languageCode text language code
	 * @param textManager text manager containing the text
	 * @param parameterMap additional segmentation parameters
	 */
	private void init(SrxDocument document, String languageCode, 
			TextManager textManager, Map<String, Object> parameterMap) {
		
		int margin = getParameter(parameterMap.get(MARGIN_PARAMETER), 
				DEFAULT_MARGIN);
		int maxLookbehindConstructLength = getParameter(parameterMap.get(
				MAX_LOOKBEHIND_CONSTRUCT_LENGTH_PARAMETER), 
					DEFAULT_MAX_LOOKBEHIND_CONSTRUCT_LENGTH);

		if (textManager.getBufferLength() > 0 &&
				textManager.getBufferLength() <= margin) {
			throw new IllegalArgumentException("Margin: " + margin +
					" must be smaller than buffer itself: " + 
					textManager.getBufferLength() + ".");
		}
		
		this.document = document;
		this.segment = null;
		this.start = 0;
		this.end = 0;
		this.textManager = textManager;
		this.margin = margin;

		List<LanguageRule> languageRuleList = 
			document.getLanguageRuleList(languageCode);
		Object[] key = new Object[]{languageRuleList, maxLookbehindConstructLength};
		this.ruleManager = document.getCache().get(key, RuleManager.class);
		
		if (ruleManager == null) {
			this.ruleManager = new RuleManager(document, languageRuleList, 
					maxLookbehindConstructLength);
			document.getCache().put(key, ruleManager);
		}

	}
		
	/**
	 * Initializes matcher list according to rules from ruleManager and 
	 * text from textManager.
	 */
	private void initMatchers() {
		this.ruleMatcherList = new LinkedList<RuleMatcher>();
		for (Rule rule : ruleManager.getBreakRuleList()) {
			RuleMatcher matcher = 
				new RuleMatcher(document, rule, textManager.getText());
			matcher.find();
			if (!matcher.hitEnd()) {
				ruleMatcherList.add(matcher);
			}
		}
	}

	/**
	 * Moves all matchers to the next position if their break position 
	 * is smaller than last segment end position.
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
	 * @return first matcher in the text or null if there are no matchers
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
	 * Returns true if there are no exception rules preventing given
	 * rule matcher from breaking the text.
	 * @param ruleMatcher rule matcher
	 * @return true if rule matcher breaks the text
	 */
	private boolean isException(RuleMatcher ruleMatcher) {
		
		Pattern pattern = 
			ruleManager.getExceptionPattern(ruleMatcher.getRule());
		
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
