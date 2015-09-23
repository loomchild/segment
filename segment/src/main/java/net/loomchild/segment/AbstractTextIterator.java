package net.loomchild.segment;

import java.util.Iterator;
import java.util.List;

import net.loomchild.segment.srx.LanguageRule;


/**
 * Represents abstract text iterator. Responsible for implementing remove
 * operation.
 * 
 * @author loomchild
 * 
 */
public abstract class AbstractTextIterator implements TextIterator {

	/**
	 * {@inheritDoc}
	 */
	public void remove() {
		throw new UnsupportedOperationException(
				"Remove is not supported by TextIterator.");
	}

	public String toString(List<LanguageRule> languageRuleList) {
		StringBuilder builder = new StringBuilder();
		if (languageRuleList.size() > 0) {
			Iterator<LanguageRule> iterator = languageRuleList.iterator();
			builder.append(iterator.next().getName());
			while (iterator.hasNext()) {
				builder.append(iterator.next().getName());
			}
		}
		return builder.toString();
	}
	

	
}
