package task.data;

import io.vavr.control.Try;
import lombok.Data;

@Data
public class Average {
    private Double maxValue;
    private Double minValue;
    private Double average;

    public Average(Double maxValue, Double minValue) {
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.average = Try.of(() -> (minValue + maxValue) / 2).getOrNull();
    }
}
