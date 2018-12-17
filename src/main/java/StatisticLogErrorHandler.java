import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StatisticLogErrorHandler implements LogErrorHandler {

    private final String folderPath;
    private List<File> filesList;
    private volatile Map<String,Integer> errors = new ConcurrentHashMap<>();

    final private static String timestampRgx = "(?<timestamp>\\d{2}[.:]\\d{2}[.:]\\d{2})";
    final private static String levelRgx = "(?<level>ERROR)";
    final private static String classRgx = "(?<class>.*?)";
    final private static String textRgx = "(?<text>.*?)(?=\\d{2}[.:]\\d{2}[.:]\\d{2})";
    final private static String exceptionNameRgx = "(?<exception>\\S+\\.\\S+Exception)";
    private static Pattern patternLogFile = Pattern.compile(timestampRgx +".*?\\s" + levelRgx + ".*?-" + textRgx, Pattern.DOTALL);

    public StatisticLogErrorHandler(String folderPath) {
        this.folderPath = folderPath;
    }


    /**
     * Метод обрабатывает список файлов, находящихся в папке, заданной при создании объекта через конструктор
     * Много поточность реализована при помощи FixedThreadExecutor.
     * Каждый поток получает на обработку свой файл.
     * Сначала извлекается информация о текстовой сотавляющей записи. Однако, есть и другие группы паттернов,
     * при помощи которых можно получить информацию о времени записи и классе, в результате работы которого записи появилась.
     *
     * Сначала обрабатывается ситуация с возникновением OutOfMemoryError, далее идут остальные исключения.
     *
     *
     * @throws IOException
     */
    @Override
    public void handle() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        getFilesList();
        if (filesList == null){
            try (PrintWriter out = new PrintWriter(folderPath + "/Statistic.txt")) {
                out.println("no log files were found");
            }
        }
        else {
            filesList.forEach(file -> executor.execute(() -> {
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
                    Pattern gcOverheadPattern = Pattern.compile("GC overhead limit exceeded");
                    Matcher outOfMemoryMatcher = gcOverheadPattern.matcher(text);
                    if (outOfMemoryMatcher.find()){
                        throwableName = "java.lang.OutOfMemoryError";
                        updateQuantity(errors, throwableName);
                    }
                    else {
                        Pattern exception = Pattern.compile(exceptionNameRgx);
                        Matcher exceptionsMatcher = exception.matcher(text);
                        if(exceptionsMatcher.find()){
                            throwableName = exceptionsMatcher.group("exception");
                            updateQuantity(errors, throwableName);
                        }
                    }
                }
            }));
            executor.shutdown();
            try {
                executor.awaitTermination(10000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Map<String,Integer> treeMap = new TreeMap<>();
        treeMap.putAll(errors);
        writeMapToFile(treeMap);

    }

    private void writeMapToFile(Map<String, Integer> errors) throws IOException {
        Files.write(Paths.get(folderPath + "/Statistic.txt"), () -> errors.entrySet().stream()
                .<CharSequence>map(e -> e.getKey() + " " + e.getValue())
                .iterator());
    }

    private void updateQuantity(Map<String, Integer> errors, String throwableName) {
        Integer quantity = errors.get(throwableName);
        if (quantity != null){
            int updatedQuantity = ++quantity;
            errors.put(throwableName,updatedQuantity);
        }
        else errors.put(throwableName,1);
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

    /**
     * Метод считывает файл как одну сплошную строку используя UTF-8 charset
     *
     * @param path
     *        Путь к файлу
     *
     * @return  файл в виде String
     *
     * @throws IOException
     */
    private static String readFile(String path) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }
}
