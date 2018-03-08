package task.service.impl;

import io.vavr.control.Try;
import one.util.streamex.StreamEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import task.data.DataObject;
import task.data.Result;
import task.service.DocService;
import task.service.FileService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private DocService docService;

    @Override
    public void writeFile(List<Result> results) {

        Optional.ofNullable(results).ifPresent(res -> {
            docService.writeToTable(results, results.stream().findFirst()
                    .orElseThrow(() -> new IndexOutOfBoundsException("Folder have't needed files")).getFile()
                    .getParent().toString().concat(".xls"));
        });
    }

    @Override
    public List<List<DataObject>> splitFileToCycles(Path path) {
        Pattern groupingPattern = Pattern.compile("^(.*(Step|Time|Cycle).*)|(^$)$");
        Pattern stringPattern = Pattern.compile("^(([-+])?\\d*[.]?\\d*\\s+){2}([-+])?\\d*[.]?\\d*\\s*$");

        return StreamEx.of(Try.of(() -> Files.lines(path))
                .getOrElseThrow(((Function<Throwable, IllegalStateException>) IllegalStateException::new)))
                .groupRuns((prev, next) -> !groupingPattern.matcher(prev).matches() &&
                        !groupingPattern.matcher(next).matches())
                .filter(list -> list.size() > 1)
                .map(list -> list.stream()
                        .filter(string -> stringPattern.matcher(string).matches())
                        .map(line -> new DataObject(Double.parseDouble(line.split("([\\s])+")[1]),
                                Double.parseDouble(line.split("([\\s])+")[2])))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }
}
