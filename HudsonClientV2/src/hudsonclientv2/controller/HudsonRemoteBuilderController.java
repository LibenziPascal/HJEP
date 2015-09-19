package hudsonclientv2.controller;

import hudsonclientv2.bo.HudsonPluginConstants;
import hudsonclientv2.bo.ResultsTestSurefire;
import hudsonclientv2.bo.ResultsTests;
import hudsonclientv2.communication.rest.RestConnectionUtils;
import hudsonclientv2.communication.rest.reader.ListingXMLReader;
import hudsonclientv2.handlers.ShellActivationTracker;
import hudsonclientv2.holders.JobHolder;
import hudsonclientv2.holders.MapCookie;
import hudsonclientv2.holders.MapHolder;
import hudsonclientv2.holders.SimpleUser;
import hudsonclientv2.utils.popups.SWTPopupUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

//TODO unit tests
public class HudsonRemoteBuilderController {

    private HudsonRemoteBuilderController() {
    }

    private static CookieForUrl getCookieAndUrlJob(String job) throws IOException {
	final SimpleUser simple = JobHolder.getJob(job).getUser();
	final List<String> cookies = MapCookie.getEntryNG(JobHolder.getJob(job).getUrlRepo(), simple.getUsername(), simple.getPassword());
	final String urlString = JobHolder.getJob(job).getUrlRepo().concat("/job/").concat(job);
	return new CookieForUrl(cookies, urlString);
    }
    
    private static CookieForUrl getCookieAndUrlJobForRepo(String repo) throws IOException {
	final SimpleUser simple = MapHolder.getEntry(repo);
	final List<String> cookies = MapCookie.getEntryNG(repo, simple.getUsername(), simple.getPassword());
	return new CookieForUrl(cookies, repo);
    }

