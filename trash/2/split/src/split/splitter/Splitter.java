package split.splitter;

import java.io.IOException;

/**
 * Interfejs splittera. 
 *
 * @author loomchild
 */
public interface Splitter {

	/**
	 * @return Zwraca kolejny segment albo null gdy napotkano koniec strumienia. 
	 * @throws IOException Zgłaszany gdy nastąpi błąd We/Wy strumienia.
	 */
	public String next() throws IOException;
	
	/**
	 * @return Zwraca true jeśli istnieją kolejne segmenty.
	 */
	public boolean hasNext();

	/**
	 * @return Zwraca true jeśli następne wywołanie next nie zablokuje strumienia. 
	 * @throws IOException Zgłaszany gdy nastąpi błąd We/Wy strumienia.
	 */
	public boolean isReady() throws IOException;


}
