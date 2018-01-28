package task.service;

import task.Result;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface DocService {

    public void writeToTable(List<Result> results, String filePath);

}
