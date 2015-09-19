package hudsonclientv2.communication.rest.reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//TODO sax read?
public class ListingXMLReader {
    public static List<String> lookForMultipleNodeValueInFlatXml(String nodeName, String content) throws ParserConfigurationException, SAXException, IOException {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	DocumentBuilder builder = factory.newDocumentBuilder();
	// Load the input XML document, parse it and return an instance of the
	// Document class.
	Document document = builder.parse(new ByteArrayInputStream(content.getBytes()));

	List<String> setValues = new ArrayList<String>();
	lookForNodeForBuildNGMultipleValues(nodeName, document.getDocumentElement().getParentNode(), setValues);
	return setValues;
    }

    private static List<String> lookForNodeForBuildNGMultipleValues(String nodeName, Node parentNode, List<String> current) {
	NodeList children = parentNode.getChildNodes();
	for (int i = 0; i < children.getLength(); i++) {
	    Node node = children.item(i);

	    if (node.getNodeName().equalsIgnoreCase(nodeName) || node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.ELEMENT_NODE) {
		if (node.getNodeName().equalsIgnoreCase(nodeName)) {
		    if (node.getFirstChild() != null) {
			current.add(node.getFirstChild().getTextContent());
		    } else {
			current.add(node.getTextContent());
		    }
		}
	    }
	    if (node.hasChildNodes() && !nodeName.equalsIgnoreCase(node.getNodeName()) && node.getNodeType() != Node.TEXT_NODE) {
		lookForNodeForBuildNGMultipleValues(nodeName, node, current);
	    }

	}
	return current;
    }

    public Map<String, String> getNodeJobOrView(String nodeName, String content) throws ParserConfigurationException, SAXException, IOException {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	DocumentBuilder builder = factory.newDocumentBuilder();
	Map<String, String> jobNamesAndUrl = new HashMap<>();
	// Load the input XML document, parse it and return an instance of the
	// Document class.
	Document document = builder.parse(new ByteArrayInputStream(content.getBytes()));

	NodeList nodeList = document.getDocumentElement().getChildNodes();

	for (int i = 0; i < nodeList.getLength(); i++) {

	    Node node = nodeList.item(i);

	    if (node.getNodeType() == Node.ELEMENT_NODE) {
		if (node.getNodeName().equalsIgnoreCase(nodeName)) {
		    jobNamesAndUrl.put(node.getFirstChild().getTextContent(), node.getChildNodes().item(1).getTextContent());
		}
	    }

	}
	return jobNamesAndUrl;
    }

    public String lookForNodeForBuild(String nodeName, Node n) throws ParserConfigurationException, SAXException, IOException {
	NodeList children = n.getChildNodes();
	String current = "";
	for (int i = 0; i < children.getLength(); i++) {

	    Node node = children.item(i);
	    // System.out.println(node.getNodeName());
	    if (node.hasChildNodes() && !nodeName.equalsIgnoreCase(node.getNodeName()) && node.getNodeType() != Node.TEXT_NODE) {
		current = (lookForNodeForBuild(nodeName, node));
		if (!current.equals("")) {
		}
	    }

	    if (node.getNodeName().equalsIgnoreCase(nodeName) || node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.ELEMENT_NODE) {
		if (node.getNodeName().equalsIgnoreCase(nodeName)) {

		    if (node.getFirstChild() != null) {
			return node.getFirstChild().getTextContent();
		    } else {
			return node.getTextContent();
		    }
		}
	    }
	}
	return current;
    }

    public static String lookForNodeValueInFlatXml(String nodeName, String content) throws SAXException, IOException, ParserConfigurationException {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	DocumentBuilder builder = factory.newDocumentBuilder();
	// Load the input XML document, parse it and return an instance of the
	// Document class.
	Document document = builder.parse(new ByteArrayInputStream(content.getBytes()));

	return lookForNodeForBuildNG(nodeName, document.getDocumentElement().getParentNode());
    }

