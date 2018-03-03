package task.service;

import task.data.Result;

import java.util.List;

public interface DocService {

    void writeToTable(List<Result> results, String filePath);

}
