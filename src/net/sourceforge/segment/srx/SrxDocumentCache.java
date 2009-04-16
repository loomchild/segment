package net.sourceforge.segment.srx;

import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for storing cached data.
 * 
 * @author loomchild
 */
public class SrxDocumentCache {

	private Map<Object, Object> map;
	
	public SrxDocumentCache() {
		this.map = new HashMap<Object, Object>();
	}
	
	public Object get(Object key) {
		return map.get(key);
	}

	public void put(Object key, Object value) {
		map.put(key, value);
	}
	
}
