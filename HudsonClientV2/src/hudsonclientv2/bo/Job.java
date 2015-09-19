package hudsonclientv2.bo;

import hudsonclientv2.holders.SimpleUser;

import java.util.ArrayList;
import java.util.List;

/**
 * The Jenkins Job object representation
 * 
 * @author libenzi
 *
 */
public class Job {

	/**
	 * Job name and description
	 */
	private String jobName, jobDescription;

	/**
	 * Builds in Job
	 */
	private List<JobBuild> builds = new ArrayList<JobBuild>();

	/**
	 * Root URL job
	 */
	private String urlRepo;

	/**
	 * User to use to connect to the root url of job
	 */
	private SimpleUser user;

	/**
	 * Simple initializing constructor
	 * @param urlRepo
	 * @param jobName
	 * @param jobDescription
	 * @param simpleUser
	 */
	public Job(String urlRepo, String jobName, String jobDescription, SimpleUser simpleUser) {
		this.user = simpleUser;
		this.jobName = jobName;
		this.jobDescription = jobDescription;
		this.urlRepo = urlRepo;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getJobDescription() {
		return jobDescription;
	}

	public void setJobDescription(String jobDescription) {
		this.jobDescription = jobDescription;
	}

	public List<JobBuild> getBuilds() {
		return builds;
	}

	public void setBuilds(List<JobBuild> builds) {
		this.builds = builds;
	}

	public void setUrlRepo(String urlRepo) {
		this.urlRepo = urlRepo;
	}

	public String getUrlRepo() {
		return urlRepo;
	}

	public SimpleUser getUser() {
		return user;
	}

	public void setUser(SimpleUser user) {
		this.user = user;
	}
}
