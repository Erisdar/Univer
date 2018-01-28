package task;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import task.service.CalcManager;
import task.service.FileService;
import task.service.JChooserService;

public class Main {

    public static void main(String[] args) {

        ApplicationContext context = new AnnotationConfigApplicationContext(task.config.AppConfig.class);
        FileService fileService = context.getBean(FileService.class);
        CalcManager calcManager = context.getBean(CalcManager.class);
        JChooserService jChooserService = context.getBean(JChooserService.class);

        fileService.writeFile(calcManager.calculateValues(jChooserService.chooseFolder()));
    }
}
