package hudsonclientv2.bo;

/**
 * Represent a build. TODO utiliser cela (pas encore fait pour l'instant)
 * 
 * @author libenzi
 *
 */
public class JobBuild {

	/**
	 * Job build state
	 */
	private JobState currentState;

	/**
	 * Console log of build
	 */
	private String logConsole;

	/**
	 * Initializer
	 * 
	 * @param currentState
	 * @param logConsole
	 */
	public JobBuild(JobState currentState, String logConsole) {
		this.currentState = currentState;
		this.logConsole = logConsole;
	}

	public JobState getCurrentState() {
		return currentState;
	}

	public void setCurrentState(JobState currentState) {
		this.currentState = currentState;
	}

	public String getLogConsole() {
		return logConsole;
	}

	public void setLogConsole(String logConsole) {
		this.logConsole = logConsole;
	}

}
