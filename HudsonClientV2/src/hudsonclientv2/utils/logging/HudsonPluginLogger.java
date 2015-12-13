package hudsonclientv2.utils.logging;

import hudsonclientv2.Activator;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;

public class HudsonPluginLogger {
	private static final ILog LOGGER = Activator.getDefault().getLog();

	private HudsonPluginLogger() {

	}

	public static void logOk(String pluginId, String message) {
		LOGGER.log(new Status(Status.OK, pluginId, message));
	}

	public static void logInfo(String pluginId, String message) {
		LOGGER.log(new Status(Status.INFO, pluginId, message));
	}

	public static void logError(String pluginId, String message) {
		LOGGER.log(new Status(Status.ERROR, pluginId, message));
	}

	public static void logWarn(String pluginId, String message) {
		LOGGER.log(new Status(Status.WARNING, pluginId, message));
	}

	public static void log(int severity, String pluginId, String message, Throwable e) {
		LOGGER.log(new Status(severity, pluginId, message, e));
	}

	public static void logException(final String message, final Throwable e) {
		LOGGER.log(new Status(Status.ERROR, Activator.PLUGIN_ID, "An error occured during refresh", e));
	}

	public static void logException(final Throwable e) {
		logException("An unexpected error has occured", e);
	}
}
