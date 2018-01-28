package task.service.impl;

import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import task.Result;
import task.service.DocService;
import task.service.FileService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    private static final String JAVA_DIRECTORY = "C:\\Users\\Public\\Documents\\UniversityPath.txt";

    @Autowired
    private DocService docService;

    @Override
    public void writeFile(List<Result> results) {

        Optional.ofNullable(results).ifPresent(res -> docService.writeToTable(results, results.get(0).getFile().getParent().toString().concat(".docx")));

    }

    @Override
    public List<Double> getValues(Path path) {
        Pattern p = Pattern.compile("^(.*(Cycle|Step|Time).*)|(^$)$");

        return Try.of(() -> Files.lines(path)
                .parallel()
                .filter(line -> !p.matcher(line).matches())
                .map(line -> line = line.split("([\\s])+")[1])
                .map(Double::parseDouble)
                .collect(Collectors.toList()))
                .get();
    }

    @Override
    public String getLastDirectory() {
        return Try.of(() -> Files.readAllLines(Paths.get(JAVA_DIRECTORY)).get(0)).getOrElse(".");
    }

    @Override
    public void writeDirectoryToFile(String directory) {
        Try.run(() -> Files.write(Paths.get(JAVA_DIRECTORY), directory.getBytes()));
    }
}
