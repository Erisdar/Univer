package task.service.impl;

import io.vavr.control.Try;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import task.data.CycleData;
import task.data.Result;
import task.service.DocService;
import task.service.FileService;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
public class DocServiceImpl implements DocService {

    @Autowired
    FileService fileService;

    @Override
    public void writeToTable(List<Result> results, String filePath) {

        //Создаём Excel лист и задаём настройки
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("new sheet");
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        Row secondRow = sheet.createRow(1);
        secondRow.createCell(0).setCellValue("I");

        //Считаем максимальное количество циклов
        int maxCyclesNum = results.stream()
                .map(Result::getCycleData)
                .map(List::size)
                .mapToInt(v -> v)
                .max().getAsInt();

        //Создаём подписи "Cycle", "U max", "U min" для всех Cycles.
        Row firstRow = sheet.createRow(0);
        for (int i = 1, cycleNum = 1; i < maxCyclesNum * 2; i += 2, cycleNum++) {
            sheet.addMergedRegion(new CellRangeAddress(firstRow.getRowNum(), firstRow.getRowNum(), i, i + 1));
            firstRow.createCell(i).setCellValue("Cycle" + cycleNum);
        }
        for (int i = 0; i < maxCyclesNum; i++) {
            secondRow.createCell(secondRow.getLastCellNum()).setCellValue("U max");
            secondRow.createCell(secondRow.getLastCellNum()).setCellValue("U min");
        }

        // Создаём подпись "Average"
        sheet.addMergedRegion(new CellRangeAddress(firstRow.getRowNum(), firstRow.getRowNum(), firstRow.getLastCellNum() + 1, firstRow.getLastCellNum() + 3));
        firstRow.createCell(firstRow.getLastCellNum() + 1).setCellValue("Average");
        secondRow.createCell(secondRow.getLastCellNum()).setCellValue("U max");
        secondRow.createCell(secondRow.getLastCellNum()).setCellValue("U min");
        secondRow.createCell(secondRow.getLastCellNum()).setCellValue("Aver");

        // Записываем данные
        results.forEach(result -> {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(result.getAmperage());
            result.getCycleData().forEach(cycleData -> {
                row.createCell(row.getLastCellNum()).setCellValue(Double.parseDouble(cycleData.getMaxPotential()));
                row.createCell(row.getLastCellNum()).setCellValue(Double.parseDouble(cycleData.getMinPotential()));
            });
        });

        List<OptionalDouble> listMaxAverages = results.stream()
                .map(Result::getCycleData)
                .map(cycleData -> cycleData.stream()
                        .map(CycleData::getMaxPotential)
                        .mapToDouble(Double::parseDouble)
                        .average())
                .collect(Collectors.toList());

        List<OptionalDouble> listMinAverages = results.stream()
                .map(Result::getCycleData)
                .map(step -> step.stream()
                        .map(CycleData::getMinPotential)
                        .mapToDouble(Double::parseDouble)
                        .average())
                .collect(Collectors.toList());

        int lastCellNum = maxCyclesNum * 2 + 1;
        for (int i = 2, listVal = 0; listVal < listMaxAverages.size(); i++, listVal++) {
            Row row = sheet.getRow(i);

            if (listMaxAverages.get(listVal).isPresent() && listMinAverages.get(listVal).isPresent()) {
                row.createCell(lastCellNum).setCellValue(listMaxAverages.get(listVal).getAsDouble());
                row.createCell(row.getLastCellNum()).setCellValue(listMinAverages.get(listVal).getAsDouble());
                row.createCell(row.getLastCellNum()).setCellValue(((listMaxAverages.get(listVal).getAsDouble() + listMinAverages.get(listVal).getAsDouble()) / 2));
            }
        }

        // Применяем настройки ко всем строкам
        for (Row row : sheet) {
            for (Cell cell : row) {
                cell.setCellStyle(style);
            }
        }

        // Записываем файл
        Try.withResources(() -> new FileOutputStream(new File(filePath))).of(fileOutputStream -> {
            wb.write(fileOutputStream);
            return fileOutputStream;
        });
    }

}
