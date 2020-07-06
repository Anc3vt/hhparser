package ru.ancevt.webparsers.headhunter.output;

import java.io.IOException;
import java.sql.SQLException;
import ru.ancevt.webdatagrabber.log.Log;
import ru.ancevt.webdatagrabber.output.IOutput;
import ru.ancevt.webparsers.headhunter.ds.Company;

/**
 *
 * @author ancevt
 */
public class PostgreSqlCompanyOutput implements IOutput<Company> {

    private static final String DB_CONNECTION = "jdbc:postgresql://";

    private final PostgreSqlHelper helper;

    public PostgreSqlCompanyOutput(String host, int port, String dbName, String username, String password) throws IOException {
        final StringBuilder s = new StringBuilder();
        s.append(DB_CONNECTION);
        s.append(host).append(':');
        s.append(port).append('/');
        s.append(dbName).append('?');
        s.append("user=").append(username).append('&');
        s.append("password=").append(password);

        helper = new PostgreSqlHelper(s.toString());
        
        try {
            helper.createCompaniesTable();
        } catch (IOException ex) {
            Log.getLogger().error(ex.getMessage(), ex);
        }
    }

    @Override
    public void output(Company company) {
        try {
            helper.insertCompany(company);
        } catch (SQLException | IOException ex) {
            Log.getLogger().error("company: " + company.toString(), ex);
        }
    }

    @Override
    public void close() throws IOException {

    }

}
