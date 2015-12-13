package hudsonclientv2.bo;

/**
 * @author libenzi Implementation of test results for surefire tests
 */
public class ResultsTestSurefire implements ResultsTests {

	private int buildNumber;

	private int totalCount;

	private int errorsCount;

	private int ignoreCount;

	public ResultsTestSurefire(int buildNumber, int totalCount, int errorsCount, int ignoreCount) {
		super();
		this.buildNumber = buildNumber;
		this.totalCount = totalCount;
		this.errorsCount = errorsCount;
		this.ignoreCount = ignoreCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudsonclient.resultsTest.ResultsTests#getBuildNumber()
	 */
	@Override
	public int getBuildNumber() {
		return buildNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudsonclient.resultsTest.ResultsTests#setBuildNumber(int)
	 */
	@Override
	public void setBuildNumber(int buildNumber) {
		this.buildNumber = buildNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudsonclient.resultsTest.ResultsTests#getTotalCount()
	 */
	@Override
	public int getTotalCount() {
		return totalCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudsonclient.resultsTest.ResultsTests#setTotalCount(int)
	 */
	@Override
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudsonclient.resultsTest.ResultsTests#getErrorsCount()
	 */
	@Override
	public int getErrorsCount() {
		return errorsCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudsonclient.resultsTest.ResultsTests#setErrorsCount(int)
	 */
	@Override
	public void setErrorsCount(int errorsCount) {
		this.errorsCount = errorsCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudsonclient.resultsTest.ResultsTests#getIgnoreCount()
	 */
	@Override
	public int getIgnoreCount() {
		return ignoreCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudsonclient.resultsTest.ResultsTests#setIgnoreCount(int)
	 */
	@Override
	public void setIgnoreCount(int ignoreCount) {
		this.ignoreCount = ignoreCount;
	}

	@Override
	public String toString() {
		return "ResultsTestSurefire [buildNumber=" + buildNumber + ", totalCount=" + totalCount + ", errorsCount=" + errorsCount + ", ignoreCount="
		        + ignoreCount + "]";
	}

}