    public static String lookForNodeForBuildNG(String nodeName, Node n) throws ParserConfigurationException, SAXException, IOException {
	NodeList children = n.getChildNodes();
	String current = "";
	for (int i = 0; i < children.getLength(); i++) {
	    if (!current.equals("")) {
		break;
	    }
	    Node node = children.item(i);

	    if (node.getNodeName().equalsIgnoreCase(nodeName) || node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.ELEMENT_NODE) {
		if (node.getNodeName().equalsIgnoreCase(nodeName)) {
		    if (node.getFirstChild() != null) {
			current = node.getFirstChild().getTextContent();
		    } else {
			current = node.getTextContent();
		    }
		}
	    }
	    if (current != null && !current.equals("")) {
		return current;
	    }
	    if (node.hasChildNodes() && !nodeName.equalsIgnoreCase(node.getNodeName()) && node.getNodeType() != Node.TEXT_NODE) {
		current = (lookForNodeForBuildNG(nodeName, node));
	    }

	}
	return current;
    }

    public static Set<String> lookForFailTests(String content) throws ParserConfigurationException, SAXException, IOException {
	/*
<testResult><duration>465.34802</duration><empty>false</empty><failCount>4</failCount><passCount>113</passCount><skipCount>0</skipCount><suite><case><age>0</age><className>com.lodh.arte.stats.FunctionCallDTOTest</className><duration>0.104</duration><failedSince>0</failedSince><name>testXML</name><skipped>false</skipped><status>PASSED</status></case><duration>0.104</duration><name>com.lodh.arte.stats.FunctionCallDTOTest</name></suite><suite><case><age>0</age><className>com.lodh.arte.stats.StatsServicePOATest</className><duration>0.139</duration><failedSince>0</failedSince><name>testTestPokinmon</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.StatsServicePOATest</className><duration>0.0</duration><failedSince>0</failedSince><name>testReportFunctionCallAsync</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.StatsServicePOATest</className><duration>0.0</duration><failedSince>0</failedSince><name>testReportFunctionCall</name><skipped>false</skipped><status>PASSED</status></case><duration>0.139</duration><name>com.lodh.arte.stats.StatsServicePOATest</name></suite><suite><case><age>0</age><className>com.lodh.arte.stats.StatsServiceTest</className><duration>0.393</duration><failedSince>0</failedSince><name>testReportCalls</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.StatsServiceTest</className><duration>0.588</duration><failedSince>0</failedSince><name>test</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.StatsServiceTest</className><duration>0.007</duration><failedSince>0</failedSince><name>testNoEntity</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.StatsServiceTest</className><duration>0.059</duration><failedSince>0</failedSince><name>testPokinmon</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.StatsServiceTest</className><duration>0.063</duration><failedSince>0</failedSince><name>testInvalidParam</name><skipped>false</skipped><status>PASSED</status></case><duration>1.11</duration><name>com.lodh.arte.stats.StatsServiceTest</name></suite><suite><case><age>0</age><className>com.lodh.arte.stats.StdDeviationTest</className><duration>0.263</duration><failedSince>0</failedSince><name>test</name><skipped>false</skipped><status>PASSED</status></case><duration>0.263</duration><name>com.lodh.arte.stats.StdDeviationTest</name></suite><suite><case><age>0</age><className>com.lodh.arte.stats.da.CallsContextCacheTest</className><duration>0.095</duration><failedSince>0</failedSince><name>fiftyUsersTwentyContextsTest</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.da.CallsContextCacheTest</className><duration>0.0</duration><failedSince>0</failedSince><name>twoUsersTwoContextsTest</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.da.CallsContextCacheTest</className><duration>0.0</duration><failedSince>0</failedSince><name>cacheDoesntContainsTest</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.da.CallsContextCacheTest</className><duration>0.067</duration><failedSince>0</failedSince><name>sameContextUsingEntityIdsTest</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.da.CallsContextCacheTest</className><duration>0.0</duration><failedSince>0</failedSince><name>testContextEquals</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.da.CallsContextCacheTest</className><duration>0.0</duration><failedSince>0</failedSince><name>twoContextsUsingEntityIdsTest</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.da.CallsContextCacheTest</className><duration>0.0</duration><failedSince>0</failedSince><name>cacheContainsEntityIdTest</name><skipped>false</skipped><status>PASSED</status></case><duration>0.162</duration><name>com.lodh.arte.stats.da.CallsContextCacheTest</name></suite><suite><case><age>0</age><className>com.lodh.arte.stats.da.StasOracleDAOTest</className><duration>0.407</duration><failedSince>0</failedSince><name>testNoAppFunc</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.da.StasOracleDAOTest</className><duration>0.005</duration><failedSince>0</failedSince><name>addDay2016Feb29Test</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.da.StasOracleDAOTest</className><duration>0.125</duration><failedSince>0</failedSince><name>truncateTest</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.da.StasOracleDAOTest</className><duration>0.0</duration><failedSince>0</failedSince><name>addDay2014Jan31Test</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.da.StasOracleDAOTest</className><duration>0.0</duration><failedSince>0</failedSince><name>oracleDateFormatTest</name><skipped>false</skipped><status>PASSED</status></case><duration>0.537</duration><name>com.lodh.arte.stats.da.StasOracleDAOTest</name></suite><suite><case><age>0</age><className>com.lodh.arte.stats.da.StatsAbstractDAOTest</className><duration>0.0</duration><failedSince>0</failedSince><name>stringBytesLengthTest1</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.da.StatsAbstractDAOTest</className><duration>0.002</duration><failedSince>0</failedSince><name>stringBytesLengthTest2</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.da.StatsAbstractDAOTest</className><duration>0.0</duration><failedSince>0</failedSince><name>truncateTest1</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.da.StatsAbstractDAOTest</className><duration>0.0</duration><failedSince>0</failedSince><name>truncateTest2</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.da.StatsAbstractDAOTest</className><duration>0.0</duration><failedSince>0</failedSince><name>truncateTest3</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.da.StatsAbstractDAOTest</className><duration>0.0</duration><failedSince>0</failedSince><name>truncateTest4</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.da.StatsAbstractDAOTest</className><duration>0.0</duration><failedSince>0</failedSince><name>truncateTest5</name><skipped>false</skipped><status>PASSED</status></case><duration>0.002</duration><name>com.lodh.arte.stats.da.StatsAbstractDAOTest</name></suite><suite><case><age>0</age><className>com.lodh.arte.stats.ejb.entity.EntityTest</className><duration>0.0</duration><failedSince>0</failedSince><name>testFunctionCallCounterEntityPK</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.ejb.entity.EntityTest</className><duration>0.0</duration><failedSince>0</failedSince><name>testAggregatedHourEntity</name><skipped>false</skipped><status>PASSED</status></case><duration>0.0</duration><name>com.lodh.arte.stats.ejb.entity.EntityTest</name></suite><suite><case><age>0</age><className>com.lodh.arte.stats.util.DateConverterTest</className><duration>0.0</duration><failedSince>0</failedSince><name>testConvertDate</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.util.DateConverterTest</className><duration>0.0</duration><failedSince>0</failedSince><name>testToString</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.util.DateConverterTest</className><duration>0.0</duration><failedSince>0</failedSince><name>testToDate</name><skipped>false</skipped><status>PASSED</status></case><duration>0.0</duration><name>com.lodh.arte.stats.util.DateConverterTest</name></suite><suite><case><age>0</age><className>com.lodh.arte.stats.util.ServerInfoArrayXmlizerTest</className><duration>0.0</duration><failedSince>0</failedSince><name>testToXML</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.util.ServerInfoArrayXmlizerTest</className><duration>0.0</duration><failedSince>0</failedSince><name>testFromXML</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.util.ServerInfoArrayXmlizerTest</className><duration>0.0</duration><failedSince>0</failedSince><name>testToXMLNull</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.util.ServerInfoArrayXmlizerTest</className><duration>0.0</duration><failedSince>0</failedSince><name>testFromXMLNull</name><skipped>false</skipped><status>PASSED</status></case><duration>0.0</duration><name>com.lodh.arte.stats.util.ServerInfoArrayXmlizerTest</name></suite><suite><case><age>0</age><className>com.lodh.arte.stats.StatsSystemExceptionTest</className><duration>0.0</duration><failedSince>0</failedSince><name>testStatsSystemExceptionStringThrowable</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>com.lodh.arte.stats.StatsSystemExceptionTest</className><duration>0.0</duration><failedSince>0</failedSince><name>testStatsSystemExceptionString</name><skipped>false</skipped><status>PASSED</status></case><duration>0.0</duration><name>com.lodh.arte.stats.StatsSystemExceptionTest</name></suite><sui...*/
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	DocumentBuilder builder = factory.newDocumentBuilder();
	// Load the input XML document, parse it and return an instance of the
	// Document class.
	Document document = builder.parse(new ByteArrayInputStream(content.getBytes()));
	return lookForNodeForNodeTest(document, new HashSet<String>());
    }

