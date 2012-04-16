package net.sourceforge.segment.srx.legacy;

import static net.sourceforge.segment.util.Util.getParameter;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.sourceforge.segment.AbstractTextIterator;
import net.sourceforge.segment.srx.LanguageRule;
import net.sourceforge.segment.srx.SrxDocument;
import net.sourceforge.segment.srx.SrxTextIterator;

/**
 * Represents fast text iterator that splits text according to SRX rules.
 * 
 * @author loomchild
 */
public class FastTextIterator extends AbstractTextIterator {

	private CharSequence text;

	private String segment;

	private MergedPattern mergedPattern;

	private ReaderMatcher breakingMatcher;

	private int startPosition, endPosition;

	/**
	 * Creates text iterator that obtains language rules form given document
	 * using given language code. To retrieve language rules calls
	 * {@link SrxDocument#getLanguageRuleList(String)}. 
	 * Supported parameters: 
	 * {@link SrxTextIterator#MAX_LOOKBEHIND_CONSTRUCT_LENGTH_PARAMETER}.
	 * 
	 * @param document
	 *            document containing language rules
	 * @param languageCode
	 *            language code to select the rule
	 * @param text
	 * @param parameterMap
	 *            additional segmentation parameters
	 */
	public FastTextIterator(SrxDocument document, String languageCode,
			CharSequence text, Map<String, Object> parameterMap) {

		this.text = text;
		this.segment = null;
		this.startPosition = 0;
		this.endPosition = 0;

		int maxLookbehindConstructLength = getParameter(parameterMap
				.get(SrxTextIterator.MAX_LOOKBEHIND_CONSTRUCT_LENGTH_PARAMETER),
				SrxTextIterator.DEFAULT_MAX_LOOKBEHIND_CONSTRUCT_LENGTH);
		List<LanguageRule> languageRuleList = document.getLanguageRuleList(languageCode);

		this.mergedPattern = document.getCache().get(MergedPattern.class, languageRuleList,
				maxLookbehindConstructLength);
		if (mergedPattern == null) {
			mergedPattern = new MergedPattern(languageRuleList,
					maxLookbehindConstructLength);
			document.getCache().put(mergedPattern, MergedPattern.class, languageRuleList,
					maxLookbehindConstructLength);
		}

		if (mergedPattern.getBreakingPattern() != null) {
			this.breakingMatcher = new ReaderMatcher(mergedPattern
					.getBreakingPattern(), text);
		}

	}

	/**
	 * Creates text iterator with no additional parameters.
	 * 
	 * @see #FastTextIterator(SrxDocument, String, CharSequence, Map)
	 * @param document
	 *            document containing language rules
	 * @param languageCode
	 *            language code to select the rule
	 * @param text
	 */
	public FastTextIterator(SrxDocument document, String languageCode,
			CharSequence text) {
		this(document, languageCode, text, new HashMap<String, Object>());
	}

	/**
	 * Creates streaming text iterator that obtains language rules form given
	 * document using given language code. To retrieve language rules calls
	 * {@link SrxDocument#getLanguageRuleList(String)}. To handle streams uses
	 * ReaderCharSequence, so not all possible regular expressions are accepted.
	 * See {@link ReaderCharSequence} for details. 
	 * Supported parameters:
	 * {@link SrxTextIterator#BUFFER_LENGTH_PARAMETER}, 
	 * {@link SrxTextIterator#MAX_LOOKBEHIND_CONSTRUCT_LENGTH_PARAMETER}.
	 * 
	 * @param document
	 *            document containing language rules
	 * @param languageCode
	 *            language code to select the rules
	 * @param reader
	 *            reader from which text will be read
	 * @param parameterMap
	 *            additional segmentation parameters
	 */
	public FastTextIterator(SrxDocument document, String languageCode,
			Reader reader, Map<String, Object> parameterMap) {
		this(document, languageCode, new ReaderCharSequence(reader,
				getParameter(parameterMap
						.get(SrxTextIterator.BUFFER_LENGTH_PARAMETER),
						SrxTextIterator.DEFAULT_BUFFER_LENGTH)), parameterMap);
	}

	/**
	 * Creates streaming text iterator with no additional parameters.
	 * 
	 * @see #FastTextIterator(SrxDocument, String, Reader, Map)
	 * @param document
	 *            document containing language rules
	 * @param languageCode
	 *            language code to select the rules
	 * @param reader
	 *            reader from which text will be read
	 */
	public FastTextIterator(SrxDocument document, String languageCode,
			Reader reader) {
		this(document, languageCode, reader, new HashMap<String, Object>());
	}

	/**
	 * {@inheritDoc}
	 */
	public String next() {
		if (hasNext()) {
			boolean found = false;
			if (breakingMatcher != null) {
				while (!found && breakingMatcher.find()) {

					// Find which breaking rule was matched in the matcher.
					// It must have matched some rule so check for
					// breakingMatcher.groupCount() is not necessary.
					int breakingRuleIndex = 1;
					while (breakingMatcher.group(breakingRuleIndex) == null) {
						++breakingRuleIndex;
					}

					// Breaking position is at the end of the group.
					endPosition = breakingMatcher.end(breakingRuleIndex);

					// When there's more than one breaking rule at the given
					// place only the first is matched, the rest is skipped.
					// So if position is not increasing the new rules are
					// applied in the same place as previously matched rule.
					if (endPosition > startPosition) {

						found = true;

						// Get non breaking patterns that are applicable
						// to breaking rule just matched.
						List<Pattern> activeNonBreakingPatternList = mergedPattern
								.getNonBreakingPatternList(breakingRuleIndex);

						for (Pattern nonBreakingPattern : activeNonBreakingPatternList) {

							// Null non breaking pattern does not match anything
							if (nonBreakingPattern != null) {
								ReaderMatcher nonBreakingMatcher = new ReaderMatcher(
										nonBreakingPattern, text);
								nonBreakingMatcher.useTransparentBounds(true);
								// When using transparent bound the upper bound
								// is not important?
								// Needed because text.length() is unknown.
								nonBreakingMatcher.region(endPosition,
										endPosition);
								found = !nonBreakingMatcher.lookingAt();
							}

							// Break when non-breaking rule matches
							if (!found) {
								break;
							}

						}

					}
				}
				// Breaking matcher cannot match text behind segment start in
				// the future.
				if (found && endPosition < text.length()) {
					breakingMatcher.region(endPosition, text.length());
				}
			}
			if (!found) {
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
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		return (startPosition < text.length());
	}

}
