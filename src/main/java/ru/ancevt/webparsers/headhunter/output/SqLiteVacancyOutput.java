package ru.ancevt.webparsers.headhunter.output;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import ru.ancevt.webdatagrabber.log.Log;
import ru.ancevt.webdatagrabber.output.IOutput;
import ru.ancevt.webparsers.headhunter.ds.Vacancy;

/**
 *
 * @author ancevt
 */
public class SqLiteVacancyOutput implements IOutput<Vacancy> {

    private final File file;
    private SqLiteHelper helper;

    public SqLiteVacancyOutput(File file) {
        this.file = file;
        try {
            helper = new SqLiteHelper(file);
        } catch (IOException ex) {
            Log.getLogger().error(ex.getMessage(), ex);
        }

        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

    }

    @Override
    public void output(Vacancy vacancy) {
        if (vacancy == null) {
            return;
        }

        if (!helper.isDataBaseFileExists()) {
            helper.createDataBase();
        }

        try {
            helper.createVacanciesTable();
        } catch (IOException ex) {
            Log.getLogger().error(ex.getMessage(), ex);
        }

        try {
            helper.insertVacancy(vacancy);
        } catch (SQLException | IOException ex) {
            Log.getLogger().error("vacancy: " + vacancy.toString(), ex);
        }
    }

    @Override
    public void close() throws IOException {
        Log.getLogger().info("Result file: " + file.getAbsolutePath());
    }
}
