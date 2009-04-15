package textsplitter;

import java.io.IOException;
import java.io.Reader;

//package pl.poleng.anat;
//import pl.poleng.schema.*;

/**
 * Obiekt dzielący strumień znaków (Reader) na kawałki w postaci obiektów klasy
 * String.
 * 
 * 
 * @author Mikołaj Wypych
 */
public interface TextSplitter {
	/**
	 * Inicjalizuje dzielenie tekstu na części.
	 * 
	 * 
	 * @param reader
	 *            Reader przekazujący tekst zawierający znaczniki TMX
	 * @param languageCode
	 *            Język naturalny w jakim zapisany jest tekst. Kod dwuliterowy
	 *            wg. normy ISO 639.
	 */
	public void initialize(Reader reader, String languageCode);

	// public void initialize(pl.poleng.schema.Reader rerader, String
	// languageCode);

	/**
	 * Informuje o stanie strumienia wejściowego.
	 * 
	 * 
	 * @return true iif w strumieniu wejściowym wystąpił end-of-file.
	 */
	public boolean eofOccured();

	/**
	 * Informuje, czy są dalsze kawałki tekstu do pobrania.
	 * 
	 */
	public boolean hasMoreStrings() throws IOException;

	/**
	 * Podaje następny kawałek tekstu ze strumienia wejściowego.
	 * 
	 * 
	 * @return Zwraca kolejny segment.
	 */
	public String nextString() throws IOException;
}
