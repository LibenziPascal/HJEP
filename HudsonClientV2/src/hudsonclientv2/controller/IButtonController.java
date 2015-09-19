package hudsonclientv2.controller;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public interface IButtonController {
	void doControl() throws IOException, ParserConfigurationException, SAXException;
}
