import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        LogErrorHandler errorHandler = new StatisticLogErrorHandler("C:/Users/ru0syda/Desktop/logs");
        errorHandler.handle();
    }
}
