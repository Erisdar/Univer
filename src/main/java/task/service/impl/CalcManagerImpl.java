package task.service.impl;

import io.vavr.control.Try;
import one.util.streamex.StreamEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import task.data.CycleData;
import task.data.DataObject;
import task.data.Extremum;
import task.data.Result;
import task.service.CalcManager;
import task.service.FileService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CalcManagerImpl implements CalcManager {

    private static final Double ACCURACY_CONST = 0.201;

    @Autowired
    private FileService fileService;

    @Override
    public List<Result> calculateValues(File folder) {
        Pattern fileName = Pattern.compile("^((\\d*([.])\\d*)|(\\d+))\\s*(RT|IT).*(.txt)$");

        return Try.of(() -> Files.walk(Paths.get(folder.toURI())))
                .getOrElseThrow(((Function<Throwable, IllegalStateException>) IllegalStateException::new))
                .parallel()
                .filter(path -> fileName.matcher(path.getFileName().toString()).matches())
                .map(file -> new Result(file,
                        Double.parseDouble(file.getFileName().toString().split("(RT|IT)")[0]), getCyclesData(file)))
                .sorted(Comparator.comparing(Result::getAmperage))
                .collect(Collectors.toList());
    }

    @Override
    public List<CycleData> getCyclesData(Path file) {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');

        return fileService.splitFileToCycles(file).stream()
                .filter(cycle -> getExtremesValues(cycle).size() == 2)
                .map(cycle -> {
                    Map<Extremum, Double> cycleValues = getExtremesValues(cycle);
                    return new CycleData(new DecimalFormat("#0.000000", otherSymbols).format(cycleValues.get(Extremum.Max)),
                            new DecimalFormat("#0.000000", otherSymbols).format(cycleValues.get(Extremum.Min)));
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<Extremum, Double> getExtremesValues(List<DataObject> dataObjectList) {
        return StreamEx.of(dataObjectList)
                .filter(dataObject -> dataObject.getCurrent() != 0)
                .filter(dataObject -> dataObjectList.indexOf(dataObject) != 0
                        && dataObjectList.indexOf(dataObject) != dataObjectList.size() - 1)
                .filter(dataObject -> (dataObject.getCurrent() > 0
                        && (dataObjectList.get(dataObjectList.indexOf(dataObject) - 1).getCurrent() > 0 ||
                        dataObjectList.get(dataObjectList.indexOf(dataObject) + 1).getCurrent() > 0)) ||
                        (dataObject.getCurrent() < 0 &&
                                (dataObjectList.get(dataObjectList.indexOf(dataObject) - 1).getCurrent() < 0 ||
                                        dataObjectList.get(dataObjectList.indexOf(dataObject) + 1).getCurrent() < 0)))
                .groupRuns((prev, next) ->
                        (prev.getCurrent() > 0 && next.getCurrent() > 0) ||
                                (prev.getCurrent() < 0 && next.getCurrent() < 0))
                .filter((individualList) -> individualList.size() > 1)
                .groupingBy(individualList -> individualList.stream()
                        .findFirst().orElseThrow(IllegalStateException::new).getCurrent() > 0)
                .entrySet().stream()
                .map((mapPairs) -> new AbstractMap.SimpleEntry<>(mapPairs.getKey() ? Extremum.Max : Extremum.Min,
                        mapPairs.getValue().stream()
                                .map(groupedIndividualList -> groupedIndividualList.stream()
                                        .map(DataObject::getPotential)
                                        .mapToDouble(potential -> potential)
                                        .reduce(mapPairs.getKey() ? Double::max : Double::min)
                                        .orElseThrow(() -> new RuntimeException("DataObject is null")))
                                .mapToDouble(maxOrMinValue -> maxOrMinValue + ACCURACY_CONST)
                                .average()
                                .orElseThrow(() -> new RuntimeException("DataObject is null"))))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }
}
