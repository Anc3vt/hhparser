package ru.ancevt.webparsers.headhunter;

import ru.ancevt.util.ini.Ini;
import ru.ancevt.util.ini.IniSection;
import ru.ancevt.webdatagrabber.config.Config;
import ru.ancevt.webdatagrabber.config.ConfigException;

/**
 * @author ancevt
 */
public class HHConfig extends Config {

    private static final String HOST = "host"; //sec
    private static final String DEFAULT_HOST = "hh.ru";

    private static final String ROBOT = "robot";
    private static final String DEFAULT_ROBOT = "Vacancies";

    private static final String SEC_VACANCIES = "Vacancies";
    private static final String SEC_COMPANIES = "Companies";

    private static final String KEY_PHRASE = "key_phrase";
    private static final String DEFAULT_KEY_PHRASE = "";

    private static final String ONLY_WITH_SALARY = "only_with_salary";
    private static final boolean DEFAULT_ONLY_WITH_SALARY = false;

    private static final String SALARY = "salary";
    private static final String DEFAULT_SALARY = "";

    private static final String PAGE_RANGE = "page_range";
    private static final String DEFAULT_PAGE_RANGE = "1-100";

    private static final String ID_RANGES = "id_ranges";
    private static final String DEFAULT_ID_RANGES = "";

    private static final String AREAS = "areas";
    private static final String DEFAULT_AREAS = "Москва";

    private static final String AREA = "area";
    private static final String DEFAULT_AREA = "Москва";

    private static final String INFO_HTML = "info_html";
    private static final boolean DEFAULT_INFO_HTML = false;

    private static final String READ_IMAGE = "read_image";
    private static final boolean DEFAULT_READ_IMAGE = true;

    public HHConfig(String configSource) {
        super(configSource);
    }

    public String getHost() {
        return ini.getString(SEC_COMMON, HOST, DEFAULT_HOST);
    }

    public String getRobot() {
        return ini.getString(SEC_COMMON, ROBOT, DEFAULT_ROBOT);
    }

    public boolean getCompaniesReadImage() {
        return ini.getBoolean(SEC_COMPANIES, READ_IMAGE, DEFAULT_READ_IMAGE);
    }

    public String getCompaniesArea() {
        return ini.getString(SEC_COMPANIES, AREA, DEFAULT_AREA);
    }

    public String getCompaniesKeyPhrase() {
        return ini.getString(SEC_COMPANIES, KEY_PHRASE, DEFAULT_KEY_PHRASE);
    }

    public String getCompaniesPageRange() {
        return ini.getString(SEC_COMPANIES, PAGE_RANGE, DEFAULT_PAGE_RANGE);
    }

    public boolean getCompaniesInfoHtml() {
        return ini.getBoolean(SEC_COMPANIES, INFO_HTML, DEFAULT_INFO_HTML);
    }

    public String getCompaniesIdRanges() {
        return ini.getString(SEC_COMPANIES, ID_RANGES, DEFAULT_ID_RANGES);
    }

    public boolean getVacanciesOnlyWithSalary() {
        return ini.getBoolean(SEC_VACANCIES, ONLY_WITH_SALARY, DEFAULT_ONLY_WITH_SALARY);
    }

    public String getVacanciesSalary() {
        return ini.getString(SEC_VACANCIES, SALARY, DEFAULT_SALARY);
    }

    public String getVacanciesAreas() {
        return ini.getString(SEC_VACANCIES, AREAS, DEFAULT_AREAS);
    }

    public boolean getVacanciesInfoHtml() {
        return ini.getBoolean(SEC_VACANCIES, INFO_HTML, DEFAULT_INFO_HTML);
    }

    public String getVacanciesKeyPhrase() {
        return ini.getString(SEC_VACANCIES, KEY_PHRASE, DEFAULT_KEY_PHRASE);
    }

    public String getVacanciesPageRange() {
        return ini.getString(SEC_VACANCIES, PAGE_RANGE, DEFAULT_PAGE_RANGE);
    }

    public String getVacanciesIdRanges() {
        return ini.getString(SEC_VACANCIES, ID_RANGES, DEFAULT_ID_RANGES);
    }

    private Output output;

