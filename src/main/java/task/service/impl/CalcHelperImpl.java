package task.service.impl;

import io.vavr.control.Try;
import org.springframework.stereotype.Service;
import task.data.Average;
import task.data.CycleData;
import task.data.Extremum;
import task.data.Result;
import task.service.CalcHelper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalcHelperImpl implements CalcHelper {

    @Override
    public int getMaxCycleCount(List<Result> results) {
        return results.stream()
                .map(Result::getCycleData)
                .map(List::size)
                .mapToInt(v -> v)
                .max().orElse(0);
    }

    @Override
    public List<Average> getListAverage(List<Result> results) {

        return results.stream()
                .map(Result::getCycleData)
                .map(listCycleData -> new Average(
                        getExtremumValue(listCycleData, Extremum.Max),
                        getExtremumValue(listCycleData, Extremum.Min)
                ))
                .collect(Collectors.toList());
    }

    private Double getExtremumValue(List<CycleData> listCycleData, Extremum extremum) {
        return Try.of(() -> listCycleData.stream()
                .map(extremum == Extremum.Max ? CycleData::getMaxPotential : CycleData::getMinPotential)
                .mapToDouble(Double::parseDouble)
                .average().orElseGet(null)).getOrNull();
    }
}
