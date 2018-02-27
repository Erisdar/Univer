package task.service.impl;

import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import task.Result;
import task.service.DocService;
import task.service.FileService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private DocService docService;

    @Override
    public void writeFile(List<Result> results) {

        Optional.ofNullable(results).ifPresent(res -> docService.writeToTable(results, results.get(0).getFile().getParent().toString().concat(".xls")));

    }

    @Override
    public List<Double> getValues(List<String> cycles) {
        return cycles.stream()
                .parallel()
                .map(line -> line = line.split("([\\s])+")[1])
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<String>> getCycles(Path path) {
        Pattern p = Pattern.compile("^(.*(Step|Time|Physical Cycle).*)|(^$)$");

        Map<String, List<String>> map = new LinkedHashMap<>();

        List<String> mainList = Try.of(() -> Files.lines(path).filter(line -> !p.matcher(line).matches())
                .collect(Collectors.toList())).get();

        long r = mainList.stream().filter(s -> s.contains("Cycle")).count();

        for (int i = 0; i < r; i++) {
            if (i < r - 1) {
                map.put("Cycle " + i + 1, mainList.subList(mainList.indexOf("Cycle " + String.valueOf(i + 1)) + 1, Optional.of(mainList.indexOf("Cycle " + String.valueOf(i + 2))).orElse(mainList.size())));
            } else {
                map.put("Cycle " + i + 1, mainList.subList(mainList.indexOf("Cycle " + String.valueOf(i + 1)) + 1, mainList.size()));
            }
        }
        return map;
    }
}
