package hudsonclientv2.holders;

import hudsonclientv2.bo.Job;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.swt.widgets.TableItem;

public class JobHolder {
    private JobHolder() {
    }

    private final static Map<String, SimpleUser> USER_BY_JOB = new HashMap<String, SimpleUser>();

    private final static Set<Job> jobsList = new HashSet<Job>();

    public static void putEntry(String urlRepo, String jobName, String description, String username, String password) {
	USER_BY_JOB.put(jobName, new SimpleUser(username, password));
	Job job = new Job(urlRepo, jobName, description, new SimpleUser(username, password));
	jobsList.add(job);
    }

    public static Job getJob(String jobName) {
	if (jobsList.isEmpty()) {
	    init();
	}
	for (Job job : jobsList) {
	    if (job.getJobName().equals(jobName)) {
		return job;
	    }
	}
	return null;
    }

    public static Set<String> getJobNames() {
	return jobsList.stream().map(j -> j.getJobName()).collect(Collectors.toSet());
    }

    public static void removeEntry(TableItem[] selection) {
	for (int i = 0; i < selection.length; i++) {
	    final String key = selection[i].getText();
	    if (USER_BY_JOB.containsKey(key)) {
		USER_BY_JOB.remove(key);
	    }
	}
	final List<Job> jobsToRemove = new ArrayList<Job>();
	for (final Job job : jobsList) {
	    for (final TableItem selectedItem : selection) {
		if (job.getJobName().equals(selectedItem.getText())) {
		    jobsToRemove.add(job);
		}
	    }
	}
	jobsList.removeAll(jobsToRemove);
	save();
    }

    public static void save() {
	BufferedWriter writer = null;
	try {
	    writer = new BufferedWriter(new FileWriter(new File("usersByJob.csv")));
	    for (Job job : jobsList) {
		writer.write(job.getUrlRepo() + "," + job.getJobName() + "," + job.getUser().getUsername() + "," + job.getUser().getPassword() + ","
		        + job.getJobDescription() + "\n");
	    }
	    writer.close();
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    try {
		if (writer != null) {
		    writer.close();
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    public static void init() {
	BufferedReader reader = null;
	try {
	    reader = new BufferedReader(new FileReader(new File("usersByJob.csv")));
	    String r = null;
	    while ((r = reader.readLine()) != null) {
		String[] values = r.split(",");
		USER_BY_JOB.put(values[0], new SimpleUser(values[1], values[2]));
		jobsList.add(new Job(values[0], values[1], values[4], new SimpleUser(values[2], values[3])));
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    try {
		if (reader != null) {
		    reader.close();
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    public static Job getRepo(String urlRepo) {
	if (jobsList.isEmpty()) {
	    init();
	}
	for (Job job : jobsList) {
	    if (job.getUrlRepo().equals(urlRepo)) {
		return job;
	    }
	}
	return null;
    }
}
