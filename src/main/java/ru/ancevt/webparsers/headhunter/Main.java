package ru.ancevt.webparsers.headhunter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import ru.ancevt.util.project.ProjectProperties;
import ru.ancevt.util.fs.SimpleFileReader;
import ru.ancevt.util.args.Args;
import ru.ancevt.util.fs.FileBackuper;
import ru.ancevt.util.Range;
import ru.ancevt.webdatagrabber.config.Config;
import ru.ancevt.webdatagrabber.config.ConfigException;
import ru.ancevt.webdatagrabber.grabber.Grabber;
import ru.ancevt.webdatagrabber.log.Log;
import ru.ancevt.webdatagrabber.output.OutputCollection;
import ru.ancevt.webparsers.headhunter.api.CompanyListRequester;
import ru.ancevt.webparsers.headhunter.api.CompanyRequester;
import ru.ancevt.webparsers.headhunter.api.VacancyListRequester;
import ru.ancevt.webparsers.headhunter.api.VacancyRequester;
import ru.ancevt.webparsers.headhunter.ds.Company;
import ru.ancevt.webparsers.headhunter.ds.Vacancy;
import ru.ancevt.webparsers.headhunter.output.JsonFileCompanyOutput;
import ru.ancevt.webparsers.headhunter.output.JsonFileVacancyOutput;
import ru.ancevt.webparsers.headhunter.output.PostgreSqlCompanyOutput;
import ru.ancevt.webparsers.headhunter.output.PostgreSqlVacancyOutput;
import ru.ancevt.webparsers.headhunter.output.SqLiteCompanyOutput;
import ru.ancevt.webparsers.headhunter.output.SqLiteVacancyOutput;
import ru.ancevt.webparsers.headhunter.output.TextFileCompanyOutput;
import ru.ancevt.webparsers.headhunter.output.TextFileVacancyOutput;
import ru.ancevt.webparsers.headhunter.output.XlsFileCompanyOutput;
import ru.ancevt.webparsers.headhunter.output.XlsFileVacancyOutput;

/**
 * @author ancevt
 */
