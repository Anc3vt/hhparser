package ru.ancevt.webparsers.headhunter.sql;

import java.io.IOException;
import ru.ancevt.util.sql.QueryStorage;

/**
 * @author ancevt
 */
public class SqliteQueryStorage extends QueryStorage {
    
    private static final String FILE_NAME = "sqlite-queries.sql";
    
    public static final String CREATE_TABLE_COMPANIES = "create-table-companies";
    public static final String CREATE_TABLE_VACANCIES = "create-table-vacancies";
    public static final String INSERT_COMPANY = "insert-company";
    public static final String INSERT_VACANCY = "insert-vacancy";
    public static final String UPDATE_COMPANY = "update-company";
    public static final String UPDATE_VACANCY = "update-vacancy";
    public static final String IS_COMPANY_EXISTS = "is-company-exists";
    public static final String IS_VACANCY_EXISTS = "is-vacancy-exists";
    
    public final void load() throws IOException {
        super.load(getClass().getClassLoader().getResourceAsStream(FILE_NAME));
    }
}
