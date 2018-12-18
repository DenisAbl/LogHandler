import errorHandlers.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LogParser {

    private final String folderPath;
    private List<File> filesList;
    private volatile Map<String, ErrorInfo> errorsMap = new ConcurrentHashMap<>();

    public LogParser(String folderPath) {
        this.folderPath = folderPath;
    }

    /**
     * Метод выполняющий функционал, описанный в первом пункте тестового задания:
     * Даны N файлов out. Необходимо за минимально возможное время вычислить
     * распределение возникновения ошибок (ERROR) за каждый час/минуту/.
     * Соответствующие результаты вывести в отдельный файл Statistics.
     *
     * Файл будет находиться в папке с логами.
     * @throws IOException
     */

    public void createExtendedListFile() throws IOException {
        writeMapToExtendedFile(createErrorMap());
    }

    /**
     * Метод выполняющий функционал, описанный во втором пункте тестового задания
     * Даны N файлов out. Необходимо за минимально возможное время вычислить
     * количество появления ошибок (ERROR) упорядочив данные по наименованию (типу)
     * исключения. Соответствующие результаты вывести в отдельный файл Statistics.
     *
     * Файл будет находиться в папке с логами.
     * @throws IOException
     */

    public void createErrorListFile() throws IOException {
        writeMapToFile(createErrorMap());
    }
    /**
     * В основе паттерн chain of responsibility: т.к. логи в присланной выборке не все имеют одинаковый формат,
     * обрабатываться они будут разными обработчиками. Обработчики последовательно предают работу друг другу.
     * Даже в ситуации, когда в одном файле могут оказаться по разному отформатированные куски логов -
     * обработчики должны будут справиться с поставленной задачей. Каждый обработчик наследуется от базового
     * абстрактного класса, в который вынесены общие методы.
     * @return
     */
    public Map<String, ErrorInfo> createErrorMap(){
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        getFilesList();
        LogErrorHandler firstErrorHandler = new DateTimeLevelClassTextLogHandler();
        LogErrorHandler secondErrorHandler = new TimeThreadLevelClassTextLogHandler();
        LogErrorHandler thirdErrorHandler = new DateTimeThreadLevelClassTextLogHandler();
        firstErrorHandler.setNextHandler(secondErrorHandler);
        secondErrorHandler.setNextHandler(thirdErrorHandler);
        if (filesList == null){
            try (PrintWriter out = new PrintWriter(folderPath + "/Statistic.txt")) {
                out.println("no log files were found");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else {
            filesList.forEach(file -> executor.execute(() -> {
                firstErrorHandler.handle(file, errorsMap);
            }));
            executor.shutdown();
            try {
                executor.awaitTermination(10000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Map<String, ErrorInfo> treeMap = new TreeMap<>();
        treeMap.putAll(errorsMap);
        return treeMap;
    }

    private void writeMapToFile(Map<String, ErrorInfo> errors) throws IOException {
        Files.write(Paths.get(folderPath + "/Statistic.txt"), () -> errors.entrySet().stream()
                .<CharSequence>map(e -> e.getKey() + " " + e.getValue().getTotalQuantity())
                .iterator());
    }

    private void writeMapToExtendedFile(Map<String, ErrorInfo> errors) throws IOException {
        FileWriter fileWriter = new FileWriter(folderPath + "/Statistic.txt");
        PrintWriter printWriter = new PrintWriter(fileWriter);
        String exceptionName;
        Map<String,AtomicInteger> internalMap;
        for (Map.Entry<String, ErrorInfo> entry : errors.entrySet()){
            exceptionName = entry.getKey();
            internalMap = entry.getValue().getQuantityPerHour();
            for (Map.Entry<String,AtomicInteger> internalEntry : internalMap.entrySet()){
                printWriter.printf("Exception type: %s hour: %s quantity %d %n", exceptionName, internalEntry.getKey(),internalEntry.getValue().get());
            }

        }
        printWriter.close();

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
}