    public static void build(final String job) throws IOException, ParserConfigurationException, SAXException {
	final CookieForUrl cookieForUrl = getCookieAndUrlJob(job);
	RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(), new URL(cookieForUrl.getUrl().concat("/build?delay=0")));
    }

    public static String console(final String job) throws IOException, ParserConfigurationException, SAXException {
	final CookieForUrl cookieForUrl = getCookieAndUrlJob(job);
	// retrieve last build number
	String lastBuildNumber = ListingXMLReader.lookForNodeValueInFlatXml("build", RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(),
	        new URL(cookieForUrl.getUrl().concat(HudsonPluginConstants.SUFFIX_API_XML))));
	// Send request to retrieve console log
	return RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(),
	        new URL(cookieForUrl.getUrl().concat(HudsonPluginConstants.SLASH + lastBuildNumber).concat("/console")));
    }

    public static String[] getIconsProperties(final String job) throws SAXException, IOException, ParserConfigurationException {
	// retrieve last build number
	final CookieForUrl cookieForUrl = getCookieAndUrlJob(job);
	String response = RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(),
	        new URL(cookieForUrl.getUrl().concat(HudsonPluginConstants.SUFFIX_API_XML)));
	String color = ListingXMLReader.lookForNodeValueInFlatXml("color", response);
	String iconUrl = ListingXMLReader.lookForNodeValueInFlatXml("iconUrl", response);
	return new String[] { color, iconUrl };
    }
    
    public static Map<String, String> getAllColors(final String job) throws SAXException, IOException, ParserConfigurationException {
    	// retrieve last build number
    	final CookieForUrl cookieForUrl = getCookieAndUrlJob(job);
    	String urlRepo = JobHolder.getJob(job).getUrlRepo();
		String response = RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(),
    	        new URL(urlRepo.concat(HudsonPluginConstants.SUFFIX_API_XML)));
    	return ListingXMLReader.getAllNodesInMap(response, "name", "color");
        }
    
    public static Map<String, String> getAllColorsForRepo(final String repoBaseUrl) throws SAXException, IOException, ParserConfigurationException {
    	// retrieve last build number
    	final CookieForUrl cookieForUrl = getCookieAndUrlJobForRepo(repoBaseUrl);
		String response = RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(),
    	        new URL(repoBaseUrl.concat(HudsonPluginConstants.SUFFIX_API_XML)));
    	return ListingXMLReader.getAllNodesInMap(response, "name", "color");
        }

    public static String getLastBuilURL(final String job) throws IOException, SAXException, ParserConfigurationException {
	final CookieForUrl cookieForUrl = getCookieAndUrlJob(job);
	String lastBuildNumber = ListingXMLReader.lookForNodeValueInFlatXml("build", RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(),
	        new URL(cookieForUrl.getUrl().concat(HudsonPluginConstants.SUFFIX_API_XML))));

	return cookieForUrl.getUrl().concat(HudsonPluginConstants.SLASH + lastBuildNumber);
    }

    public static List<ResultsTests> getTrend(final String job) throws IOException, SAXException, ParserConfigurationException {
	// TODO coverage could be a bonus
	final CookieForUrl cookieForUrl = getCookieAndUrlJob(job);
	// Retrieve all available build numbers
	Set<String> buildsNumbers = ListingXMLReader.lookForMultipleNodeValueInFlatXml("build", RestConnectionUtils.makeConnectionAndRead(
	        cookieForUrl.getCookies(), new URL(cookieForUrl.getUrl().concat(HudsonPluginConstants.SUFFIX_API_XML))));

	// Order builds
	final List<String> buildNumbersList = new ArrayList<>(buildsNumbers);
	Collections.sort(buildNumbersList);

	// If urlJob/buildNumber/testResults exists there is surefire tests
	final List<ResultsTests> resultsSurefire = new ArrayList<ResultsTests>();
	// Here is a read test from the url of testReport to know if testReport
	// exists on the project
	for (String buildNumber : buildsNumbers) {
	    try {
		String urlResultTests = cookieForUrl.getUrl().concat(HudsonPluginConstants.SLASH + buildNumber + HudsonPluginConstants.SUFFIX_API_XML);
		String testResultForBuild = RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(), new URL(urlResultTests));

		int fC = Integer.parseInt(ListingXMLReader.lookForNodeValueInFlatXml("failCount", testResultForBuild));
		int sC = Integer.parseInt(ListingXMLReader.lookForNodeValueInFlatXml("skipCount", testResultForBuild));
		int tC = 0;
		try {
		    tC = Integer.parseInt(ListingXMLReader.lookForNodeValueInFlatXml("totalCount", testResultForBuild));
		} catch (FileNotFoundException e) {
		    tC = Integer.parseInt(ListingXMLReader.lookForNodeValueInFlatXml("passCount", testResultForBuild)) + sC + fC;

		}
		resultsSurefire.add(new ResultsTestSurefire(Integer.parseInt(buildNumber), tC, fC, sC));
	    } catch (FileNotFoundException | NumberFormatException e) {
		resultsSurefire.add(new ResultsTestSurefire(Integer.parseInt(buildNumber),0,0,0));
		// Continue even if a build has no test results, it can be on
		// the next and previous
	    }
	}
	if (resultsSurefire.isEmpty()) {
	    SWTPopupUtils.showMessage(ShellActivationTracker.getActiveShell(), "No tests results are available from this job...");
	}
	return resultsSurefire;
    }

    private static class CookieForUrl {
	final List<String> cookies;
	final String url;

	public CookieForUrl(List<String> cookies, String url) {
	    super();
	    this.cookies = cookies;
	    this.url = url;
	}

	public List<String> getCookies() {
	    return cookies;
	}

	public String getUrl() {
	    return url;
	}
    }

    public static Set<String> getLastFails(String job) throws ProtocolException, MalformedURLException, SAXException, IOException, ParserConfigurationException {
	final CookieForUrl cookieForUrl = getCookieAndUrlJob(job);
	String lastBuildNumber = ListingXMLReader.lookForNodeValueInFlatXml("build", RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(),
	        new URL(cookieForUrl.getUrl().concat(HudsonPluginConstants.SUFFIX_API_XML))));

	String urlStringCurrentBuild = cookieForUrl.getUrl().concat(
	        HudsonPluginConstants.SLASH + lastBuildNumber + "/testReport" + HudsonPluginConstants.SUFFIX_API_XML);

	Set<String> failures = ListingXMLReader.lookForFailTests(RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(), new URL(
	        urlStringCurrentBuild)));

	return failures;
    }

}
