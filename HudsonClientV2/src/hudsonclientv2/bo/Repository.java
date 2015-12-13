package hudsonclientv2.bo;

import java.util.ArrayList;
import java.util.List;

/**
 * Define an object representation of a repository
 * 
 * @author libenzi
 *
 */
public class Repository {

	/**
	 * List of job for the repository
	 */
	private List<Job> jobs = new ArrayList<Job>();

	/**
	 * Name of the repository ant it URL
	 */
	private String repoName, rootURL;

	public Repository(List<Job> jobs, String repoName, String rootURL) {
		super();
		this.jobs = jobs;
		this.repoName = repoName;
		this.rootURL = rootURL;
	}

	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public String getRootURL() {
		return rootURL;
	}

	public void setRootURL(String rootURL) {
		this.rootURL = rootURL;
	}

}
