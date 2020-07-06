package ru.ancevt.webparsers.headhunter.output;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import ru.ancevt.webdatagrabber.log.Log;
import ru.ancevt.webdatagrabber.output.IOutput;
import ru.ancevt.webparsers.headhunter.ds.Company;

/**
 *
 * @author ancevt
 */
public class SqLiteCompanyOutput implements IOutput<Company> {

    private final File file;
    private SqLiteHelper helper;

    public SqLiteCompanyOutput(File file) {
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
    public void output(Company company) {
        if (company == null) {
            return;
        }

        if (!helper.isDataBaseFileExists()) {
            helper.createDataBase();
        }

        try {
            helper.createCompaniesTable();
        } catch (IOException ex) {
            Log.getLogger().error(ex.getMessage(), ex);
        }

        try {
            helper.insertCompany(company);
        } catch (SQLException | IOException ex) {
            Log.getLogger().error("company: " + company.toString(), ex);
        }
    }

    @Override
    public void close() throws IOException {
        // empty
    }
}
