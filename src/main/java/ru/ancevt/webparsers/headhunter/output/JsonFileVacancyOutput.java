package ru.ancevt.webparsers.headhunter.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.JSONObject;
import ru.ancevt.webdatagrabber.log.Log;
import ru.ancevt.webdatagrabber.output.IOutput;
import ru.ancevt.webparsers.headhunter.ds.Address;
import ru.ancevt.webparsers.headhunter.ds.Area;
import ru.ancevt.webparsers.headhunter.ds.ContactInfo;
import ru.ancevt.webparsers.headhunter.ds.Salary;
import ru.ancevt.webparsers.headhunter.ds.Vacancy;

/**
 *
 * @author ancevt
 */
public class JsonFileVacancyOutput implements IOutput<Vacancy> {

    private File file;
    private final boolean includeInfoHtml;
    private PrintWriter printWriter;
    private int count;
    private final boolean indentation;

    public JsonFileVacancyOutput(boolean includeInfoHtml, boolean indentation) {
        this.includeInfoHtml = includeInfoHtml;
        this.indentation = indentation;

        count = 0;
    }

    @Override
    public void output(Vacancy vacancy) {
        if (vacancy.getName() == null || vacancy.getName().isEmpty()) {
            return;
        }

        if (printWriter == null) {
            try {
                if (file.getParentFile() != null && !file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                printWriter = new PrintWriter(new FileOutputStream(file));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }

        if (printWriter != null) {
            if (count == 0) {
                printWriter.write("{\"vacancies\":[");
            } else {
                printWriter.write(indentation ? ",\n\n" : ",");
            }

            printWriter.write(toJsonVacancy(vacancy));
            printWriter.flush();
        }

        count++;
    }

    public final String toJsonVacancy(Vacancy v) {
        final JSONObject root = new JSONObject();
        root.put("id", v.getId());
        root.put("name", handleNull(v.getName()));
        root.put("company", handleNull(v.getCompany()));
        root.put("companyUrl", handleNull(v.getCompanyUrl()));
        root.put("city", handleNull(v.getCity()));
        root.put("description", handleNull(v.getDescription()));
        root.put("date", v.getDate() == null ? JSONObject.NULL : v.getDate().getTime() / 1000);
        root.put("dateText", handleNull(v.getDateText()));
        root.put("pageUrl", handleNull(v.getPageUrl()));
        root.put("rawAddress", handleNull(v.getRawAddress()));
        root.put("info", handleNull(v.getInfo()));

        if (includeInfoHtml) {
            root.put("infoHtml", v.getInfoHtml());
        } else {
            root.put("infoHtml", JSONObject.NULL);
        }

        root.put("trusted", v.isTrusted());
        root.put("archived", v.isArchived());
        root.put("active", v.isActive());
        root.put("disabled", v.isDisabled());
        root.put("metroStation", handleNull(v.getMetroStation()));
        root.put("abridged", v.isAbridged());

        if (v.getSalary() != null) {
            final Salary s = v.getSalary();
            final JSONObject jSalary = new JSONObject();
            jSalary.put("from", s.getFrom());
            jSalary.put("to", s.getTo());
            jSalary.put("unit", handleNull(s.getUnit()));
            jSalary.put("extra", handleNull(s.getExtra()));
            jSalary.put("displayText", handleNull(s.getSource()));

            root.put("salary", jSalary);
        } else {
            root.put("salary", JSONObject.NULL);
        }

        if (v.getContactInfo() != null) {
            final ContactInfo c = v.getContactInfo();
            final JSONObject jContact = new JSONObject();
            jContact.put("email", handleNull(c.getEmail()));
            jContact.put("fullName", handleNull(c.getFullName()));
            jContact.put("comment", handleNull(c.getComment()));
            jContact.put("phones", handleNull(c.getPhones()));

            root.put("contactInfo", jContact);
        } else {
            root.put("contactInfo", JSONObject.NULL);
        }

        if (v.getArea() != null) {
            final Area a = v.getArea();
            final JSONObject jArea = new JSONObject();
            jArea.put("areaNamePre", handleNull(a.getAreaNamePre()));
            jArea.put("regionName", handleNull(a.getRegionName()));
            jArea.put("regionId", handleNull(a.getRegionId()));
            jArea.put("id", a.getId());
            jArea.put("countryCode", handleNull(a.getCountryCode()));
            jArea.put("city", handleNull(a.getCity()));

            root.put("area", jArea);
        } else {
            root.put("area", JSONObject.NULL);
        }

        if (v.getAddress() != null) {
            final Address a = v.getAddress();
            final JSONObject jAddr = new JSONObject();
            jAddr.put("city", handleNull(a.getCity()));
            jAddr.put("street", handleNull(a.getStreet()));
            jAddr.put("displayName", handleNull(a.getDisplayName()));
            jAddr.put("building", handleNull(a.getBuilding()));
            jAddr.put("metroStations", handleNull(a.getMetroStations()));

            root.put("address", jAddr);
        } else {
            root.put("address", JSONObject.NULL);
        }

        root.put("skills", handleNull(v.getSkills()));

        return indentation ? root.toString(3) : root.toString();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    private static Object handleNull(Object object) {
        if (object == null || (object instanceof String) && ((String)object).length() == 0) {
            return JSONObject.NULL;
        }
        return object;
    }

    @Override
    public void close() throws IOException {
        if (printWriter != null) {
            printWriter.write("]}");
            Log.getLogger().info("Result file: " + file.getAbsolutePath());
            printWriter.close();
        }
    }

}
