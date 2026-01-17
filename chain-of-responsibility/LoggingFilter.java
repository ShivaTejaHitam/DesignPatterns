public abstract class Logger {
  protected Logger nextLogger;

  public void setNextLogger(Logger nextLogger){
    this.nextLogger = nextLogger;
  }

  public void logMessage(Loglevel level,String message) {
    if(canHandle(level)){
       handle(message);
    } 
    if (nextLogger != null){
      nextLogger.logMessage(level,message);
    }
  }

  protected abstract boolean canHandle(LogLevel level);
  protected abstract void write(String message);
}

public enum LogLevel {
    INFO,
    DEBUG,
    ERROR
}

public class InfoLogger extends Logger {

    @Override
    protected boolean canHandle(LogLevel level) {
        return level == LogLevel.INFO;
    }

    @Override
    protected void write(String message) {
        System.out.println("INFO: " + message);
    }
}

public class DebugLogger extends Logger {

    @Override
    protected boolean canHandle(LogLevel level) {
        return level == LogLevel.DEBUG;
    }

    @Override
    protected void write(String message) {
        System.out.println("DEBUG: " + message);
    }
}

public class ErrorLogger extends Logger {

    @Override
    protected boolean canHandle(LogLevel level) {
        return level == LogLevel.ERROR;
    }

    @Override
    protected void write(String message) {
        System.out.println("ERROR: " + message);
    }
}

public class LoggerChainFactory {

    public static Logger createLoggerChain() {
        Logger infoLogger = new InfoLogger();
        Logger debugLogger = new DebugLogger();
        Logger errorLogger = new ErrorLogger();

        infoLogger.setNextLogger(debugLogger);
        debugLogger.setNextLogger(errorLogger);

        return infoLogger; 
    }
}

public class Client {

    public static void main(String[] args) {

        Logger loggerChain = LoggerChainFactory.createLoggerChain();

        loggerChain.logMessage(LogLevel.INFO, "Application started");
        loggerChain.logMessage(LogLevel.DEBUG, "Debugging issue");
        loggerChain.logMessage(LogLevel.ERROR, "Null pointer exception");
    }
}



