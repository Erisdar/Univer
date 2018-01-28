package task.service.impl;

import io.vavr.control.Try;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import task.Result;
import task.service.DocService;
import task.service.FileService;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.List;

@Service
public class DocServiceImpl implements DocService {

    @Autowired
    FileService fileService;

    @Override
    public void writeToTable(List<Result> results, String filePath) {

        XWPFDocument document = new XWPFDocument();
        XWPFTable table = document.createTable();
        table.setCellMargins(0, 1000, 0, 1000);
        XWPFTableRow firstTableRow = table.getRow(0);
        firstTableRow.getCell(0).setText("I");
        firstTableRow.addNewTableCell().setText("Emax");
        firstTableRow.addNewTableCell().setText("Emin");

        results.forEach(result -> {
            XWPFTableRow tableRow = table.createRow();
            tableRow.getCell(0).setText(result.getAmperage().toString().replace(".", ","));
            tableRow.getCell(1).setText(result.getMaxPotential());
            tableRow.getCell(2).setText(result.getMinPotential());
        });

        Try.run(() -> document.write(new BufferedOutputStream(new FileOutputStream(new File(filePath)), 128*1024)));
        fileService.writeDirectoryToFile(Paths.get(filePath).getParent().toString());

    }
}