    public Output output() {
        if (this.output == null) {
            this.output = new Output(ini);
        }
        return this.output;
    }

    @Override
    public void validate() throws ConfigException {
        if (!ini.hasSection(SEC_COMMON)) {
            throw new ConfigException("Section [" + SEC_COMMON + "] not found");
        } else {
            final IniSection s = ini.getSection(SEC_COMMON);
            if (!s.hasLine(HOST)) {
                throw new ConfigException("property \"" + HOST + "\" not found in section [" + SEC_COMMON + "]");
            } else if (!s.hasLine(ROBOT)) {
                throw new ConfigException("property \"" + ROBOT + "\" not found in section [" + SEC_COMMON + "]");
            }
        }

        if (!ini.hasSection(SEC_VACANCIES)) {
            throw new ConfigException("Section [" + SEC_VACANCIES + "] not found");
        }

        if (!ini.hasSection(SEC_COMPANIES)) {
            throw new ConfigException("Section [" + SEC_COMPANIES + "] not found");
        }
    }

    public class Output {

        private final TextFile textFile;
        private final JSONFile jsonFile;
        private final XLSFile xlsFile;
        private final SQLite sqLite;
        private final PostgreSql postgresQl;

        public Output(Ini ini) {
            textFile = new TextFile(ini);
            jsonFile = new JSONFile(ini);
            xlsFile = new XLSFile(ini);
            sqLite = new SQLite(ini);
            postgresQl = new PostgreSql(ini);
        }

        public TextFile textFile() {
            return textFile;
        }

        public JSONFile jsonFile() {
            return jsonFile;
        }

        public XLSFile xlsFile() {
            return xlsFile;
        }

        public SQLite sqLite() {
            return sqLite;
        }

        public PostgreSql postgreSql() {
            return postgresQl;
        }

        public class TextFile {

            private static final String SEC_TEXTFILE = "TextFile";

            private static final String ENABLED = "enabled";
            private static final boolean DEFAULT_ENABLED = false;

            private static final String VACANCIES_FILE = "vacancies_file";
            private static final String DEFAULT_VACANCIES_FILE = "out/vacancies.txt";

            private static final String COMPANIES_FILE = "companies_file";
            private static final String DEFAULT_COMPANIES_FILE = "out/companies.txt";

            private static final String BACKUP_FILES = "backup_files";
            private static final boolean DEFAULT_BACKUP_FILES = false;

            private final Ini ini;

            public TextFile(Ini ini) {
                this.ini = ini;
            }

            public boolean isEnabled() {
                return ini.getBoolean(SEC_TEXTFILE, ENABLED, DEFAULT_ENABLED);
            }

            public String getVacanciesFile() {
                return ini.getString(SEC_TEXTFILE, VACANCIES_FILE, DEFAULT_VACANCIES_FILE);
            }

            public String getCompaniesFile() {
                return ini.getString(SEC_TEXTFILE, COMPANIES_FILE, DEFAULT_COMPANIES_FILE);
            }

            public boolean getBackupFiles() {
                return ini.getBoolean(SEC_TEXTFILE, BACKUP_FILES, DEFAULT_BACKUP_FILES);
            }

        }

        public class JSONFile {

            private static final String SEC_JSONFILE = "JSONFile";

            private static final String ENABLED = "enabled";
            private static final boolean DEFAULT_ENABLED = false;

            private static final String VACANCIES_FILE = "vacancies_file";
            private static final String DEFAULT_VACANCIES_FILE = "out/vacancies.json";

            private static final String COMPANIES_FILE = "companies_file";
            private static final String DEFAULT_COMPANIES_FILE = "out/companies.json";

            private static final String BACKUP_FILES = "backup_files";
            private static final boolean DEFAULT_BACKUP_FILES = false;

            private static final String INDENTATION = "indentation";
            private static final boolean DEFAULT_INDENTATION = false;

            private final Ini ini;

            public JSONFile(Ini ini) {
                this.ini = ini;

            }

            public boolean isEnabled() {
                return ini.getBoolean(SEC_JSONFILE, ENABLED, DEFAULT_ENABLED);
            }

