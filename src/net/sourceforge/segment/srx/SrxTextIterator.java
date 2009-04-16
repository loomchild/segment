package net.sourceforge.segment.srx;

import java.io.Reader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rootnode.loomchild.util.exceptions.EndOfStreamException;
import net.rootnode.loomchild.util.io.ReaderCharSequence;
import net.sourceforge.segment.AbstractTextIterator;

/**
 * Represents text iterator that splits text according to SRX rules.
 * 
 * @author loomchild
 */
public class SrxTextIterator extends AbstractTextIterator {

	private CharSequence text;

	private String segment;
	
	private MergedPattern mergedPattern;

	private Matcher breakingMatcher;

	private int startPosition, endPosition;

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
		
		this.text = text;
		this.segment = null;
		this.startPosition = 0;
		this.endPosition = 0;

		//If text is empty iterator finishes immediately.
		if (!canReadNextChar()) {
			this.startPosition = text.length();
		}

		List<LanguageRule> languageRuleList = 
			document.getLanguageRuleList(languageCode);
		
		this.mergedPattern = 
			(MergedPattern)document.getCache().get(languageRuleList);
		if (mergedPattern == null) {
			mergedPattern = new MergedPattern(languageRuleList);
			document.getCache().put(languageRuleList, mergedPattern);
		}
		
		if (mergedPattern.getBreakingPattern() != null) {
			this.breakingMatcher = 
				mergedPattern.getBreakingPattern().matcher(text);
		}
		
	}

	/**
	 * Creates streaming text iterator that obtains language rules form given
	 * document using given language code. To retrieve language rules calls
	 * {@link SrxDocument#getLanguageRuleList(String)}. To handle streams uses
	 * ReaderCharSequence, so not all possible regular expressions are accepted.
	 * See {@link ReaderCharSequence} for details.
	 * 
	 * @param document
	 *            Document containing language rules.
	 * @param languageCode
	 *            Language code to select the rules.
	 * @param reader
	 *            Reader from which text will be read.
	 */
	public SrxTextIterator(SrxDocument document, String languageCode,
			Reader reader) {
		this(document, languageCode, new ReaderCharSequence(reader));
	}

	/**
	 * {@inheritDoc}
	 */
	public String next() {
		if (hasNext()) {
			boolean found = false;
			if (breakingMatcher != null) {
				while (!found && find(breakingMatcher)) {
					endPosition = breakingMatcher.end();
					// When there's more than one breaking rule at the given
					// place only the first is matched, the rest is skipped.
					// So if position is not increasing the new rules are
					// applied in the same place as previously matched rule.
					if (endPosition > startPosition) {
						// Index of current capturing group
						int groupIndex = 1;
						found = true;

						for (Pattern nonBreakingPatern : 
							mergedPattern.getNonBreakingPatternList()) {

							// Null non breaking pattern does not match anything
							if (nonBreakingPatern != null) {
								Matcher nonBreakingMatcher = nonBreakingPatern
										.matcher(text);
								nonBreakingMatcher.useTransparentBounds(true);
								nonBreakingMatcher.region(endPosition,
										endPosition + 1);
								found = !nonBreakingMatcher.lookingAt();
							}

							// Break when non-breaking rule matches or
							// the current group index is equal to breaking
							// rule index, so further non-breaking rules
							// do not apply to this breaking rule.
							String group = breakingMatcher.group(groupIndex);
							if (!found || group != null) {
								break;
							}

							++groupIndex;
							
						}

					}
				}
			}
			if (!found || !canReadNextChar()) {
				endPosition = text.length();
			}
			segment = text.subSequence(startPosition, endPosition).toString();
			startPosition = endPosition;
			return segment;
		} else {
			return null;
		}
	}
	
	/**
	 * Checks if next character can be read. It is needed as
	 * {@link net.rootnode.loomchild.util.io.ReaderCharSequence} throws
	 * EndOfStreamException at the end.
	 * @return True if next character is available.
	 */
	private boolean canReadNextChar() {
		try {
			if (endPosition < text.length()) {
				text.charAt(endPosition);
				return true;
			} else {
				return false;
			}
		} catch (EndOfStreamException e) {
			return false;
		}
	}

	/**
	 * Searches for the next pattern occurrence and return true if it has been
	 * found, false otherwise. It is needed as
	 * {@link net.rootnode.loomchild.util.io.ReaderCharSequence} throws
	 * EndOfStreamException at the end.
	 * 
	 * @param matcher
	 *            Matcher which will perform search.
	 * @return Returns true when the matcher found the pattern.
	 */
	private boolean find(Matcher matcher) {
		try {
			return matcher.find();
		} catch (EndOfStreamException e) {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		return (startPosition < text.length());
	}

}
