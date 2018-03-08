package task.service;

import task.data.CycleData;
import task.data.DataObject;
import task.data.Extremum;
import task.data.Result;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface CalcManager {

    List<Result> calculateValues(File folder);

    Map<Extremum, Double> getExtremesValues(List<DataObject> dataObjectList);

    List<CycleData> getCyclesData(Path path);

}
