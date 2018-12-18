package errorHandlers;

import java.io.File;
import java.util.Map;

public interface LogErrorHandler {

    void handle(File file, Map<String, ErrorInfo> errorMap);

    void setNextHandler(LogErrorHandler handler);
}
