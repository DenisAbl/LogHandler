package errorHandlers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseErrorHandler implements LogErrorHandler {


    protected String readFile(String path) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    protected void updateQuantity(Map<String, ErrorInfo> errors, String throwableName, String hour) {
        ErrorInfo errorInfo = errors.get(throwableName);
        if (errorInfo != null){
            errorInfo.incrementTotalQuantity();
            errorInfo.incrementQuantityPerHour(hour);
        }
        else errors.put(throwableName, new ErrorInfo(throwableName,hour));
    }




}
