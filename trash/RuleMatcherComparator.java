package srx;

import java.util.Comparator;

public class RuleMatcherComparator implements Comparator<RuleMatcher> {

	public static RuleMatcherComparator getInstance() {
		if (instance == null) {
			instance = new RuleMatcherComparator();
		}
		return instance;
	}
	
	public int compare(RuleMatcher matcher1, RuleMatcher matcher2) {
		if (matcher1.hitEnd()) {
			if (matcher2.hitEnd()) {
				return 0;
			} else {
				return 1;
			}
		} else if (matcher2.hitEnd()) {
			return -1;
		} else {
			return matcher1.getBreakPosition() - matcher2.getBreakPosition();
		}
	}
	
	private static RuleMatcherComparator instance;

}
