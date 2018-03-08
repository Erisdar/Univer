package task.service;

import task.data.DataObject;
import task.data.Result;

import java.nio.file.Path;
import java.util.List;

public interface FileService {

    void writeFile(List<Result> results);

    List<List<DataObject>> splitFileToCycles(Path path);

}
