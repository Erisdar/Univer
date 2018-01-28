package task.service;

import task.Result;

import java.io.File;
import java.util.List;

public interface CalcManager {

    List<Result> calculateValues(File folder);

}
