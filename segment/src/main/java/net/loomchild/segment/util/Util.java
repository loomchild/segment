package net.loomchild.segment.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
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

import net.loomchild.segment.srx.SrxDocument;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class Util {
	
	public static final int READ_BUFFER_SIZE = 1024;

	public static final String MANIFEST_PATH = "/META-INF/MANIFEST.MF";

	private static final Pattern STAR_PATTERN = Pattern
			.compile("(?<=(?<!\\\\)(?:\\\\\\\\){0,100})\\*");

	private static final Pattern PLUS_PATTERN = Pattern
			.compile("(?<=(?<!\\\\)(?:\\\\\\\\){0,100})(?<![\\?\\*\\+]|\\{[0-9],?[0-9]?\\}?\\})\\+");

	private static final Pattern RANGE_PATTERN = Pattern
			.compile("(?<=(?<!\\\\)(?:\\\\\\\\){0,100})\\{\\s*([0-9]+)\\s*,\\s*\\}");

	private static final Pattern CAPTURING_GROUP_PATTERN = Pattern
			.compile("(?<=(?<!\\\\)(?:\\\\\\\\){0,100})\\((?!\\?)");

	/**
	 * @param inputStream
	 * @return UTF-8 encoded reader from given input stream
	 * @throws IORuntimeException if IO error occurs
	 */
	public static Reader getReader(InputStream inputStream) {
		try {
			Reader reader = new InputStreamReader(inputStream, "utf-8");
			return reader;
		} catch (UnsupportedEncodingException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * @param outputStream
	 * @return UTF-8 encoded writer to a given output stream
	 * @throws IORuntimeException if IO error occurs
	 */
	public static Writer getWriter(OutputStream outputStream) {
		try {
			Writer writer = new OutputStreamWriter(outputStream, "utf-8");
			return writer;
		} catch (UnsupportedEncodingException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * Opens a file for reading and returns input stream associated with it.
	 * @param fileName
	 * @return input stream
	 * @throws IORuntimeException if IO error occurs
	 */
	public static FileInputStream getFileInputStream(String fileName) {
		try {
			return new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * Opens a file for writing and returns output stream associated with it.
	 * @param fileName
	 * @return output stream
	 * @throws IORuntimeException if IO error occurs
	 */
	public static FileOutputStream getFileOutputStream(String fileName) {
		try {
			return new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * Finds a resource using system classloader and returns it as
	 * input stream.
	 * @see ClassLoader
	 * 
	 * @param name resource name
	 * @return resource input stream
	 * @throws ResourceNotFoundException if resource can not be found 
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
	 * Reads whole contents of a reader to a string.
	 * 
	 * @param reader
	 * @return a string containing reader contents
	 * @throws IORuntimeException on IO error
	 */
	public static String readAll(Reader reader) {
		StringWriter writer = new StringWriter();
		copyAll(reader, writer);
		return writer.toString();
	}

	/**
	 * Copies the whole content of a reader to a writer.
	 * @param reader
	 * @param writer
	 * @throws IORuntimeException on IO error
	 */
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
	
	/**
	 * Returns Manifest of a jar containing given class. If class is not
	 * in a jar, throws {@link ResourceNotFoundException}.
	 * @param klass class
	 * @return manifest
	 * @throws ResourceNotFoundException if manifest was not found
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
	
	/**
	 * Returns XMLReader validating against given XML schema. 
	 * The reader ignores DTD defined in XML file.
	 * @param schema
	 * @return XMLReader
	 * @throws XmlException when SAX error occurs
	 */
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

	/**
	 * @see Util#getXmlReader(Schema)
	 * @return XMLReader without XML schema associated with it
	 * @throws XmlException when SAX error occurs
	 */
	public static XMLReader getXmlReader() {
		return getXmlReader(null);
	}

	/**
	 * Reads a XML schema from given reader. 
	 * @param reader
	 * @return XML Schema
	 * @throws XmlException when XML schema parsing error occurs
	 */
	public static Schema getSchema(Reader reader) {
		return getSchema(new Reader[] { reader });
	}

	/**
	 * Reads a XML schema from given readers. Schema files can depend on
	 * one another.
	 * @param readerArray readers containing XML schemas
	 * @return XML Schema object
	 * @throws XmlException when XML schema parsing error occurs
	 */
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

	/**
	 * @param reader
	 * @param schema XML schema
	 * @return XML source from given reader and with given schema 
	 */
	public static Source getSource(Reader reader, Schema schema) {
		Source source = new SAXSource(getXmlReader(schema),
				new InputSource(reader));
		return source;
	}

	/**
	 * @param context context package name
	 * @return JAXB context
	 * @throws XmlException if JAXB error occurs
	 */
	public static JAXBContext getContext(String context) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(context);
			return jaxbContext;
		} catch (JAXBException e) {
			throw new XmlException("Error creating JAXB context", e);
		}
	}

	/**
	 * @param context context package name
	 * @param classLoader class loader used to load classes from the context
	 * @return JAXB context; loads the classes using given classloader
	 * @throws XmlException if JAXB error occurs
	 */
	public static JAXBContext getContext(String context, 
			ClassLoader classLoader) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(context, classLoader);
			return jaxbContext;
		} catch (JAXBException e) {
			throw new XmlException("Error creating JAXB context", e);
		}
	}

	/**
	 * @param classesToBeBound
	 * @return JAXBContext according to classes to bind 
	 * Dependency classes are also loaded automatically.
	 * @throws XmlException if JAXB error occurs
	 */
	public static JAXBContext getContext(Class<?>... classesToBeBound) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(classesToBeBound);
			return jaxbContext;
		} catch (JAXBException e) {
			throw new XmlException("Error creating JAXB context", e);
		}
	}

	/**
	 * Returns XML transform templates from given reader containing XSLT 
	 * stylesheet.
	 * @param reader
	 * @return templates; they can be reused many times to perform the transformation
	 * @throws XmlException if XML parsing error occurs
	 */
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

	/**
	 * Performs XML schema validation and XSLT transformation.
	 * @param templates XSLT stylesheet
	 * @param schema XML schema to validate against
	 * @param reader reader with input document
	 * @param writer writer which will be used to write output
	 * @param parameterMap transformation parameters
	 * @throws XmlException if transformation error occurs
	 */
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

	/**
	 * Performs XML schema validation and XSLT transformation.
	 * @param templates XSLT stylesheet
	 * @param schema XML schema to validate against
	 * @param reader reader with input document
	 * @param writer writer which will be used to write output
	 * @throws XmlException if transformation error occurs
	 */
	public static void transform(Templates templates, Schema schema,
			Reader reader, Writer writer) {
		Map<String, Object> parameterMap = Collections.emptyMap();
		transform(templates, schema, reader, writer, parameterMap);
	}

	/**
	 * Performs XSLT transformation.
	 * @param templates XSLT stylesheet
	 * @param reader reader with input document
	 * @param writer writer which will be used to write output
	 * @param parameterMap transformation parameters
	 * @throws XmlException if transformation error occurs
	 */
	public static void transform(Templates templates, Reader reader, 
			Writer writer, Map<String, Object> parameterMap) {
		transform(templates, null, reader, writer, parameterMap); 
	}

	/**
	 * Performs XSLT transformation.
	 * @param templates XSLT stylesheet
	 * @param reader reader with input document
	 * @param writer writer which will be used to write output
	 * @throws XmlException if transformation error occurs
	 */
	public static void transform(Templates templates, Reader reader,
			Writer writer) {
		Map<String, Object> parameterMap = Collections.emptyMap();
		transform(templates, reader, writer, parameterMap);
	}
	
	/**
	 * Replaces block quotes in regular expressions with normal quotes. For
	 * example "\Qabc\E" will be replace with "\a\b\c".
	 * 
	 * @param pattern
	 * @return pattern with replaced block quotes
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
	 * replacing constructs with "*" and "+" symbols with their finite 
	 * counterparts - "{0,n}" and {1,n}. 
	 * As a side effect block quotes are replaced with normal quotes
	 * by using {@link #removeBlockQuotes(String)}.
	 * 
	 * @param pattern pattern to be finitized
	 * @param infinity "n" number
	 * @return limited length pattern
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

	public static Pattern compile(SrxDocument document, String regex) {
		String key = "PATTERN_" + regex;
		Pattern pattern = (Pattern)document.getCache().get(key);
		if (pattern == null) {
			pattern = Pattern.compile(regex);
			document.getCache().put(key, pattern);
		}
		return pattern;
	}

	/**
	 * Replaces capturing groups with non-capturing groups in the given regular
	 * expression. As a side effect block quotes are replaced with normal quotes
	 * by using {@link #removeBlockQuotes(String)}.
	 * 
	 * @param pattern
	 * @return modified pattern
	 */
	public static String removeCapturingGroups(String pattern) {
		String newPattern = removeBlockQuotes(pattern);
		Matcher capturingGroupMatcher = CAPTURING_GROUP_PATTERN
				.matcher(newPattern);
		newPattern = capturingGroupMatcher.replaceAll("(?:");
		return newPattern;
	}
	
	/**
	 * Returns value if it is not null or default value if it is null.
	 * Automatically cast value to the same type as default value.
	 * @param value object
	 * @param defaultValue default value.
	 * @return object value or default value if object value is null
	 * @throws ClassCastException when value cannot be cast to default value type
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getParameter(Object value, T defaultValue) {
		T result;
		if (value != null) {
			result = (T)value;
		} else {
			result = defaultValue;
		}
		return result;
	}
	
	public static Map<String, Object> getEmptyParameterMap() {
		return Collections.emptyMap();
	}

}
