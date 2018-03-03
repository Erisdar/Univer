package task.service.impl;

import io.vavr.control.Try;
import one.util.streamex.StreamEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import task.data.CycleData;
import task.data.DataObject;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CalcManagerImpl implements CalcManager {

    private static final Double ACCURACY_CONST = 0.201;

    @Autowired
    private FileService fileService;

    @Override
    public List<Result> calculateValues(File folder) {
        Pattern p = Pattern.compile("^.*(\\.txt)$");


        return Try.of(() -> Files.walk(Paths.get(folder.toURI()))
                .parallel()
                .filter(path -> p.matcher(path.toString()).lookingAt())
                .map(path -> new Result(path, Double.parseDouble(path.getFileName().toString().split("(RT|IT)")[0]), getCyclesData(path)))
                .sorted(Comparator.comparing(Result::getAmperage))
                .collect(Collectors.toList()))
                .getOrNull();
    }

    @Override
    public List<CycleData> getCyclesData(Path path) {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');

        return fileService.getCycles(path).entrySet().stream()
                .filter(mapCycles -> getMaxAndMinValue(fileService.getValues(mapCycles.getValue())).size() == 2)
                .map(mapCycles -> {
                    Map<Boolean, Double> map = getMaxAndMinValue(fileService.getValues(mapCycles.getValue()));
                    return new CycleData(new DecimalFormat("#0.000000", otherSymbols).format(map.get(true)),
                            new DecimalFormat("#0.000000", otherSymbols).format(map.get(false)));
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<Boolean, Double> getMaxAndMinValue(List<DataObject> dataObjectList) {
        return StreamEx.of(dataObjectList)
                .groupRuns((prev, next) ->
                        (prev.getCurrent() > 0 && next.getCurrent() > 0) ||
                                (prev.getCurrent() < 0 && next.getCurrent() < 0))
                .filter((individualList) -> individualList.size() > 1)
                .groupingBy(individualList -> individualList.stream().findFirst().get().getCurrent() > 0)
                .entrySet().stream()
                .map((mapPairs) -> new AbstractMap.SimpleEntry<>(mapPairs.getKey(),
                        mapPairs.getValue().stream()
                                .map(groupedIndividualList -> groupedIndividualList.stream()
                                        .map(DataObject::getPotential)
                                        .mapToDouble(potential -> potential)
                                        .reduce(mapPairs.getKey() ? Double::max : Double::min)
                                        .getAsDouble())
                                .mapToDouble(maxOrMinValue -> maxOrMinValue + ACCURACY_CONST)
                                .average()
                                .getAsDouble()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }
}
