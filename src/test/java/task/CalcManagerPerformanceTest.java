package task;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import task.service.CalcManager;
import task.service.FileService;

import java.io.File;
import java.nio.file.Paths;

public class CalcManagerPerformanceTest {

    private ApplicationContext context = new AnnotationConfigApplicationContext(task.config.AppConfig.class);
    private FileService fileService = context.getBean(FileService.class);
    private CalcManager calcManager = context.getBean(CalcManager.class);

    private File file = Paths.get("G:\\Trogg\\Knowledge\\Магистратура\\Научка\\Kuzmar\\10k1 BGTU sper Результаты").toFile();

    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule();

    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10)
    @Test
    public void testFull() throws Exception {
        fileService.writeFile(calcManager.calculateValues(file));
    }

    @Test
    public void testSelectPath() {
        System.out.println(fileService.getLastDirectory());
    }

}
