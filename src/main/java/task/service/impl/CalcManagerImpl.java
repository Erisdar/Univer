package task.service.impl;

import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import task.CycleData;
import task.Result;
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

    private List<CycleData> getCyclesData(Path path) {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');

        List<CycleData> cycleData = new ArrayList<>();

        fileService.getCycles(path).forEach((k, v) -> {
            List<Double> values = fileService.getValues(v);
            cycleData.add(new CycleData(k, new DecimalFormat("#0.000000", otherSymbols).format(Collections.max(values) + ACCURACY_CONST),
                    new DecimalFormat("#0.000000", otherSymbols).format(Collections.min(values) + ACCURACY_CONST)));
        });

        return cycleData;
    }
}
