package task.service.impl;

import io.vavr.control.Try;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import task.data.Average;
import task.data.Result;
import task.service.CalcHelper;
import task.service.DocService;
import task.service.FileService;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;

@Service
public class DocServiceImpl implements DocService {

    @Autowired
    FileService fileService;

    @Autowired
    CalcHelper calcHelper;

    @Override
    public void writeToTable(List<Result> results, String filePath) {
        //Create Excel list and set settings
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("new sheet");
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        //Get max cycles count from results
        int maxCyclesCount = calcHelper.getMaxCycleCount(results);

        //Create titles: "Cycle", "U max", "U min", "I".
        Row firstRow = sheet.createRow(0);
        for (int i = 1, cycleNum = 1; i < maxCyclesCount * 2; i += 2, cycleNum++) {
            sheet.addMergedRegion(new CellRangeAddress(firstRow.getRowNum(), firstRow.getRowNum(), i, i + 1));
            firstRow.createCell(i).setCellValue("Cycle" + cycleNum);
        }
        Row secondRow = sheet.createRow(1);
        secondRow.createCell(0).setCellValue("I");
        for (int i = 0; i < maxCyclesCount; i++) {
            secondRow.createCell(secondRow.getLastCellNum()).setCellValue("U max");
            secondRow.createCell(secondRow.getLastCellNum()).setCellValue("U min");
        }

        // Create title: "Average"
        sheet.addMergedRegion(new CellRangeAddress(firstRow.getRowNum(), firstRow.getRowNum(), firstRow.getLastCellNum() + 1, firstRow.getLastCellNum() + 3));
        firstRow.createCell(firstRow.getLastCellNum() + 1).setCellValue("Average");
        secondRow.createCell(secondRow.getLastCellNum()).setCellValue("U max");
        secondRow.createCell(secondRow.getLastCellNum()).setCellValue("U min");
        secondRow.createCell(secondRow.getLastCellNum()).setCellValue("Aver");

        // Get data from results
        results.forEach(result -> {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(result.getAmperage());
            result.getCycleData().forEach(cycleData -> {
                row.createCell(row.getLastCellNum()).setCellValue(Double.parseDouble(cycleData.getMaxPotential()));
                row.createCell(row.getLastCellNum()).setCellValue(Double.parseDouble(cycleData.getMinPotential()));
            });
        });

        // Get average data
        List<Average> listAverages = calcHelper.getListAverage(results);

        int lastCellNum = maxCyclesCount * 2 + 1;
        for (int i = 2, listVal = 0; listVal < listAverages.size(); i++, listVal++) {
            Row row = sheet.getRow(i);
            if (Optional.ofNullable(listAverages.get(listVal).getAverage()).isPresent()) {
                row.createCell(lastCellNum).setCellValue(listAverages.get(listVal).getMaxValue());
                row.createCell(row.getLastCellNum()).setCellValue(listAverages.get(listVal).getMinValue());
                row.createCell(row.getLastCellNum()).setCellValue(listAverages.get(listVal).getAverage());
            }
        }

        // Apply settings to all rows
        for (Row row : sheet) {
            for (Cell cell : row) {
                cell.setCellStyle(style);
            }
        }

        // Write excel file
        Try.withResources(() -> new FileOutputStream(new File(filePath))).of(fileOutputStream -> {
            wb.write(fileOutputStream);
            return fileOutputStream;
        });
    }

}
