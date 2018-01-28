package task.service.impl;

import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import task.Result;
import task.service.CalcManager;
import task.service.FileService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CalcManagerImpl implements CalcManager {

    @Autowired
    private FileService fileService;

    @Override
    public List<Result> calculateValues(File folder) {
        Pattern p = Pattern.compile("^.*(\\.txt)$");

        return Try.of(() -> Files.walk(Paths.get(folder.toURI()))
                .parallel()
                .filter(path -> p.matcher(path.toString()).lookingAt())
                .map(path -> {
                    List<Double> values = fileService.getValues(path);
                    return new Result(path, Double.parseDouble(path.getFileName().toString().split("RT")[0]),
                            new DecimalFormat("#0.000000").format(Collections.max(values)),
                            new DecimalFormat("#0.000000").format(Collections.min(values)));
                })
                .sorted(Comparator.comparing(Result::getAmperage))
                .collect(Collectors.toList()))
                .getOrNull();
    }
}
