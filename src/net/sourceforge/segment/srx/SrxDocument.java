package net.sourceforge.segment.srx;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents SRX document. Responsible for storing and searching matching
 * language rules for given language code.
 * 
 * @author loomchild
 */
public class SrxDocument {

	/**
	 * Default cascade value.
	 */
	public static final boolean DEFAULT_CASCADE = true;

	private boolean cascade;

	private List<LanguageMap> languageMapList;
	
	private SrxDocumentCache cache;

	/**
	 * Creates empty document.
	 * 
	 * @param cascade true if document is cascading
	 */
	public SrxDocument(boolean cascade) {
		this.cascade = cascade;
		this.languageMapList = new ArrayList<LanguageMap>();
		this.cache = new SrxDocumentCache();
	}

	/**
	 * Creates empty document with default cascade. See {@link #DEFAULT_CASCADE}.
	 */
	public SrxDocument() {
		this(DEFAULT_CASCADE);
	}

	/**
	 * Sets if document is cascading or not.
	 * 
	 * @param cascade true f document is cascading
	 */
	public void setCascade(boolean cascade) {
		this.cascade = cascade;
	}

	/**
	 * @return true if document is cascading
	 */
	public boolean getCascade() {
		return cascade;
	}

	/**
	 * Add language map to this document.
	 * 
	 * @param pattern language code pattern
	 * @param languageRule
	 */
	public void addLanguageMap(String pattern, LanguageRule languageRule) {
		LanguageMap languageMap = new LanguageMap(pattern, languageRule);
		languageMapList.add(languageMap);
	}

	/**
	 * If cascade is true then returns all language rules matching given
	 * language code. If cascade is false returns first language rule matching
	 * given language code. If no matching language rules are found returns
	 * empty list.
	 * 
	 * @param languageCode language code, for example en_US
	 * @return matching language rules
	 */
	public List<LanguageRule> getLanguageRuleList(String languageCode) {
		List<LanguageRule> matchingLanguageRuleList = new ArrayList<LanguageRule>();
		for (LanguageMap languageMap : languageMapList) {
			if (languageMap.matches(languageCode)) {
				matchingLanguageRuleList.add(languageMap.getLanguageRule());
				if (!cascade) {
					break;
				}
			}
		}
		return matchingLanguageRuleList;
	}

	public SrxDocumentCache getCache() {
		return cache;
	}
	
}
