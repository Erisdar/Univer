package task.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import task.service.FileService;
import task.service.JChooserService;

import javax.swing.*;
import java.io.File;
import java.util.Optional;

@Service
public class JChooserServiceImpl implements JChooserService {

    @Autowired
    private FileService fileService;

    private JFileChooser chooser = new JFileChooser();

    @Override
    public File chooseFolder() {

        chooser.setCurrentDirectory(new File("."));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.showOpenDialog(null);

        return Optional.ofNullable(chooser.getSelectedFile()).orElse(null);
    }

}
