package hudsonclientv2.controller;

import hudsonclientv2.Activator;
import hudsonclientv2.bo.HudsonPluginConstants;
import hudsonclientv2.communication.rest.RestConnectionUtils;
import hudsonclientv2.communication.rest.reader.ListingXMLReader;
import hudsonclientv2.holders.JobHolder;
import hudsonclientv2.holders.MapCookie;
import hudsonclientv2.holders.MapHolder;
import hudsonclientv2.utils.logging.HudsonPluginLogger;
import hudsonclientv2.utils.popups.DetailsJobPopup;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xml.sax.SAXException;

public class HudsonRemoteDescriptionController implements IButtonController {

    private Text urlRemoteRepository, username, password;
    private org.eclipse.swt.widgets.List searchType;
    private org.eclipse.swt.widgets.List resultList;

    private String currentCase;
    private List<String> jobNamesList;
    private Map<String, String> jobs;

    public HudsonRemoteDescriptionController(Text urlRepositoryTF, org.eclipse.swt.widgets.List filterObjectTypeList, Text usernameTF, Text pwdTF,
	    org.eclipse.swt.widgets.List resultList2) {
	super();
	this.urlRemoteRepository = urlRepositoryTF;
	this.searchType = filterObjectTypeList;
	this.username = usernameTF;
	this.password = pwdTF;
	this.resultList = resultList2;
    }

    @Override
    public void doControl() throws IOException {
	// MapCookie
	List<String> cookies = MapCookie.getEntryNG(urlRemoteRepository.getText(), username.getText(), password.getText());
	find(urlRemoteRepository.getText(), cookies, searchType.getSelection()[0]);

	// If success we keep configuration
	MapHolder.putEntry(urlRemoteRepository.getText(), username.getText(), password.getText());
    }

    private void find(String urlRemoteRepository2, List<String> cookies, String search) {
	try {

	    final URL url = new URL(urlRemoteRepository2.concat(HudsonPluginConstants.SUFFIX_API_XML));
	    final String content = RestConnectionUtils.makeConnectionAndRead(cookies, url);

	    switch (search) {
	    case "Views":
		currentCase = "View";
		Map<String, String> view = new ListingXMLReader().getNodeJobOrView("View", content);
		Set<String> viewNames = view.keySet();
		List<String> viewNamesList = new ArrayList<String>(viewNames);
		Collections.sort(viewNamesList);
		resultList.removeAll();
		viewNamesList.stream().forEach(vName -> resultList.add(vName));
		break;
	    case "Jobs":
	    default:
		currentCase = "Job";
		jobs = new ListingXMLReader().getNodeJobOrView("Job", content);
		Set<String> jobNames = jobs.keySet();
		jobNamesList = new ArrayList<String>(jobNames);
		Collections.sort(jobNamesList);
		resultList.removeAll();
		jobNamesList.stream().forEach(jnl -> resultList.add(jnl));
		break;
	    }

	} catch (Exception e0) {
	    e0.printStackTrace();
	}
    }

    public void seeCurrentDetails(Shell parentShell) throws IOException, ParserConfigurationException, SAXException {
	List<String> cookies = MapCookie.getEntryNG(urlRemoteRepository.getText(), username.getText(), password.getText());
	URL url = new URL(urlRemoteRepository.getText());
	if(!currentCase.equals("View")) {
	    url = new URL(jobs.get(resultList.getSelection()[0]).concat(HudsonPluginConstants.SUFFIX_API_XML));
	} else {
	    url = new URL(urlRemoteRepository.getText().concat("/view/").concat(resultList.getSelection()[0]).concat(HudsonPluginConstants.SUFFIX_API_XML));
	}
	DetailsJobPopup.showPopupForJobDetails(parentShell, RestConnectionUtils.makeConnectionAndRead(cookies, url), currentCase.equals("View"));
    }

