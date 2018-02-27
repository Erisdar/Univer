package task.service;

import task.Result;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface FileService {

    void writeFile(List<Result> results);

    List<Double> getValues(List<String> cycles);

    Map<String, List<String>> getCycles(Path path);


}
