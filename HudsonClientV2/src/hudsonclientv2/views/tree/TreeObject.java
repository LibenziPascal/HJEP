package hudsonclientv2.views.tree;

import hudsonclientv2.bo.Job;

import org.eclipse.core.runtime.IAdaptable;

public class TreeObject implements IAdaptable {
    private String name;
    private TreeParent parent;
    private int lvl = -1;
    private Job job;
    
    public TreeObject(String name) {
        this.name = name;
    }
    
    public TreeObject(Job job) {
        this.job = job;
        this.name = job.getJobName();
    }

    public String getName() {
        return name;
    }

    public void setParent(TreeParent parent) {
        this.parent = parent;
    }

    public TreeParent getParent() {
        return parent;
    }

    public String toString() {
        return getName();
    }

    public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
        return null;
    }
    
    public Job getJob() {
        return job;
    }

    public int getLevel() {
        TreeObject currentElem = this;
        if (lvl == -1) {
    	while (currentElem.getParent() != null) {
    	    lvl++;
    	    currentElem = currentElem.getParent();
    	}
        }
        return lvl;
    }
}