package srx;

/**
 * 
 *
 * @author loomchild
 */
public class Settings {
	
	// Być może dodać klonowalność i umożliwić modyfikacje przez mutatory

	public Settings(boolean includeStart, boolean includeEnd, 
			boolean includeIsolated, boolean segmentSubflows) {
		this.includeStart = includeStart;
		this.includeEnd = includeEnd;
		this.includeIsolated = includeIsolated;
		this.segmentSubflows = segmentSubflows;
	}
	
	public Settings() {
		this(false, true, true, true);
	}

	public boolean isIncludeEnd() {
		return includeEnd;
	}

	public boolean isIncludeIsolated() {
		return includeIsolated;
	}

	public boolean isIncludeStart() {
		return includeStart;
	}

	public boolean isSegmentSubflows() {
		return segmentSubflows;
	}

	private boolean includeStart;

	private boolean includeEnd;

	private boolean includeIsolated;
	
	private boolean segmentSubflows;

}
