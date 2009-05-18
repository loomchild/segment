package net.sourceforge.segment.srx;

import java.io.Reader;
import java.util.List;
import java.util.regex.Pattern;

import net.rootnode.loomchild.util.io.ReaderCharSequence;
import net.rootnode.loomchild.util.regex.ReaderMatcher;
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

	private ReaderMatcher breakingMatcher;

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

		List<LanguageRule> languageRuleList = 
			document.getLanguageRuleList(languageCode);
		
		this.mergedPattern = 
			(MergedPattern)document.getCache().get(languageRuleList);
		if (mergedPattern == null) {
			mergedPattern = new MergedPattern(languageRuleList);
			document.getCache().put(languageRuleList, mergedPattern);
		}
		
		if (mergedPattern.getBreakingPattern() != null) {
			this.breakingMatcher = new ReaderMatcher(
					mergedPattern.getBreakingPattern(), text);
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
	 * @param length
	 * 			  Length of stream in reader.
	 * @param bufferSize
	 * 			  Reader buffer size. Segments cannot be longer than this value.
	 */
	public SrxTextIterator(SrxDocument document, String languageCode,
			Reader reader, int length, int bufferSize) {
		this(document, languageCode, new ReaderCharSequence(reader, length, 
				bufferSize));
	}

	/**
	 * Creates streaming text iterator with default buffer size 
	 * ({@link ReaderCharSequence#DEFAULT_BUFFER_SIZE}). 
	 * See {@link #SrxTextIterator(SrxDocument, String, Reader, int, int)}}.
	 */
	public SrxTextIterator(SrxDocument document, String languageCode,
			Reader reader, int length) {
		this(document, languageCode, 
				new ReaderCharSequence(reader, length));
	}

	/**
	 * Creates streaming text iterator with unknown stream length. 
	 * See {@link #SrxTextIterator(SrxDocument, String, Reader, int, int)}}.
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
				while (!found && breakingMatcher.find()) {
					endPosition = breakingMatcher.end();
					// When there's more than one breaking rule at the given
					// place only the first is matched, the rest is skipped.
					// So if position is not increasing the new rules are
					// applied in the same place as previously matched rule.
					if (endPosition > startPosition) {
						// Index of current capturing group
						int groupIndex = 1;
						found = true;

						for (Pattern nonBreakingPattern : 
							mergedPattern.getNonBreakingPatternList()) {

							// Null non breaking pattern does not match anything
							if (nonBreakingPattern != null) {
								ReaderMatcher nonBreakingMatcher = 
									new ReaderMatcher(nonBreakingPattern, text);
								nonBreakingMatcher.useTransparentBounds(true);
								// When using transparent bound the upper bound
								// is not important?
								// Needed because text.length() is unknown.
								nonBreakingMatcher.region(endPosition,
										endPosition);
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
