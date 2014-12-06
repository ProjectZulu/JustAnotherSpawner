package jas.gui;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiLog {
	private static final String LOG_NAME = "JAS_GUI";
	private static Logger myLog;
	private static GuiLog log;
	private static boolean isSetup;

	public static GuiLog log() {
		return log;
	}

	private class LogType {
		private final boolean isEnabled;

		public LogType(boolean isEnabled) {
			this.isEnabled = isEnabled;
		}

		public boolean isEnabled() {
			return isEnabled;
		}
	}

	public final String FILE_VERSION = "1.0";
	private final LogType DEBUG;

	public GuiLog() {
		DEBUG = new LogType(false);
		log = this;
	}

	public static void setLogger(GuiLog log) {
		if (!isSetup) {
			GuiLog.isSetup = true;
			GuiLog.log = log;
			GuiLog.myLog = LogManager.getLogger("JAS_GUI");
		}
	}

	public void log(Level level, String format, Object... data) {
		myLog.log(level, String.format(format, data));
	}

	public void info(String format, Object... data) {
		log(Level.INFO, format, data);
	}

	public void warning(String format, Object... data) {
		log(Level.WARN, format, data);
	}

	public void severe(String format, Object... data) {
		log(Level.ERROR, format, data);
	}

	public void debug(Level level, String format, Object... data) {
		if (DEBUG.isEnabled()) {
			log(level, format, data);
		}
	}

	public void log(LogType type, Level level, String format, Object... data) {
		if (type.isEnabled()) {
			log(level, format, data);
		}
	}
}