            public String getVacanciesFile() {
                return ini.getString(SEC_JSONFILE, VACANCIES_FILE, DEFAULT_VACANCIES_FILE);
            }

            public String getCompaniesFile() {
                return ini.getString(SEC_JSONFILE, COMPANIES_FILE, DEFAULT_COMPANIES_FILE);
            }

            public boolean getBackupFiles() {
                return ini.getBoolean(SEC_JSONFILE, BACKUP_FILES, DEFAULT_BACKUP_FILES);
            }

            public boolean getIndentation() {
                return ini.getBoolean(SEC_JSONFILE, INDENTATION, DEFAULT_INDENTATION);
            }

        }

        public class XLSFile {

            private static final String SEC_XLSFILE = "XLSFile";

            private static final String ENABLED = "enabled";
            private static final boolean DEFAULT_ENABLED = false;

            private static final String VACANCIES_FILE = "vacancies_file";
            private static final String DEFAULT_VACANCIES_FILE = "out/vacancies.xls";

            private static final String COMPANIES_FILE = "companies_file";
            private static final String DEFAULT_COMPANIES_FILE = "out/companies.xls";

            private static final String BACKUP_FILES = "backup_files";
            private static final boolean DEFAULT_BACKUP_FILES = false;

            private static final String ROWS_PER_SHEET = "rows_per_sheet";
            private static final int DEFAULT_ROWS_PER_SHEET = 200;

            private final Ini ini;

            public XLSFile(Ini ini) {
                this.ini = ini;
            }

            public boolean isEnabled() {
                return ini.getBoolean(SEC_XLSFILE, ENABLED, DEFAULT_ENABLED);
            }

            public String getVacanciesFile() {
                return ini.getString(SEC_XLSFILE, VACANCIES_FILE, DEFAULT_VACANCIES_FILE);
            }

            public String getCompaniesFile() {
                return ini.getString(SEC_XLSFILE, COMPANIES_FILE, DEFAULT_COMPANIES_FILE);
            }

            public boolean getBackupFiles() {
                return ini.getBoolean(SEC_XLSFILE, BACKUP_FILES, DEFAULT_BACKUP_FILES);
            }

            public int getRowsPerSheet() {
                return ini.getInt(SEC_XLSFILE, ROWS_PER_SHEET, DEFAULT_ROWS_PER_SHEET);
            }
        }

        public class SQLite {

            private static final String SEC_SQLITE = "SQLite";

            private static final String ENABLED = "enabled";
            private static final boolean DEFAULT_ENABLED = false;

            private static final String SQLITE_FILE = "sqlite_file";
            private static final String DEFAULT_SQLITE_FILE = "out/hhoutput.db";

            private final Ini ini;

            public SQLite(Ini ini) {
                this.ini = ini;
            }

            public boolean isEnabled() {
                return ini.getBoolean(SEC_SQLITE, ENABLED, DEFAULT_ENABLED);
            }

            public String getSqLiteFile() {
                return ini.getString(SEC_SQLITE, SQLITE_FILE, DEFAULT_SQLITE_FILE);
            }

        }

        public class PostgreSql {

            private static final String SEC_POSTGRESQL = "PostgreSQL";

            private static final String ENABLED = "enabled";
            private static final boolean DEFAULT_ENABLED = false;

            private static final String HOST = "host";
            private static final String PORT = "port";
            private static final String DBNAME = "dbname";
            private static final String USERNAME = "username";
            private static final String PASSWORD = "password";

            private final Ini ini;

            public PostgreSql(Ini ini) {
                this.ini = ini;
            }

            public boolean isEnabled() {
                return ini.getBoolean(SEC_POSTGRESQL, ENABLED, DEFAULT_ENABLED);
            }

            public String getHost() {
                return ini.getString(SEC_POSTGRESQL, HOST);
            }

            public int getPort() {
                return ini.getInt(SEC_POSTGRESQL, PORT);
            }

            public String getUsername() {
                return ini.getString(SEC_POSTGRESQL, USERNAME);
            }

            public String getDbName() {
                return ini.getString(SEC_POSTGRESQL, DBNAME);
            }

            public String getPassword() {
                return ini.getString(SEC_POSTGRESQL, PASSWORD);
            }
        }
    }
}
