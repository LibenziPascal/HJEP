package hudsonclientv2.controller;

import hudsonclientv2.bo.HudsonPluginConstants;
import hudsonclientv2.communication.rest.CookieForUrl;
import hudsonclientv2.communication.rest.RestConnectionUtils;
import hudsonclientv2.communication.rest.reader.ListingXMLReader;
import hudsonclientv2.holders.JobHolder;
import hudsonclientv2.holders.MapCookie;
import hudsonclientv2.holders.MapHolder;
import hudsonclientv2.holders.SimpleUser;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

//TODO unit tests
public final class HudsonRemoteBuilderController {

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
		String response = RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(), new URL(urlRepo.concat(HudsonPluginConstants.SUFFIX_API_XML)));
		return ListingXMLReader.getAllNodesInMap(response, "name", "color");
	}

	public static Map<String, String> getAllColorsForRepo(final String repoBaseUrl) throws SAXException, IOException, ParserConfigurationException {
		// retrieve last build number
		final CookieForUrl cookieForUrl = getCookieAndUrlJobForRepo(repoBaseUrl);
		String response = RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(),
		        new URL(repoBaseUrl.concat(HudsonPluginConstants.SUFFIX_API_XML)));
		return ListingXMLReader.getAllNodesInMap(response, "name", "color");
	}

}
