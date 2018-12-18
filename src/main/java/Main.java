import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        LogParser parser = new LogParser("C:/Users/ru0syda/Desktop/logs");
        parser.createExtendedListFile();
    }
}
