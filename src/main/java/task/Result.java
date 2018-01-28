package task;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;

@Data
@AllArgsConstructor
public class Result {

    private Path file;
    private Double amperage;
    private String maxPotential;
    private String minPotential;

}
