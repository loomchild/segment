package net.sourceforge.segment.srx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents SRX document cache.
 * Responsible for managing cached data.
 * 
 * @author loomchild
 */
public class SrxDocumentCache {
	
	private Map<String, Object> map;
	
	public SrxDocumentCache() {
		this.map = new ConcurrentHashMap<String, Object>();
	}
	
	/**
	 * Retrieves object from cache. 
	 * @param key
	 * @return value object
	 */
	public Object get(String key) {
		Object value = map.get(key);
		return value;
	}

	/**
	 * Puts an object in cache.
	 * @param <T> value object type
	 * @param key
	 * @param value value object
	 */
	public void put(String key, Object value) {
		map.put(key, value);
	}
	
}