public class Main {

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        new Main(new Args(args));

    }

    private static final String CONFIG_FILE = "hhparser.conf";

    private final HHConfig config;

    public Main(Args args) throws FileNotFoundException, IOException, InterruptedException {

        String build = null;
        if (args.contains("--version")) {
            build = SimpleFileReader.readUtf8(
                getClass().getClassLoader().getResourceAsStream("build.txt")
            ).trim();
            System.out.println(ProjectProperties.getNameVersion() + "." + build + '\n' + ProjectProperties.getDescription());
            System.exit(0);
        }

        if (args.contains("--version-dirname")) {
            try {
                build = SimpleFileReader.readUtf8(
                    getClass().getClassLoader().getResourceAsStream("build.txt")
                ).trim();
                System.out.println(
                    ProjectProperties.getDirname()
                    + "-"
                    + ProjectProperties.getVersion()
                    + "."
                    + build
                );
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.exit(0);
        }

        config = loadConfig();

        argsToConfig(args, config);

        try {
            config.validate();
        } catch (ConfigException ex) {
            Log.getLogger().error(ex.getMessage(), ex);
            System.exit(1);
        }

        Log.getLogger().info("Starting with config:\n" + config.getIni().stringify());

        final Range pageRangeVacancies = new Range(config.getVacanciesPageRange(), true);
        final Range pageRangeCompanies = new Range(config.getCompaniesPageRange(), true);

        final String grabber = config.getRobot();
        switch (grabber) {
            case Mode.VACANCIES:
                startVacancies(pageRangeVacancies);
                break;
            case Mode.COMPANIES:
                startCompanies(pageRangeCompanies);
                break;
            default:
                Log.getLogger().error(String.format("Unknown robot in config \"%s\"", grabber));
        }

        Log.getLogger().info("Exiting");
    }

    private void startVacancies(final Range pageRange) throws IOException {
        final boolean optText = config.output().textFile().isEnabled();
        final boolean optJson = config.output().jsonFile().isEnabled();
        final boolean optXls = config.output().xlsFile().isEnabled();
        final boolean optSqLite = config.output().sqLite().isEnabled();
        final boolean optPostgreSql = config.output().postgreSql().isEnabled();

        try (final OutputCollection outputCollection = new OutputCollection()) {
            if (optText) {
                final TextFileVacancyOutput fileVacancyOutput = new TextFileVacancyOutput(config.getVacanciesInfoHtml());
                File outputFile = new File(config.output().textFile().getVacanciesFile());

                if (config.output().textFile().getBackupFiles()) {
                    outputFile = FileBackuper.getBackupOf(outputFile);
                }

                fileVacancyOutput.setFile(outputFile);
                outputCollection.add(fileVacancyOutput);
            }

            if (optJson) {
                final JsonFileVacancyOutput jsonFileVacancyOutput = new JsonFileVacancyOutput(
                    config.getVacanciesInfoHtml(),
                    config.output().jsonFile().getIndentation()
                );
                File outputFile = new File(config.output().jsonFile().getVacanciesFile());

                if (config.output().jsonFile().getBackupFiles()) {
                    outputFile = FileBackuper.getBackupOf(outputFile);
                }
                jsonFileVacancyOutput.setFile(outputFile);
                outputCollection.add(jsonFileVacancyOutput);
            }

            if (optXls) {
                final XlsFileVacancyOutput output = new XlsFileVacancyOutput(
                    config.getVacanciesInfoHtml(),
                    config.output().xlsFile().getRowsPerSheet()
                );
                File outputFile = new File(config.output().xlsFile().getVacanciesFile());

                if (config.output().xlsFile().getBackupFiles()) {
                    outputFile = FileBackuper.getBackupOf(outputFile);
                }
                output.setFile(outputFile);
                outputCollection.add(output);
            }

            if (optSqLite) {
                final File outputFile = new File(config.output().sqLite().getSqLiteFile());
                final SqLiteVacancyOutput output = new SqLiteVacancyOutput(outputFile);
                outputCollection.add(output);
            }

            if (optPostgreSql) {
                final PostgreSqlVacancyOutput output = new PostgreSqlVacancyOutput(
                    config.output().postgreSql().getHost(),
                    config.output().postgreSql().getPort(),
                    config.output().postgreSql().getDbName(),
                    config.output().postgreSql().getUsername(),
                    config.output().postgreSql().getPassword()
                );
                outputCollection.add(output);
            }

            final Grabber<Vacancy> robot = new Grabber(config, outputCollection, VacancyListRequester.class, VacancyRequester.class);

            final String idRanges = config.getVacanciesIdRanges();
            if (!idRanges.isEmpty()) {
                final String[] s = idRanges.split(",");
                final List<Range> idRangesList = new ArrayList<>();
                for (String item : s) {
                    idRangesList.add(new Range(item, false));
                }
                robot.startByIdRanges(idRangesList);
            } else {
                robot.startByPageRange(pageRange.getFrom(), pageRange.getTo());
            }
        }
    }

    private void startCompanies(final Range pageRange) throws IOException {
        final boolean optText = config.output().textFile().isEnabled();
        final boolean optJson = config.output().jsonFile().isEnabled();
        final boolean optXls = config.output().xlsFile().isEnabled();
        final boolean optSqLite = config.output().sqLite().isEnabled();
        final boolean optPostgreSql = config.output().postgreSql().isEnabled();

        try (final OutputCollection outputCollection = new OutputCollection()) {
            if (optText) {
                final TextFileCompanyOutput fileCompanyOutput = new TextFileCompanyOutput(config.getCompaniesInfoHtml());
                File outputFile = new File(config.output().textFile().getCompaniesFile());

                if (config.output().textFile().getBackupFiles()) {
                    outputFile = FileBackuper.getBackupOf(outputFile);
                }

                fileCompanyOutput.setFile(outputFile);
                outputCollection.add(fileCompanyOutput);
            }

            if (optJson) {
                final JsonFileCompanyOutput jsonFileCompanyOutput = new JsonFileCompanyOutput(
                    config.getCompaniesInfoHtml(),
                    config.output().jsonFile().getIndentation()
                );
                File outputFile = new File(config.output().jsonFile().getCompaniesFile());

                if (config.output().jsonFile().getBackupFiles()) {
                    outputFile = FileBackuper.getBackupOf(outputFile);
                }
                jsonFileCompanyOutput.setFile(outputFile);
                outputCollection.add(jsonFileCompanyOutput);
            }

            if (optXls) {
                final XlsFileCompanyOutput output = new XlsFileCompanyOutput(
                    config.getCompaniesInfoHtml(),
                    config.output().xlsFile().getRowsPerSheet()
                );
                File outputFile = new File(config.output().xlsFile().getCompaniesFile());

                if (config.output().xlsFile().getBackupFiles()) {
                    outputFile = FileBackuper.getBackupOf(outputFile);
                }
                output.setFile(outputFile);
                outputCollection.add(output);
            }

            if (optSqLite) {
                final File outputFile = new File(config.output().sqLite().getSqLiteFile());
                final SqLiteCompanyOutput output = new SqLiteCompanyOutput(outputFile);
                outputCollection.add(output);
            }

            if (optPostgreSql) {
                final PostgreSqlCompanyOutput output = new PostgreSqlCompanyOutput(
                    config.output().postgreSql().getHost(),
                    config.output().postgreSql().getPort(),
                    config.output().postgreSql().getDbName(),
                    config.output().postgreSql().getUsername(),
                    config.output().postgreSql().getPassword()
                );
                outputCollection.add(output);
            }

            final Grabber<Company> robot = new Grabber<>(config, outputCollection, CompanyListRequester.class, CompanyRequester.class);
            final String idRanges = config.getCompaniesIdRanges();
            if (!idRanges.isEmpty()) {
                final String[] s = idRanges.split(",");
                final List<Range> idRangesList = new ArrayList<>();
                for (String item : s) {
                    idRangesList.add(new Range(item, false));
                }
                robot.startByIdRanges(idRangesList);
            } else {
                robot.startByPageRange(pageRange.getFrom(), pageRange.getTo());
            }
        }
    }

    private HHConfig loadConfig() throws FileNotFoundException, IOException {
        final URL url = getClass().getClassLoader().getResource(CONFIG_FILE);

        if (url != null) {
            if (Log.getLogger().isDebugEnabled()) {
                Log.getLogger().debug("Loading config from resources");
            }

            final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
            return new HHConfig(SimpleFileReader.readUtf8(inputStream));
        } else {
            if (Log.getLogger().isDebugEnabled()) {
                Log.getLogger().debug("Loading config from application directory");
            }

            final String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            if (path.endsWith(".jar")) {
                final File f = new File(path).getParentFile();
                final File file = new File(f.getAbsoluteFile() + "/" + CONFIG_FILE);
                return new HHConfig(SimpleFileReader.readUtf8(file));
            }

            // for run from IDE
            return new HHConfig(SimpleFileReader.readUtf8(new File(CONFIG_FILE)));
        }
    }

    private static void argsToConfig(Args args, Config config) {
        for (int i = 0; i < args.size(); i++) {
            final String element = args.getString(i);
            try {

                if (element.startsWith("-")) {
                    continue;
                }

                final String[] s = element.split("=", 2);
                final String left = s[0];
                final String right = s[1];

                final String[] s1 = left.split("\\.");
                final String section = s1[0];
                final String key = s1[1];

                final String value = right.replaceAll("\"", "");

                config.getIni().set(section, key, value);
            } catch (Exception ex) {
                Log.getLogger().error("invalid cli config modification: " + element);
            }
        }

        if (args.contains("--output-directory", "-o")) {
            final String path = args.getString("--output-directory", "-o");

            config.getIni().set("TextFile", "vacancies_file", path + "/vacancies.txt");
            config.getIni().set("TextFile", "companies_file", path + "/companies.txt");
            config.getIni().set("JSONFile", "vacancies_file", path + "/vacancies.json");
            config.getIni().set("JSONFile", "companies_file", path + "/companies.json");
            config.getIni().set("XLSFile", "vacancies_file", path + "/vacancies.xls");
            config.getIni().set("XLSFile", "companies_file", path + "/companies.xls");
            config.getIni().set("SQLite", "sqlite_file", path + "/hh.db");
        }
    }

}

class Mode {

    public static final String VACANCIES = "Vacancies";
    public static final String COMPANIES = "Companies";
}
