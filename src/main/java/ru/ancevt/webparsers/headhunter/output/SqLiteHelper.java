package ru.ancevt.webparsers.headhunter.output;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import ru.ancevt.webdatagrabber.ds.Image;
import ru.ancevt.webdatagrabber.log.Log;
import ru.ancevt.webparsers.headhunter.ds.Address;
import ru.ancevt.webparsers.headhunter.ds.Area;
import ru.ancevt.webparsers.headhunter.ds.Company;
import ru.ancevt.webparsers.headhunter.ds.ContactInfo;
import ru.ancevt.webparsers.headhunter.ds.Salary;
import ru.ancevt.webparsers.headhunter.ds.Vacancy;
import ru.ancevt.webparsers.headhunter.sql.SqliteQueryStorage;

public class SqLiteHelper {

    private static final String DB_DRIVER = "org.sqlite.JDBC";
    private static final String DB_CONNECTION = "jdbc:sqlite:";

    private final File file;
    private final SqliteQueryStorage queryStorage;
    private boolean driverLoaded;

    public SqLiteHelper(File file) throws IOException {
        this.file = file;
        this.queryStorage = new SqliteQueryStorage();
        queryStorage.load();
    }

    public final boolean isDataBaseFileExists() {
        return file.exists();
    }

    public final void createDataBase() {
        if (isDataBaseFileExists()) {
            Log.getLogger().warn("File " + file.getAbsolutePath() + " is already exists");
        }

        final String url = DB_CONNECTION + file.getAbsolutePath();
        try (Connection conn = DriverManager.getConnection(url)) {

            if (conn != null) {
                conn.getMetaData();
                Log.getLogger().info("A new db " + file.getAbsolutePath() + " has been created.");
            }
        } catch (SQLException e) {
            Log.getLogger().error(e.getMessage(), e);
        }

    }

    public final void createCompaniesTable() throws IOException {
        executeSimpleSQL(queryStorage.getQueryString(SqliteQueryStorage.CREATE_TABLE_COMPANIES));
    }

    public final void createVacanciesTable() throws IOException {
        executeSimpleSQL(queryStorage.getQueryString(SqliteQueryStorage.CREATE_TABLE_VACANCIES));
    }

