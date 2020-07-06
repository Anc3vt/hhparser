package ru.ancevt.webparsers.headhunter.output;

import java.io.IOException;
import java.sql.SQLException;
import ru.ancevt.webdatagrabber.log.Log;
import ru.ancevt.webdatagrabber.output.IOutput;
import ru.ancevt.webparsers.headhunter.ds.Vacancy;

/**
 *
 * @author ancevt
 */
public class PostgreSqlVacancyOutput implements IOutput<Vacancy> {

    private static final String DB_CONNECTION = "jdbc:postgresql://";

    private final PostgreSqlHelper helper;

    public PostgreSqlVacancyOutput(String host, int port, String dbName, String username, String password) throws IOException {
        final StringBuilder s = new StringBuilder();
        s.append(DB_CONNECTION);
        s.append(host).append(':');
        s.append(port).append('/');
        s.append(dbName).append('?');
        s.append("user=").append(username).append('&');
        s.append("password=").append(password);

        helper = new PostgreSqlHelper(s.toString());
        //jdbc:postgresql://host:port/database
        //"jdbc:postgresql://localhost/test?user=fred&password=secret&ssl=true";
        
        try {
            helper.createVacanciesTable();
        } catch (IOException ex) {
            Log.getLogger().error(ex.getMessage(), ex);
        }
    }

    @Override
    public void output(Vacancy vacancy) {
        try {
            helper.insertVacancy(vacancy);
        } catch (SQLException | IOException ex) {
            Log.getLogger().error("company: " + vacancy.toString(), ex);
        }
    }

    @Override
    public void close() throws IOException {

    }

}