    private static Set<String> lookForNodeForNodeTest(Node parentNode, Set<String> current) {
	NodeList children = parentNode.getChildNodes();
	for (int i = 0; i < children.getLength(); i++) {
	    Node node = children.item(i);

	    if (node.getNodeName().equalsIgnoreCase("case") || node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.ELEMENT_NODE) {
		if (node.getNodeName().equalsIgnoreCase("case")) {
		    NodeList childNodes = node.getChildNodes();

		    String className = "";
		    String name = "";
		    for (int j = 0; j < childNodes.getLength(); j++) {
			if ("className".equals(childNodes.item(j).getNodeName())) {
			    className = childNodes.item(j).getTextContent();
			}
			if ("name".equals(childNodes.item(j).getNodeName())) {
			    name = childNodes.item(j).getTextContent();
			}
		    }

		    for (int j = 0; j < childNodes.getLength(); j++) {
			if ("status".equals(childNodes.item(j).getNodeName()) && ("REGRESSION".equals(childNodes.item(j).getTextContent())||"FAILED".equals(childNodes.item(j).getTextContent()))) {
			    current.add(className + "." + name);
			}
		    }

		}
	    }
	    if (node.hasChildNodes() && !"case".equalsIgnoreCase(node.getNodeName()) && node.getNodeType() != Node.TEXT_NODE) {
		lookForNodeForNodeTest(node, current);
	    }

	}
	return current;
    }

