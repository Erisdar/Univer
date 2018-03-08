package task;


import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import io.vavr.control.Try;
import one.util.streamex.StreamEx;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import task.service.CalcManager;
import task.service.DocService;
import task.service.FileService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CalcManagerPerformanceTest extends Assert {

    private ApplicationContext context = new AnnotationConfigApplicationContext(task.config.AppConfig.class);
    private FileService fileService = context.getBean(FileService.class);
    private CalcManager calcManager = context.getBean(CalcManager.class);
    private DocService docService = context.getBean(DocService.class);

    private File folder = Paths.get("D:\\test").toFile();

    private Path file = Paths.get("D:\\test\\0.5RT60na20ip=iobr el1.txt");
    private Path sixCyclesFile = Paths.get("G:\\Trogg\\Knowledge\\Магистратура\\Научка\\Kuzmar\\10k1 BGTU Результаты\\0.1IT10k1BGTU.txt");


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
        Pattern p = Pattern.compile("^(.*(Step|Time|Cycle).*)|(^$)$");

        StreamEx.of(Try.of(() -> Files.lines(sixCyclesFile)).get())
                .groupRuns((prev, next) -> !p.matcher(prev).matches() && !p.matcher(next).matches())
                .filter(list -> list.size() > 1)
                .collect(Collectors.toList());
    }

}
