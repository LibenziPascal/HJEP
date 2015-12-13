package hudsonclientv2.controller;

import hudsonclientv2.bo.HudsonPluginConstants;
import hudsonclientv2.bo.Job;
import hudsonclientv2.bo.ResultsTestSurefire;
import hudsonclientv2.bo.ResultsTests;
import hudsonclientv2.communication.rest.CookieForUrl;
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
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

//TODO unit tests
public final class HudsonRemoteBuildControllerNG {

	private HudsonRemoteBuildControllerNG() {
	}

	private static CookieForUrl getCookieAndUrlJob(Job job) throws IOException {
		Job jobFromConfiguration = JobHolder.getJob(job.getJobName());
		// Si le job n'existe pas c'est qu'il faut prendre la configuration du
		// repository
		SimpleUser simple;
		if (jobFromConfiguration == null) {
			simple = MapHolder.getEntry(job.getUrlRepo());
			jobFromConfiguration = job;
		} else {
			simple = jobFromConfiguration.getUser();
		}
		if (simple == null) {
			throw new IllegalArgumentException();
		}
		final List<String> cookies = MapCookie.getEntryNG(jobFromConfiguration.getUrlRepo(), simple.getUsername(), simple.getPassword());
		// TODO property in job: rootJobUrl
		final String urlString = jobFromConfiguration.getUrlRepo().concat("/job/").concat(job.getJobName());
		return new CookieForUrl(cookies, urlString);
	}

	public static void build(final Job job) throws IOException, ParserConfigurationException, SAXException {
		final CookieForUrl cookieForUrl = getCookieAndUrlJob(job);
		RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(), new URL(cookieForUrl.getUrl().concat("/build?delay=0")));
	}

	public static String console(final Job job) throws IOException, ParserConfigurationException, SAXException {
		final CookieForUrl cookieForUrl = getCookieAndUrlJob(job);
		// retrieve last build number
		String lastBuildNumber = ListingXMLReader.lookForNodeValueInFlatXml("build", RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(),
		        new URL(cookieForUrl.getUrl().concat(HudsonPluginConstants.SUFFIX_API_XML))));
		// Send request to retrieve console log
		return RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(),
		        new URL(cookieForUrl.getUrl().concat(HudsonPluginConstants.SLASH + lastBuildNumber).concat("/console")));
	}

	public static String getLastBuilURL(final Job job) throws IOException, SAXException, ParserConfigurationException {
		final CookieForUrl cookieForUrl = getCookieAndUrlJob(job);
		String lastBuildNumber = ListingXMLReader.lookForNodeValueInFlatXml("build", RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(),
		        new URL(cookieForUrl.getUrl().concat(HudsonPluginConstants.SUFFIX_API_XML))));

		return cookieForUrl.getUrl().concat(HudsonPluginConstants.SLASH + lastBuildNumber);
	}

	public static List<ResultsTests> getTrend(final Job job) throws IOException, SAXException, ParserConfigurationException {
		// TODO coverage could be a bonus
		final CookieForUrl cookieForUrl = getCookieAndUrlJob(job);
		// Retrieve all available build numbers
		List<String> buildsNumbers = ListingXMLReader.lookForMultipleNodeValueInFlatXml("build", RestConnectionUtils.makeConnectionAndRead(
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
				resultsSurefire.add(new ResultsTestSurefire(Integer.parseInt(buildNumber), 0, 0, 0));
				// Continue even if a build has no test results, it can be on
				// the next and previous
			}
		}
		if (resultsSurefire.isEmpty()) {
			SWTPopupUtils.showMessage(ShellActivationTracker.getActiveShell(), "No tests results are available from this job...");
		}
		return resultsSurefire;
	}

	public static Set<String> getLastFails(Job job) throws ProtocolException, MalformedURLException, SAXException, IOException, ParserConfigurationException {
		final CookieForUrl cookieForUrl = getCookieAndUrlJob(job);
		List<String> buildNumbers = ListingXMLReader.lookForMultipleNodeValueInFlatXml("build", RestConnectionUtils.makeConnectionAndRead(
		        cookieForUrl.getCookies(), new URL(cookieForUrl.getUrl().concat(HudsonPluginConstants.SUFFIX_API_XML))));

		int maxNb = 0;

		for (String bn : buildNumbers) {
			int currentBuildNb = Integer.parseInt(bn);
			if (currentBuildNb > maxNb) {
				maxNb = currentBuildNb;
			}
		}

		String urlStringCurrentBuild = cookieForUrl.getUrl().concat(HudsonPluginConstants.SLASH + maxNb + "/testReport" + HudsonPluginConstants.SUFFIX_API_XML);

		Set<String> failures = ListingXMLReader.lookForFailTests(RestConnectionUtils.makeConnectionAndRead(cookieForUrl.getCookies(), new URL(
		        urlStringCurrentBuild)));

		return failures;
	}

}
