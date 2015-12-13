package hudsonclientv2.bo;

/**
 * @author libenzi Defines interface for testresults.
 */
public interface ResultsTests {

	public abstract int getBuildNumber();

	public abstract void setBuildNumber(int buildNumber);

	public abstract int getTotalCount();

	public abstract void setTotalCount(int totalCount);

	public abstract int getErrorsCount();

	public abstract void setErrorsCount(int errorsCount);

	public abstract int getIgnoreCount();

	public abstract void setIgnoreCount(int ignoreCount);

}