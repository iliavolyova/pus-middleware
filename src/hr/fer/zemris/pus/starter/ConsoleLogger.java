package hr.fer.zemris.pus.starter;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ConsoleLogger {
	
	private Logger log;
	private String name;
	
	public ConsoleLogger(String loggerName){
		this.name=loggerName;
		log = Logger.getLogger("[LOG]SP "+name);
		log.setUseParentHandlers(false);
		ConsoleHandler conHdlr = new ConsoleHandler();
		conHdlr.setLevel(Level.ALL);
		conHdlr.setFormatter(new Formatter() {
			public String format(LogRecord record) {
				return "[" + record.getLevel() + "] "
						+ name + ": "
						+ record.getMessage() + "\n";
			}
		});
		log.setLevel(Level.ALL);
		log.addHandler(conHdlr);
	}
	
	public void log(String text){
		log.info(text);
	}
	
}
