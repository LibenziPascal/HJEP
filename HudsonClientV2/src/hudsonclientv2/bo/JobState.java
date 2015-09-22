package hudsonclientv2.bo;

/**
 * @author libenzi
 * Defines states for a job. It could be ok (green or blue), ko (red) or unstable (yellow)
 */
public enum JobState {
	OK(true), 
	KO(false, true), 
	UNSTABLE(false);
	
	private boolean green = false;
	private boolean red = false;
	
	private JobState(boolean green) {
		this.green = green;
    }

	private JobState(boolean green, boolean isRed) {
		this.green = green;
		this.red = isRed;
    }

	public boolean isGreen() {
	    return green;
    }
	
	public boolean isRed() {
	    return red;
    }
	
	public static void main(String[] args) {
		JobBuild jobBuild = new JobBuild(OK, "");
		System.out.println(jobBuild.getCurrentState().isGreen());
		System.out.println(jobBuild.getCurrentState().isRed());
		}
}