    public void addCurrentSelectedJobs() throws ProtocolException, SAXException, IOException, ParserConfigurationException {
	//TODO multiselection?
	// If we're in case "View" then we must save all jobs in this views
	// For the view with job views (which will be a part of HudsonCLient
	// later, we'll llokin for the views by an action from the user)
	if (!("View").equals(currentCase)) {
	    List<String> cookies = MapCookie.getEntryNG(urlRemoteRepository.getText(), username.getText(), password.getText());
	    URL url = new URL(jobs.get(resultList.getSelection()[0]).concat(HudsonPluginConstants.SUFFIX_API_XML));
	    String description = ListingXMLReader.lookForNodeValueInFlatXml("description", RestConnectionUtils.makeConnectionAndRead(cookies, url));
	    JobHolder.putEntry(urlRemoteRepository.getText(), resultList.getSelection()[0], description, username.getText(), password.getText());
	} else {

	    final List<String> cookies = MapCookie.getEntryNG(urlRemoteRepository.getText(), username.getText(), password.getText());
	    final String rootView = urlRemoteRepository.getText().concat("/view/").concat(resultList.getSelection()[0]);
	    // While we have views contained in the xml result, we must scan the
	    // subViews
	    final HashSet<String> collectedJobsRootUrls = new HashSet<String>();
	    scanViews(collectedJobsRootUrls, cookies, rootView);

	    for (String collectedJobRootUrl : collectedJobsRootUrls) {
		//TODOTEST : Job description multiline
		String description = ListingXMLReader.lookForNodeValueInFlatXml("description", RestConnectionUtils.makeConnectionAndRead(cookies, new URL(collectedJobRootUrl.concat(HudsonPluginConstants.SUFFIX_API_XML))));
		description = description.replaceAll("\\r\\n", "");
		description = description.replaceAll("\\n", "");
		final String[] splittedUrl = collectedJobRootUrl.split("/");
		JobHolder.putEntry(urlRemoteRepository.getText(), splittedUrl[splittedUrl.length-1], description, username.getText(), password.getText());
	    }
	}
    }
    
    //TODO refactor move static methods
    public static void findNestedViewsOneLvl(List<String> collectedViewsNames, List<String> cookies, String url) throws ParserConfigurationException, SAXException, IOException {
	final String contentViewXml = RestConnectionUtils.makeConnectionAndRead(cookies, new URL(url.concat(HudsonPluginConstants.SUFFIX_API_XML).replaceAll(" ", "%20")));
	final Set<String> nestedViewsNodes = new HashSet<>(ListingXMLReader.lookForMultipleNodeValueInFlatXml("nestedView", contentViewXml));
	boolean hasNestedViews = nestedViewsNodes != null && !nestedViewsNodes.isEmpty();
	final List<String> objectsNames = ListingXMLReader.lookForMultipleNodeValueInFlatXml("name", RestConnectionUtils.makeConnectionAndRead(cookies, new URL(url.concat(HudsonPluginConstants.SUFFIX_API_XML).replaceAll(" ", "%20"))));
	int numberOfFoundViewName = 0;
	String currentViewName = url.split("/")[url.split("/").length-1];
	if (hasNestedViews) {
	    for (final String nestedViewName : objectsNames) {
		if(nestedViewName.equals(currentViewName)) {
		    numberOfFoundViewName ++;
		}
		collectedViewsNames.add(nestedViewName);
	    }
	    //If we found the name of the current view just once, no nested view contains it
	    if(numberOfFoundViewName<=1) {
		collectedViewsNames.remove(currentViewName);
	    }
	}
    }

    /**
     * Scan views and subViews. Goal is to retrieve all jobs, with no link to views for the moment.
     * @param collectedUrls
     * @param cookies
     * @param url
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static void scanViews(Set<String> collectedUrls, List<String> cookies, String url) throws ParserConfigurationException, SAXException, IOException {
	final String contentViewXml = RestConnectionUtils.makeConnectionAndRead(cookies, new URL(url.concat(HudsonPluginConstants.SUFFIX_API_XML).replaceAll(" ", "%20")));
	final Set<String> nestedViewsNodes = new HashSet<>(ListingXMLReader.lookForMultipleNodeValueInFlatXml("nestedView", contentViewXml));
	boolean hasNestedViews = nestedViewsNodes != null && !nestedViewsNodes.isEmpty();
	final Set<String> objectsNames = new HashSet<>(ListingXMLReader.lookForMultipleNodeValueInFlatXml("name", RestConnectionUtils.makeConnectionAndRead(cookies, new URL(url.concat(HudsonPluginConstants.SUFFIX_API_XML).replaceAll(" ", "%20")))));
	if (hasNestedViews) {
	    for (final String nestedViewName : objectsNames) {
		if(!nestedViewName.equals(url.substring(url.lastIndexOf("/")+1))) {
        		HudsonPluginLogger.logInfo(Activator.PLUGIN_ID, "intern view detected: " + nestedViewName);
        		scanViews(collectedUrls, cookies, url.concat("/view/").concat(nestedViewName));
		}
	    }
	} else {
	    for (final String jobName : objectsNames) {
		HudsonPluginLogger.logInfo(Activator.PLUGIN_ID, "job detected: " + jobName);
		collectedUrls.add(url.concat("/job/").concat(jobName));
	    }
	}
    }

}