    // public static void main(String[] args) throws
    // ParserConfigurationException, SAXException, IOException {
    // String content =
    // "<testResult><suite><case><age>1</age><className>DBQueryTest</className><duration>0.0</duration><errorDetails>Unknown C++ exception thrown in the test body.</errorDetails><errorStackTrace>unknown file Unknown C++ exception thrown in the test body.</errorStackTrace><failedSince>4</failedSince><name>deleteAll</name><skipped>false</skipped><status>FAILED</status></case><case><age>1</age><className>DBQueryTest</className><duration>0.0</duration><errorDetails>Unknown C++ exception thrown in the test body.</errorDetails><errorStackTrace>unknown file Unknown C++ exception thrown in the test body.</errorStackTrace><failedSince>4</failedSince><name>insertAll</name><skipped>false</skipped><status>FAILED</status></case><case><age>1</age><className>DBQueryTest</className><duration>0.0</duration><errorDetails>Unknown C++ exception thrown in the test body.</errorDetails><errorStackTrace>unknown file Unknown C++ exception thrown in the test body.</errorStackTrace><failedSince>4</failedSince><name>deleteAll</name><skipped>false</skipped><status>FAILED</status></case><case><age>1</age><className>DBQueryTest</className><duration>0.001</duration><errorDetails>Unknown C++ exception thrown in the test body.</errorDetails><errorStackTrace>unknown file Unknown C++ exception thrown in the test body.</errorStackTrace><failedSince>4</failedSince><name>insertAll</name><skipped>false</skipped><status>FAILED</status></case><duration>0.613</duration><name>DBQueryTest</name></suite><suite><case><age>0</age><className>DBSelectorTest</className><duration>0.007</duration><failedSince>0</failedSince><name>SelectTitles</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>DBSelectorTest</className><duration>0.003</duration><failedSince>0</failedSince><name>SelectSalesDetail</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>DBSelectorTest</className><duration>0.003</duration><failedSince>0</failedSince><name>SelectSales</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>DBSelectorTest</className><duration>0.004</duration><failedSince>0</failedSince><name>SelectBlurbs</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>DBSelectorTest</className><duration>0.004</duration><failedSince>0</failedSince><name>SelectPublishers</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>DBSelectorTest</className><duration>0.005</duration><failedSince>0</failedSince><name>SelectAuthors</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>DBSelectorTest</className><duration>0.004</duration><failedSince>0</failedSince><name>SelectIssue270</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>DBSelectorTest</className><duration>0.003</duration><failedSince>0</failedSince><name>SelectWithJoin</name><skipped>false</skipped><status>PASSED</status></case><case><age>0</age><className>DBSelectorTest</className><duration>0.002</duration><failedSince>0</failedSince><name>SelectView</name><skipped>false</skipped><status>PASSED</status></case><case><age>1</age><className>DBSelectorTest</className><duration>0.001</duration><errorDetails>Unknown C++ exception thrown in the test body.</errorDetails><errorStackTrace>unknown file Unknown C++ exception thrown in the test body.</errorStackTrace><failedSince>4</failedSince><name>selectEmployee</name><skipped>false</skipped><status>FAILED</status></case><case><age>1</age><className>DBSelectorTest</className><duration>0.0</duration><errorDetails>Unknown C++ exception thrown in the test body.</errorDetails><errorStackTrace>unknown file Unknown C++ exception thrown in the test body.</errorStackTrace><failedSince>4</failedSince><name>selectDynamicEmployee</name><skipped>false</skipped><status>FAILED</status></case><case><age>1</age><className>DBSelectorTest</className><duration>0.0</duration><errorDetails>Unknown C++ exception thrown in the test body.</errorDetails><errorStackTrace>unknown file Unknown C++ exception thrown in the test body.</errorStackTrace><failedSince>4</failedSince><name>selectAll_DBElements</name><skipped>false</skipped><status>FAILED</status></case><case><age>1</age><className>DBSelectorTest</className><duration>0.0</duration><errorDetails>Unknown C++ exception thrown in the test body.</errorDetails><errorStackTrace>unknown file Unknown C++ exception thrown in the test body.</errorStackTrace><failedSince>4</failedSince><name>selectAll_CString</name><skipped>false</skipped><status>FAILED</status></case><case><age>1</age><className>DBSelectorTest</className><duration>0.0</duration><errorDetails>Unknown C++ exception thrown in the test body.</errorDetails><errorStackTrace>unknown file Unknown C++ exception thrown in the test body.</errorStackTrace><failedSince>4</failedSince><name>selectAllBind</name><skipped>false</skipped><status>FAILED</status></case><case><age>1</age><className>DBSelectorTest</className><duration>0.0</duration><errorDetails>Unknown C++ exception thrown in the test body.</errorDetails><errorStackTrace>unknown file Unknown C++ exception thrown in the test body.</errorStackTrace><failedSince>4</failedSince><name>selectEmployee</name><skipped>false</skipped><status>FAILED</status></case><case><age>1</age><className>DBSelectorTest</className><duration>0.0</duration><errorDetails>Unknown C++ exception thrown in the test body.</errorDetails><errorStackTrace>unknown file Unknown C++ exception thrown in the test body.</errorStackTrace><failedSince>4</failedSince><name>selectDynamicEmployee</name><skipped>false</skipped><status>FAILED</status></case><case><age>1</age><className>DBSelectorTest</className><duration>0.001</duration><errorDetails>Unknown C++ exception thrown in the test body.</errorDetails><errorStackTrace>unknown file Unknown C++ exception thrown in the test body.</errorStackTrace><failedSince>4</failedSince><name>selectAll_DBElements</name><skipped>false</skipped><status>FAILED</status></case><case><age>1</age><className>DBSelectorTest</className><duration>0.0</duration><errorDetails>Unknown C++ exception thrown in the test body.</errorDetails><errorStackTrace>unknown file Unknown C++ exception thrown in the test body.</errorStackTrace><failedSince>4</failedSince><name>selectAll_CString</name><skipped>false</skipped><status>FAILED</status></case><case><age>1</age><className>DBSelectorTest</className><duration>0.0</duration><errorDetails>Unknown C++ exception thrown in the test body.</errorDetails><errorStackTrace>unknown file Unknown C++ exception thrown in the test body.</errorStackTrace><failedSince>4</failedSince><name>selectAllBind</name><skipped>false</skipped><status>FAILED</status></case><duration>0.036999997</duration><name>DBSelectorTest</name></suite><suite><case><age>1</age><className>DBStoredProcTest</className><duration>0.438</duration><errorDetails>Value of: m_resultat Actual: \"1.2;1;1.2000000000;20150201;01.02.2015;1.2;312e32;\" Expected: Resultats_Attendus[i] Which is: \"1.2;1;1.2000000000;20140201;01.02.2014;1.2;312e32;\"</errorDetails><errorStackTrace>.DBStoredProcTest.cpp:235 Value of: m_resultat Actual: \"1.2;1;1.2000000000;20150201;01.02.2015;1.2;312e32;\" Expected: Resultats_Attendus[i] Which is: \"1.2;1;1.2000000000;20140201;01.02.2014;1.2;312e32;\"</errorStackTrace><failedSince>4</failedSince><name>testStoredProcedures</name><skipped>false</skipped><status>FAILED</status></case><case><age>1</age><className>DBStoredProcTest</className><duration>0.001</duration><errorDetails>Unknown C++ exception thrown in the test body.</errorDetails><errorStackTrace>unknown file Unknown C++ exception thrown in the test body.</errorStackTrace><failedSince>4</failedSince><name>testProcedures</name><skipped>false</skipped><status>FAILED</status></case></suite></testResult>";
    // Set<String> lookForFailTests = lookForFailTests(content);
    // // System.out.println(lookForFailTests);
    // content =
    // "<allView><job><name>hibernate-validator</name><url>http://localhost:8080/job/hibernate-validator/</url><color>red</color></job><job><name>hibernatesearchsnapshot</name><url>http://localhost:8080/job/hibernatesearchsnapshot/</url><color>yellow_anime</color></job><name>All</name><url>http://localhost:8080/</url></allView>";
    // Map<String, String> allNodesInMap = getAllNodesInMap(content, "name",
    // "color");
    // System.out.println(allNodesInMap);
    // }

