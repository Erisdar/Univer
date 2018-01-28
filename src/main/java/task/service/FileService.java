package task.service;

import task.Result;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface FileService {

    void writeFile(List<Result> results);

    List<Double> getValues(Path path);

    String getLastDirectory();

    void writeDirectoryToFile(String directory);

}
