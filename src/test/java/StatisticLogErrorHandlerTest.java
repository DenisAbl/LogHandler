import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class StatisticLogErrorHandlerTest {

    StatisticLogErrorHandler handler;

    @Before
    public void setUp() {
        handler = new StatisticLogErrorHandler("C:/Users/WAY/Desktop/logs");
    }

    @Test
    public void handleTest() throws IOException {
        handler.handle();
//        handler.filesList.forEach(file -> System.out.println(file.getName()));
    }


}