    public static Map<String, String> getAllNodesInMap(String content, String key, String props) throws ParserConfigurationException, SAXException, IOException {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	DocumentBuilder builder = factory.newDocumentBuilder();
	// Load the input XML document, parse it and return an instance of the
	// Document class.
	Document document = builder.parse(new ByteArrayInputStream(content.getBytes()));

	HashMap<String, String> mapsOfPropByKey = new HashMap<String, String>();
	return lookForNodeForNodesPropertiesAtSameLevel(document, key, props, mapsOfPropByKey);
    }

    private static Map<String, String> lookForNodeForNodesPropertiesAtSameLevel(Node parentNode, String key, String value, Map<String, String> mapValues) {
	NodeList children = parentNode.getChildNodes();
	for (int i = 0; i < children.getLength(); i++) {
	    Node node = children.item(i);

	    if (node.getNodeName().equalsIgnoreCase(key) || node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.ELEMENT_NODE) {
		if (node.getNodeName().equalsIgnoreCase(key)) {
		    NodeList brothersNodes = node.getParentNode().getChildNodes();
		    // System.out.println("here");
		    String keyFound = node.getTextContent();
		    String valueFound = "";
		    for (int j = 0; j < brothersNodes.getLength(); j++) {
			if (value.equals(brothersNodes.item(j).getNodeName())) {
			    valueFound = brothersNodes.item(j).getTextContent();
			}
		    }

		    if (!keyFound.isEmpty() && !valueFound.isEmpty()) {
			mapValues.put(keyFound, valueFound);
		    }

		}
		if (node.hasChildNodes() && !"case".equalsIgnoreCase(node.getNodeName()) && node.getNodeType() != Node.TEXT_NODE) {
		    lookForNodeForNodesPropertiesAtSameLevel(node, key, value, mapValues);
		}

	    }
	}
	return mapValues;
    }
}