    public final void insertCompany(Company company) throws SQLException, IOException {

        if (!isCompanyExists(company.getId())) {
            final String sql = queryStorage.getQueryString(SqliteQueryStorage.INSERT_COMPANY);

            try (Connection connection = getConnection()) {
                final PreparedStatement ps = connection.prepareStatement(sql);

                int c = 1;

                ps.setLong(c++, company.getId());
                ps.setString(c++, company.getName());
                ps.setString(c++, company.getType());
                ps.setString(c++, company.getInfo());
                ps.setString(c++, company.getInfoHtml());
                ps.setString(c++, company.getArea());
                ps.setString(c++, company.getPageUrl());
                ps.setString(c++, company.getWebSite());
                ps.setBoolean(c++, company.isTrusted());

                if (company.getImage() != null) {
                    final Image image = company.getImage();
                    ps.setBytes(c++, image.getBytes());
                    ps.setString(c++, image.getAlt());
                    ps.setString(c++, image.getSource());
                    ps.setString(c++, image.getMimeType());
                } else {
                    ps.setBytes(c++, null);
                    ps.setBytes(c++, null);
                    ps.setBytes(c++, null);
                    ps.setBytes(c++, null);
                }

                ps.execute();
            }
        } else {
            final String sql = queryStorage.getQueryString(SqliteQueryStorage.UPDATE_COMPANY);

            try (Connection connection = getConnection()) {
                final PreparedStatement ps = connection.prepareStatement(sql);

                int c = 1;

                ps.setString(c++, company.getName());
                ps.setString(c++, company.getType());
                ps.setString(c++, company.getInfo());
                ps.setString(c++, company.getInfoHtml());
                ps.setString(c++, company.getArea());
                ps.setString(c++, company.getPageUrl());
                ps.setString(c++, company.getWebSite());
                ps.setBoolean(c++, company.isTrusted());

                if (company.getImage() != null) {
                    final Image image = company.getImage();
                    ps.setBytes(c++, image.getBytes());
                    ps.setString(c++, image.getAlt());
                    ps.setString(c++, image.getSource());
                    ps.setString(c++, image.getMimeType());
                } else {
                    ps.setBytes(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                }

                ps.setLong(c++, company.getId());

                ps.execute();
            }
        }
    }

    public final void insertVacancy(Vacancy vacancy) throws SQLException, IOException {

        if (!isVacancyExists(vacancy.getId())) {
            final String sql = queryStorage.getQueryString(SqliteQueryStorage.INSERT_VACANCY);

            try (Connection connection = getConnection()) {
                final PreparedStatement ps = connection.prepareStatement(sql);

                int c = 1;

                final Date sqlDate = convertUtilDateToSqlDate(
                        vacancy.getDate()
                );

                ps.setLong(c++, vacancy.getId());
                ps.setString(c++, vacancy.getName());
                ps.setString(c++, vacancy.getCompany());
                ps.setString(c++, vacancy.getCompanyUrl());
                ps.setString(c++, vacancy.getCity());
                ps.setString(c++, vacancy.getDescription());
                ps.setDate(c++, sqlDate);
                ps.setString(c++, vacancy.getDateText());
                ps.setString(c++, vacancy.getRawAddress());
                ps.setString(c++, vacancy.getInfo());
                ps.setString(c++, vacancy.getInfoHtml());
                ps.setString(c++, vacancy.getMetroStation());
                ps.setString(c++, vacancy.getPageUrl());

                if (vacancy.getSalary() != null) {
                    final Salary s = vacancy.getSalary();
                    ps.setInt(c++, s.getFrom());
                    ps.setInt(c++, s.getTo());
                    ps.setString(c++, s.getUnit());
                    ps.setString(c++, s.getExtra());
                    ps.setString(c++, s.getSource());
                } else {
                    ps.setInt(c++, 0);
                    ps.setInt(c++, 0);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                }

                if (vacancy.getContactInfo() != null) {
                    final ContactInfo cont = vacancy.getContactInfo();
                    ps.setString(c++, cont.getFullName());
                    ps.setString(c++, cont.getEmail());
                    ps.setString(c++, cont.getComment());
                    ps.setString(c++, cont.getPhonesString());
                } else {
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                }

                if (vacancy.getAddress() != null) {
                    final Address a = vacancy.getAddress();
                    ps.setString(c++, a.getRawAddress());
                    ps.setString(c++, a.getCity());
                    ps.setString(c++, a.getStreet());
                    ps.setString(c++, a.getBuilding());
                    ps.setString(c++, a.getDisplayName());
                    ps.setString(c++, a.getMetroStationsString());
                } else {
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                }

                if (vacancy.getArea() != null) {
                    final Area a = vacancy.getArea();
                    ps.setString(c++, a.getRegionName());
                    ps.setString(c++, a.getCity());
                    ps.setString(c++, a.getCountryCode());
                    ps.setString(c++, a.getRegionId());
                } else {
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                }

                ps.setString(c++, vacancy.getSkillsString());

                ps.setBoolean(c++, vacancy.isTrusted());
                ps.setBoolean(c++, vacancy.isArchived());
                ps.setBoolean(c++, vacancy.isActive());
                ps.setBoolean(c++, vacancy.isDisabled());
                ps.setBoolean(c++, vacancy.isAbridged());

                ps.execute();
            }
        } else {
            final String sql = queryStorage.getQueryString(SqliteQueryStorage.UPDATE_VACANCY);

            try (Connection connection = getConnection()) {
                final PreparedStatement ps = connection.prepareStatement(sql);

                int c = 1;

                final Date sqlDate = convertUtilDateToSqlDate(
                        vacancy.getDate()
                );

                ps.setString(c++, vacancy.getName());
                ps.setString(c++, vacancy.getCompany());
                ps.setString(c++, vacancy.getCompanyUrl());
                ps.setString(c++, vacancy.getCity());
                ps.setString(c++, vacancy.getDescription());
                ps.setDate(c++, sqlDate);
                ps.setString(c++, vacancy.getDateText());
                ps.setString(c++, vacancy.getRawAddress());
                ps.setString(c++, vacancy.getInfo());
                ps.setString(c++, vacancy.getInfoHtml());
                ps.setString(c++, vacancy.getMetroStation());
                ps.setString(c++, vacancy.getPageUrl());

                if (vacancy.getSalary() != null) {
                    final Salary s = vacancy.getSalary();
                    ps.setInt(c++, s.getFrom());
                    ps.setInt(c++, s.getTo());
                    ps.setString(c++, s.getUnit());
                    ps.setString(c++, s.getExtra());
                    ps.setString(c++, s.getSource());
                } else {
                    ps.setInt(c++, 0);
                    ps.setInt(c++, 0);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                }

                if (vacancy.getContactInfo() != null) {
                    final ContactInfo cont = vacancy.getContactInfo();
                    ps.setString(c++, cont.getFullName());
                    ps.setString(c++, cont.getEmail());
                    ps.setString(c++, cont.getComment());
                    ps.setString(c++, cont.getPhonesString());
                } else {
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                }

                if (vacancy.getAddress() != null) {
                    final Address a = vacancy.getAddress();
                    ps.setString(c++, a.getRawAddress());
                    ps.setString(c++, a.getCity());
                    ps.setString(c++, a.getStreet());
                    ps.setString(c++, a.getBuilding());
                    ps.setString(c++, a.getDisplayName());
                    ps.setString(c++, a.getMetroStationsString());
                } else {
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                }

                if (vacancy.getArea() != null) {
                    final Area a = vacancy.getArea();
                    ps.setString(c++, a.getRegionName());
                    ps.setString(c++, a.getCity());
                    ps.setString(c++, a.getCountryCode());
                    ps.setString(c++, a.getRegionId());
                } else {
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                    ps.setString(c++, null);
                }

                ps.setString(c++, vacancy.getSkillsString());

                ps.setBoolean(c++, vacancy.isTrusted());
                ps.setBoolean(c++, vacancy.isArchived());
                ps.setBoolean(c++, vacancy.isActive());
                ps.setBoolean(c++, vacancy.isDisabled());
                ps.setBoolean(c++, vacancy.isAbridged());

                ps.setLong(c++, vacancy.getId());

                ps.execute();
            }
        }
    }

    private static java.sql.Date convertUtilDateToSqlDate(java.util.Date uDate) {
        if (uDate == null) {
            return null;
        }
        java.sql.Date sDate = new java.sql.Date(uDate.getTime());
        return sDate;
    }

    public final boolean isCompanyExists(long companyId) throws SQLException {
        int locId;
        try (Connection connection = getConnection()) {
            final String sql = queryStorage.getQueryString(SqliteQueryStorage.IS_COMPANY_EXISTS);
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, companyId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            locId = 0;
            while (resultSet.next()) {
                locId = resultSet.getInt("id");
            }
        }
        return locId != 0;
    }

    public final boolean isVacancyExists(long vacancyId) throws SQLException {
        int locId;
        try (Connection connection = getConnection()) {
            final String sql = queryStorage.getQueryString(SqliteQueryStorage.IS_VACANCY_EXISTS);
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, vacancyId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            locId = 0;
            while (resultSet.next()) {
                locId = resultSet.getInt("id");
            }
        }
        return locId != 0;
    }

    private Connection getConnection() throws SQLException {
        try {
            if (!driverLoaded) {
                Class.forName(DB_DRIVER);
            }
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            Log.getLogger().error(e.getMessage(), e);
        }

        return DriverManager.getConnection(DB_CONNECTION + file.getAbsolutePath());
    }

    public final void executeSimpleSQL(final String sql) {
        try {
            try (Connection connection = getConnection()) {
                final Statement statement = connection.createStatement();
                statement.execute(sql);
            }
        } catch (SQLException e) {
            Log.getLogger().error(e.getMessage(), e);
        }
    }

    public int getCountRecordsOfTable(final String tableName) {
        int count = -1;

        try {
            try (Connection connection = getConnection()) {
                final Statement statement = connection.createStatement();
                final String sql = "SELECT COUNT(*) count FROM " + tableName + ";";
                final ResultSet resultSet = statement.executeQuery(sql);

                while (resultSet.next()) {
                    count = resultSet.getInt("count");
                }
            }

        } catch (SQLException e) {
            Log.getLogger().error(e.getMessage(), e);
        }

        return count;
    }

    public final void removeDataBaseFile() {
        if (file.exists()) {
            final boolean result = file.delete();
            if (result) {
                Log.getLogger().info(file.getAbsolutePath() + " file removed");
            } else {
                Log.getLogger().info(file.getAbsolutePath() + " db file can not be removed");
            }
        }
    }

    private int getGeneratedId(final Statement statement) {
        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            Log.getLogger().error(e.getMessage(), e);
        }

        return 0;
    }

}
