package me.THEREALWWEFAN231.funnelmc.utils;

import java.io.File;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

import net.minecraft.client.Minecraft;

// Vanilla's log4j2 config only writes to latest.log, which mixes in every other mod's/game's noise -
// makes it painful to grab just the lines relevant to a FunnelMC bug report. This attaches a second
// appender straight to the live Log4j2 configuration (rather than shipping our own log4j2.xml, which
// would replace vanilla's config wholesale instead of adding to it), filtered down to just our own
// package's log lines plus anything vanilla logged whose exception actually originated in our code
// (e.g. "Error executing task on Client" from BlockableEventLoop swallowing an exception thrown by one
// of our packet translators) - that second case covers most of the actually-useful crash traces, since
// they're logged by vanilla's own loggers, not ours. latest.log keeps getting everything as before.
public class FunnelLogSetup {

	private static final String PACKAGE_PREFIX = "me.THEREALWWEFAN231.funnelmc";

	public static void install() {
		File logFile = new File(Minecraft.getInstance().gameDirectory, "logs/funnelmc.log");

		LoggerContext context = (LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
		Configuration configuration = context.getConfiguration();

		PatternLayout layout = PatternLayout.newBuilder()
				.withConfiguration(configuration)
				.withPattern("%d{HH:mm:ss} [%t/%level]: %msg%n%throwable")
				.build();

		FileAppender appender = FileAppender.newBuilder()
				.setName("FunnelMCFile")
				.withFileName(logFile.getAbsolutePath())
				.withAppend(false)
				.setLayout(layout)
				.setImmediateFlush(true)
				.setConfiguration(configuration)
				.build();
		appender.start();
		configuration.addAppender(appender);

		LoggerConfig rootConfig = configuration.getRootLogger();
		rootConfig.addAppender(appender, null, new RelevantToFunnelMCFilter());

		context.updateLoggers();
	}

	private static final class RelevantToFunnelMCFilter extends AbstractFilter {

		@Override
		public Result filter(LogEvent event) {
			if (event.getLoggerName() != null && event.getLoggerName().startsWith(PACKAGE_PREFIX)) {
				return Result.ACCEPT;
			}

			Throwable thrown = event.getThrown();
			while (thrown != null) {
				for (StackTraceElement frame : thrown.getStackTrace()) {
					if (frame.getClassName().startsWith(PACKAGE_PREFIX)) {
						return Result.ACCEPT;
					}
				}
				thrown = thrown.getCause();
			}

			return Result.DENY;
		}

	}

}
