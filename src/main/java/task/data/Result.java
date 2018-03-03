package task.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;
import java.util.List;

@Data
@AllArgsConstructor
public class Result {

    private Path file;
    private Double amperage;
    private List<CycleData> cycleData;

}
