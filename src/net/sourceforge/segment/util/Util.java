package net.sourceforge.segment.util;

import static junit.framework.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import junit.framework.AssertionFailedError;
import net.sourceforge.segment.srx.SrxDocument;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class Util {
	
	public static final int READ_BUFFER_SIZE = 1024;

	public static final int DEFAULT_FINITE_INFINITY = 100;

	private static final Pattern STAR_PATTERN = Pattern
			.compile("(?<=(?<!\\\\)(?:\\\\\\\\){0,100})\\*");

	private static final Pattern PLUS_PATTERN = Pattern
			.compile("(?<=(?<!\\\\)(?:\\\\\\\\){0,100})(?<![\\?\\*\\+]|\\{[0-9],?[0-9]?\\}?\\})\\+");

	private static final Pattern RANGE_PATTERN = Pattern
			.compile("(?<=(?<!\\\\)(?:\\\\\\\\){0,100})\\{\\s*([0-9]+)\\s*,\\s*\\}");

	private static final Pattern CAPTURING_GROUP_PATTERN = Pattern
			.compile("(?<=(?<!\\\\)(?:\\\\\\\\){0,100})\\((?!\\?)");

	public static BufferedReader getReader(InputStream inputStream) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream, "utf-8"));
			return reader;
		} catch (UnsupportedEncodingException e) {
			throw new IORuntimeException(e);
		}
	}

	public static PrintWriter getWriter(OutputStream outputStream) {
		try {
			return new PrintWriter(new OutputStreamWriter((outputStream),
					"utf-8"), true);
		} catch (UnsupportedEncodingException e) {
			throw new IORuntimeException(e);
		}
	}

	public static FileInputStream getFileInputStream(String fileName) {
		try {
			return new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			throw new IORuntimeException(e);
		}
	}

	public static FileOutputStream getFileOutputStream(String fileName) {
		try {
			return new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * Znajduje zasób i zwraca go w postaci strumienia wejściowego. Do szukania
	 * zasobu używa systemowego Classloader-a.
	 * 
	 * @param name
	 *            Nazwa zasobu.
	 * @return Zwraca strumień wejściowy zasobu.
	 * @throws ResourceNotFoundException
	 *             Zgłaszany gdy nie udało się odnaleźć zasobu.
	 */
	public static InputStream getResourceStream(String name) {
		InputStream inputStream = Util.class.getClassLoader()
				.getResourceAsStream(name);
		if (inputStream == null) {
			throw new ResourceNotFoundException(name);
		}
		return inputStream;
	}

	/**
	 * Znajduje ścieżkę do zasobu. Trzeba ograniczyć używanie ponieważ nie
	 * działa poprawnie gdy zasób znajduje się w archiwum JAR.
	 * 
	 * @param name
	 *            Nazwa zasobu.
	 * @return Zwraca ścieżkę do zasobu.
	 * @throws ResourceNotFoundException
	 *             Zgłaszany gdy nie udało się odnaleźć zasobu.
	 */
	public static String getResourcePath(String name) {
		URL url = Util.class.getClassLoader().getResource(name);
		if (url == null) {
			throw new ResourceNotFoundException(name);
		}
		return url.getPath();
	}
	
	public static String read(Reader reader, int count) {
		try {
			char[] readBuffer = new char[count];
			reader.read(readBuffer);
			return new String(readBuffer);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * Wczytuje całą zawartość strumienia wejściowego do napisu. W razie
	 * niepowodzenia zgłasza wyjątek.
	 * 
	 * @param reader
	 *            Strumień wejściowy.
	 * @return Zwraca napis zawierający odczytany strumień.
	 * @throws IORuntimeException
	 *             gdy wystąpi błąd IO.
	 */
	public static String readAll(Reader reader) {
		StringWriter writer = new StringWriter();
		copyAll(reader, writer);
		return writer.toString();
	}

	public static void copyAll(Reader reader, Writer writer) {
		try {
			char[] readBuffer = new char[READ_BUFFER_SIZE];
			int count;
			while ((count = reader.read(readBuffer)) != -1) {
				writer.write(readBuffer, 0, count);
			}
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	public static String getFileExtension(String fileName) {
		int dotPosition = fileName.lastIndexOf('.');
		if (dotPosition == -1) {
			return "";
		} else {
			return fileName.substring(dotPosition);
		}
	}
	
	public static final String MANIFEST_PATH = "/META-INF/MANIFEST.MF";

	/**
	 * Returns Manifest of a jar containing given class. If class is not
	 * in a jar, throws {@link ResourceNotFoundException}.
	 * @param klass Class.
	 * @return Manifest.
	 * @throws ResourceNotFoundException Thrown if manifest was not found.
	 */
	public static Manifest getJarManifest(Class<?> klass) {
        URL classUrl = klass.getResource(klass.getSimpleName() + ".class");
        if (classUrl == null) {
            throw new IllegalArgumentException("Class not found: " + 
                    klass.getName() + ".");
        }
        String classPath = classUrl.toString();
        int jarIndex = classPath.indexOf('!');
        if (jarIndex != -1) {
            String manifestPath = 
            	classPath.substring(0, jarIndex + 1) + MANIFEST_PATH;
            try {
                URL manifestUrl = new URL(manifestPath);
                InputStream manifestStream = manifestUrl.openStream();
                Manifest manifest = new Manifest(manifestStream);
                return manifest;
            } 
            catch (IOException e) {
            	throw new ResourceNotFoundException(
            			"IO Error retrieving manifest.", e);
            }
        }
        else {
        	throw new ResourceNotFoundException(
        			"Class is not in a JAR archive " + klass.getName() + ".");
        }
    }
	
	public static XMLReader getXmlReader(Schema schema) {
		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setValidating(false);
			parserFactory.setNamespaceAware(true);
			if (schema != null) {
				parserFactory.setSchema(schema);
			}
			SAXParser saxParser = parserFactory.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setEntityResolver(new IgnoreDTDEntityResolver());
			return xmlReader;
		} catch (ParserConfigurationException e) {
			throw new XmlException("SAX Parser configuration error.", e);
		} catch (SAXException e) {
			throw new XmlException("Error creating XMLReader.", e);
		}
	}

	public static XMLReader getXmlReader() {
		return getXmlReader(null);
	}

	public static Schema getSchema(Reader reader) {
		return getSchema(new Reader[] { reader });
	}

	public static Schema getSchema(Reader[] readerArray) {
		try {
			Source[] sourceArray = new Source[readerArray.length];
			for (int i = 0; i < readerArray.length; ++i) {
				Reader reader = readerArray[i];
				Source source = new StreamSource(reader);
				sourceArray[i] = source;
			}
			SchemaFactory schemaFactory = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(sourceArray);
			return schema;
		} catch (SAXException e) {
			throw new XmlException("Error creating XML Schema.", e);
		}
	}

	public static Source getSource(Reader reader, Schema schema) {
		Source source = new SAXSource(getXmlReader(schema),
				new InputSource(reader));
		return source;
	}

	public static JAXBContext getContext(String context) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(context);
			return jaxbContext;
		} catch (JAXBException e) {
			throw new XmlException("Error creating JAXB context", e);
		}
	}

	public static JAXBContext getContext(String context, 
			ClassLoader classLoader) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(context, classLoader);
			return jaxbContext;
		} catch (JAXBException e) {
			throw new XmlException("Error creating JAXB context", e);
		}
	}

	public static JAXBContext getContext(Class<?>... classesToBeBound) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(classesToBeBound);
			return jaxbContext;
		} catch (JAXBException e) {
			throw new XmlException("Error creating JAXB context", e);
		}
	}
	
	public static Templates getTemplates(Reader reader) {
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			Source source = new StreamSource(reader);
			Templates templates;
			templates = factory.newTemplates(source);
			return templates;
		} catch (TransformerConfigurationException e) {
			throw new XmlException("Error creating XSLT templates.", e);
		}
	}

	public static void transform(Templates templates, Schema schema,
			Reader reader, Writer writer, Map<String, Object> parameterMap) {
		try {
			Source source = getSource(reader, schema);
			Result result = new StreamResult(writer);
			Transformer transformer = templates.newTransformer();
			transformer.setErrorListener(new TransformationErrorListener());
			for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
				transformer.setParameter(entry.getKey(), entry.getValue());
			}
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			throw new XmlException("Error creating XSLT transformer.", e);
		} catch (TransformerException e) {
			throw new XmlException("XSLT transformer error.", e);
		}
	}

	public static void transform(Templates templates, Reader reader,
			Writer writer, Map<String, Object> parameterMap) {
		transform(templates, null, reader, writer, parameterMap);
	}

	public static void transform(Templates templates, Schema schema,
			Reader reader, Writer writer) {
		Map<String, Object> parameterMap = Collections.emptyMap();
		transform(templates, schema, reader, writer, parameterMap);
	}

	public static void transform(Templates templates, Reader reader,
			Writer writer) {
		transform(templates, null, reader, writer);
	}
	
	public static void validate(Schema schema, Reader reader) {
		try {
			Source source = new StreamSource(reader);
			Validator validator = schema.newValidator();
			validator.validate(source);
		} catch (SAXException e) {
			throw new XmlException("Validation error.", e);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}
	
	/**
	 * Replaces block quotes in regular expressions with normal quotes. For
	 * example "\Qabc\E" will be replace with "\a\b\c".
	 * 
	 * @param pattern
	 *            Pattern.
	 * @return Returns pattern with replaced block quotes.
	 */
	public static String removeBlockQuotes(String pattern) {
		StringBuilder patternBuilder = new StringBuilder();
		boolean quote = false;
		char previousChar = 0;

		for (int i = 0; i < pattern.length(); ++i) {
			char currentChar = pattern.charAt(i);

			if (quote) {
				if (previousChar == '\\' && currentChar == 'E') {
					quote = false;
					// Need to remove "\\" at the end as it has been added
					// in previous iteration.
					patternBuilder.delete(patternBuilder.length() - 2,
							patternBuilder.length());
				} else {
					patternBuilder.append('\\');
					patternBuilder.append(currentChar);
				}
			} else {
				if (previousChar == '\\' && currentChar == 'Q') {
					quote = true;
					// Need to remove "\" at the end as it has been added
					// in previous iteration.
					patternBuilder.deleteCharAt(patternBuilder.length() - 1);
				} else {
					patternBuilder.append(currentChar);
				}
			}

			previousChar = currentChar;
		}

		return patternBuilder.toString();
	}

	/**
	 * Changes unlimited length pattern to limited length pattern. It is done by
	 * replacing "*" and "+" symbols with their finite counterparts - "{0,n}"
	 * and {1,n}. As a side effect block quotes are replaced with normal quotes
	 * by using {@link #removeBlockQuotes(String)}.
	 * 
	 * @param pattern
	 *            Pattern to be finitized.
	 * @param infinity
	 *            n number.
	 * @return Returns limited length pattern.
	 */
	public static String finitize(String pattern, int infinity) {
		String finitePattern = removeBlockQuotes(pattern);
		
		Matcher starMatcher = STAR_PATTERN.matcher(finitePattern);
		finitePattern = starMatcher.replaceAll("{0," + infinity + "}");
		
		Matcher plusMatcher = PLUS_PATTERN.matcher(finitePattern);
		finitePattern = plusMatcher.replaceAll("{1," + infinity + "}");
		
		Matcher rangeMatcher = RANGE_PATTERN.matcher(finitePattern);
		finitePattern = rangeMatcher.replaceAll("{$1," + infinity + "}");
		
		return finitePattern;
	}

	/**
	 * Finitizes pattern with default infinity. {@link #finitize(String, int)}
	 * 
	 * @param pattern
	 *            Pattern to be finitized.
	 * @return Finite pattern.
	 */
	public static String finitize(String pattern) {
		return finitize(pattern, DEFAULT_FINITE_INFINITY);
	}
	
	public static Pattern compile(SrxDocument document, String regex) {
		Pattern pattern = document.getCache().get(regex, Pattern.class);
		if (pattern == null) {
			pattern = Pattern.compile(regex);
			document.getCache().put(regex, pattern);
		}
		return pattern;
	}

	/**
	 * Replaces capturing groups with non-capturing groups in the given regular
	 * expression. As a side effect block quotes are replaced with normal quotes
	 * by using {@link #removeBlockQuotes(String)}.
	 * 
	 * @param pattern
	 *            Pattern.
	 * @return Returns modified pattern.
	 */
	public static String removeCapturingGroups(String pattern) {
		String newPattern = removeBlockQuotes(pattern);
		Matcher capturingGroupMatcher = CAPTURING_GROUP_PATTERN
				.matcher(newPattern);
		newPattern = capturingGroupMatcher.replaceAll("(?:");
		return newPattern;
	}
	
	/**
	 * Asserts that list has given contents. Otherwise throws exception. To be
	 * used in testing.
	 * 
	 * @param <T>
	 *            List type
	 * @param expectedArray
	 *            Expected list contents.
	 * @param actualList
	 *            List to be checked.
	 * @throws AssertionFailedError
	 *             Thrown when list are not equal.
	 */
	public static <T> void assertListEquals(String message, T[] expectedArray,
			List<T> actualList) {
		assertEquals(message, expectedArray.length, actualList.size());
		Iterator<T> actualIterator = actualList.iterator();
		for (T expected : expectedArray) {
			T actual = actualIterator.next();
			assertEquals(message, expected, actual);
		}
	}

	public static <T> void assertListEquals(T[] expectedArray,
			List<T> actualList) {
		assertListEquals("", expectedArray, actualList);
	}

}
