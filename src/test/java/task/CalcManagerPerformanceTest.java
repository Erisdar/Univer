package task;


import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import task.data.CycleData;
import task.service.CalcManager;
import task.service.DocService;
import task.service.FileService;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class CalcManagerPerformanceTest extends Assert {

    private ApplicationContext context = new AnnotationConfigApplicationContext(task.config.AppConfig.class);
    private FileService fileService = context.getBean(FileService.class);
    private CalcManager calcManager = context.getBean(CalcManager.class);
    private DocService docService = context.getBean(DocService.class);

    private File folder = Paths.get("D:\\test").toFile();

    private Path file = Paths.get("D:\\test\\0.5RT60na20ip=iobr el1.txt");

    private static final Double ACCURACY_CONST = 0.201;


    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule();

    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10)
    @Test
    public void testFull() throws Exception {
        fileService.writeFile(calcManager.calculateValues(folder));
    }

    @Test
    public void getCycles() throws Exception {
        System.out.println(fileService.getCycles(file).size());

    }

    @Test
    public void getCyclesData() throws Exception {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        List<CycleData> cycleData = new ArrayList<>();


        fileService.getCycles(file).forEach((k, v) -> {
            Map<Boolean, Double> map = calcManager.getMaxAndMinValue(fileService.getValues(v));
            Optional.ofNullable(map.get(true)).ifPresent(maxValue -> Optional.ofNullable(map.get(false)).ifPresent(minValue -> {
                cycleData.add(new CycleData(new DecimalFormat("#0.000000", otherSymbols).format(maxValue),
                        new DecimalFormat("#0.000000", otherSymbols).format(minValue)));
            }));
        });

        assertEquals(cycleData, calcManager.getCyclesData(file));

    }

}
