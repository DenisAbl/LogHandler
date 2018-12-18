package errorHandlers;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeThreadLevelClassTextLogHandler extends BaseErrorHandler {

    private LogErrorHandler nextHandler;

    final private static String TIMESTAMP = "(?<timestamp>\\d{2}.\\d{2}.\\d{4}\\s\\d{2}:\\d{2}:\\d{2})";
    final private static String THREAD = "\\[(?<thread>[^\\]]+)]";
    final private static String LEVEL = "(?<level>ERROR)";
    final private static String CLASS = "(?<class>.*?)";
    final private static String TEXT = "(?<text>.*?)(?=\\d{2}.\\d{2}.\\d{4}\\s\\d{2}:\\d{2}:\\d{2}|\\Z)";
    final private static String EXCEPTION = "(?<exception>\\S+\\.\\S+Exception)";
    private static Pattern patternLogFile = Pattern.compile(TIMESTAMP + "\\s" + THREAD + "\\s" + LEVEL + "\\s+" + CLASS + "\\s+-" + TEXT, Pattern.DOTALL);

    public DateTimeThreadLevelClassTextLogHandler() {}

    @Override
    public void handle(File file, Map<String, ErrorInfo> errorsMap){
                String logText = null;
                try {
                    logText = readFile(file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Matcher matcher = patternLogFile.matcher(logText);
                while (matcher.find()) {
                    String throwableName;
                    String text = matcher.group("text");
                    String hour = matcher.group("timestamp").substring(11,13);
                    Pattern gcOverheadPattern = Pattern.compile("GC overhead limit exceeded");
                    Matcher outOfMemoryMatcher = gcOverheadPattern.matcher(text);
                    if (outOfMemoryMatcher.find()){
                        throwableName = "java.lang.OutOfMemoryError";

                        updateQuantity(errorsMap, throwableName, hour);
                    }
                    else {
                        Pattern exception = Pattern.compile(EXCEPTION+"[:]");
                        Matcher exceptionsMatcher = exception.matcher(text);
                        if(exceptionsMatcher.find()){
                            throwableName = exceptionsMatcher.group("exception");
                            updateQuantity(errorsMap, throwableName, hour);
                        }
                    }
                }
                if(nextHandler!= null) nextHandler.handle(file, errorsMap);
    }

    @Override
    public void setNextHandler(LogErrorHandler handler) {
        nextHandler = handler;
    }




}
