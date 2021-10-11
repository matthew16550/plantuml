package net.sourceforge.plantuml.xml;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

public class XmlFactories {

	private XmlFactories() {
	}

	// This class uses the "initialization-on-demand holder" idiom to provide thread-safe
	// lazy initialization of expensive factories.
	// (see https://stackoverflow.com/a/8297830/1848731)

	private static class DocumentBuilderFactoryHolder {
		static final DocumentBuilderFactory INSTANCE;

		static {
			INSTANCE = DocumentBuilderFactory.newInstance();
			
			// Prevent XML security problems
			// See https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java
			setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			setFeature("http://xml.org/sax/features/external-general-entities", false);
			setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			INSTANCE.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			INSTANCE.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			INSTANCE.setXIncludeAware(false);
			INSTANCE.setExpandEntityReferences(false);
		}

		private static void setFeature(String name, boolean value) {
			try {
				INSTANCE.setFeature(name, value);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
	}

	private static class TransformerFactoryHolder {
		static final TransformerFactory INSTANCE;

		static {
			INSTANCE = TransformerFactory.newInstance();

			// Prevent XML security problems
			// See https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java
			INSTANCE.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			INSTANCE.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		}
	}

	public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		return DocumentBuilderFactoryHolder.INSTANCE.newDocumentBuilder();
	}

	public static Transformer newTransformer() throws TransformerConfigurationException {
		return TransformerFactoryHolder.INSTANCE.newTransformer();
	}
}
