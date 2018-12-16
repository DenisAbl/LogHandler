import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StatisticLogErrorHandler implements LogErrorHandler {

    private final String folderPath;
    public List<File> filesList;

    final private static String timestampRgx = "(?<timestamp>\\d{2}.\\d{2}.\\d{4}\\s\\d{2}:\\d{2}:\\d{2}.\\d{3})";
    final private static String levelRgx = "(?<level>ERROR)";
    final private static String classRgx = "\\[(?<class>[^\\]]+)]";
    final private static String threadRgx = "\\[(?<thread>[^\\]]+)]";
   // final private static String textRgx = "(?<text>.*?)(?=\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}|\\Z)";
    private static Pattern patternFullLog = Pattern.compile(timestampRgx + " " + levelRgx /*+ "\\s+" + classRgx + "-" + threadRgx + "\\s+"*/, Pattern.DOTALL);


    public StatisticLogErrorHandler(String folderPath) {
        this.folderPath = folderPath;
    }

    @Override
    public void handle() throws IOException {
        getFilesList();
        String logText = readFile(filesList.get(1).getAbsolutePath());
        Matcher matcher = patternFullLog.matcher(logText);
        while(matcher.find()) {
            System.out.println(matcher.group("timestamp"));
            System.out.println(matcher.group("level"));
//            System.out.println(matcher.group("class"));
//            System.out.println(matcher.group("thread"));
//            System.out.println(matcher.group("text"));

            System.out.println("----");
        }

    }

    /**
     * Метод получает список файлов из указанной при создании объекта папки.
     * В список попадают только файлы начинающиеся на log*
      */
    private void getFilesList(){
        File[] files = new File(folderPath).listFiles();
        filesList = Arrays.stream(files)
                .filter(file -> file.getName().startsWith("log"))
                .collect(Collectors.toList());
    }

    private static String readFile(String path)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }
}
