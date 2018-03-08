package task.service;

import task.data.Average;
import task.data.Result;

import java.util.List;
import java.util.OptionalDouble;

public interface CalcHelper {

    int getMaxCycleCount(List<Result> results);

    List<Average> getListAverage(List<Result> results);

}
