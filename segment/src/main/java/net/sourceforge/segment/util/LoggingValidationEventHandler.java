package net.sourceforge.segment.util;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles XML validation errors by writing them to log.
 * @author loomchild
 */
public class LoggingValidationEventHandler implements ValidationEventHandler {

	private static final Log log = LogFactory
			.getLog(LoggingValidationEventHandler.class);

	public boolean handleEvent(ValidationEvent event) {
		if ((event.getSeverity() == ValidationEvent.FATAL_ERROR)
				|| (event.getSeverity() == ValidationEvent.ERROR)) {
			return false;
		} else if (event.getSeverity() == ValidationEvent.WARNING) {
			log.debug("Validation warning: " + event.getMessage() + ".");
			return true;
		} else {
			log.warn("Unknown validation event type.");
			return false;
		}
	}

}
