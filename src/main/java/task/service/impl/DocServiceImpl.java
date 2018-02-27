package task.service.impl;

import io.vavr.control.Try;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import task.CycleData;
import task.Result;
import task.service.DocService;
import task.service.FileService;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
public class DocServiceImpl implements DocService {

    @Autowired
    FileService fileService;

    @Override
    public void writeToTable(List<Result> results, String filePath) {

        List<Row> rowsWithData = new ArrayList<>();

        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("new sheet");
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        Row firstRow = sheet.createRow(0);
        Row secondRow = sheet.createRow(1);
        secondRow.createCell(0).setCellValue("I");

        Integer maxCyclesNum = results.stream()
                .map(Result::getStepData)
                .map(List::size)
                .mapToInt(v -> v)
                .max().getAsInt();


        //Создаём подписи "Cycle" для всех Cycles.
        for (int i = 1, cycleNum = 1; i < maxCyclesNum * 2; i += 2, cycleNum++) {
            sheet.addMergedRegion(new CellRangeAddress(firstRow.getRowNum(), firstRow.getRowNum(), i, i + 1));
            firstRow.createCell(i).setCellValue("Cycle" + cycleNum);
        }

        for (int i = 0; i < maxCyclesNum; i++) {
            secondRow.createCell(secondRow.getLastCellNum()).setCellValue("U max");
            secondRow.createCell(secondRow.getLastCellNum()).setCellValue("U min");
        }

        // Создаём подпись "Среднее"
        sheet.addMergedRegion(new CellRangeAddress(firstRow.getRowNum(), firstRow.getRowNum(), firstRow.getLastCellNum() + 1, firstRow.getLastCellNum() + 2));
        firstRow.createCell(firstRow.getLastCellNum() + 1).setCellValue("Average");
        secondRow.createCell(secondRow.getLastCellNum()).setCellValue("U max");
        secondRow.createCell(secondRow.getLastCellNum()).setCellValue("U min");

        results.forEach(result -> {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(result.getAmperage());
            result.getStepData().forEach(stepData -> {
                row.createCell(row.getLastCellNum()).setCellValue(Double.parseDouble(stepData.getMaxPotential()));
                row.createCell(row.getLastCellNum()).setCellValue(Double.parseDouble(stepData.getMinPotential()));
            });
        });

        List<OptionalDouble> listMaxAverages = results.stream()
                .map(Result::getStepData)
                .map(step -> step.stream()
                        .map(CycleData::getMaxPotential)
                        .mapToDouble(Double::parseDouble)
                        .average())
                .collect(Collectors.toList());

        List<OptionalDouble> listMinAverages = results.stream()
                .map(Result::getStepData)
                .map(step -> step.stream()
                        .map(CycleData::getMinPotential)
                        .mapToDouble(Double::parseDouble)
                        .average())
                .collect(Collectors.toList());


        int lastCellNum = maxCyclesNum * 2 + 1;
        for (int i = 2, listVal = 0; listVal < listMaxAverages.size(); i++, listVal++) {
            Row row = sheet.getRow(i);
            row.createCell(lastCellNum).setCellValue(listMaxAverages.get(listVal).getAsDouble());
            row.createCell(row.getLastCellNum()).setCellValue(listMinAverages.get(listVal).getAsDouble());
        }

        for (Row row : sheet) {
            for (Cell cell : row) {
                cell.setCellStyle(style);
            }
        }

        Try.withResources(() -> new FileOutputStream(new File(filePath))).of(fileOutputStream -> {
            wb.write(fileOutputStream);
            return fileOutputStream;
        });

    }

}
