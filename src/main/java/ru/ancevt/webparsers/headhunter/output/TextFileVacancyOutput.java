package ru.ancevt.webparsers.headhunter.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import ru.ancevt.webdatagrabber.log.Log;
import ru.ancevt.webdatagrabber.output.IOutput;
import ru.ancevt.webparsers.headhunter.ds.Address;
import ru.ancevt.webparsers.headhunter.ds.Area;
import ru.ancevt.webparsers.headhunter.ds.ContactInfo;
import ru.ancevt.webparsers.headhunter.ds.Vacancy;

public class TextFileVacancyOutput implements IOutput<Vacancy> {

    private static final String END_LINE = String.format("%n");

    private PrintWriter printWriter;
    private final boolean includeInfoHtml;
    private File file;

    public TextFileVacancyOutput(boolean includeInfoHtml) {
        this.includeInfoHtml = includeInfoHtml;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
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
            printWriter.write(prettyFormat(vacancy));
            printWriter.flush();
        }
    }

    private String prettyFormat(Vacancy v) {
        final StringBuilder s = new StringBuilder();
        s.append("------------------------------------------------------------").append(END_LINE);
        s.append("id: " + v.getId()).append(END_LINE);
        append(s, "pageUrl", v.getPageUrl());
        append(s, "name", v.getName());
        append(s, "city", v.getCity());
        append(s, "company", v.getCompany());
        append(s, "companyUrl", v.getCompanyUrl());
        append(s, "rawAddress", v.getRawAddress());
        append(s, "metroStation", v.getMetroStation());
        append(s, "date", v.getDate());
        append(s, "dateText", v.getDateText());
        if (v.getSalary() != null) {
            append(s, "salary", v.getSalary().getSource());
        }
        append(s, "description", v.getDescription());
        append(s, "info", v.getInfo());

        if (includeInfoHtml) {
            append(s, "infoHtml", v.getInfoHtml());
        }

        if (v.getSkills() != null) {
            final String[] skills = v.getSkills();
            if (skills != null && skills.length > 0) {
                final StringBuilder skillsSb = new StringBuilder();
                for (int i = 0; i < skills.length; i++) {
                    if (i > 0) {
                        skillsSb.append(", ");
                    }
                    skillsSb.append(skills[i]);
                }
                append(s, "skills", skillsSb.toString());
            }
        }

        append(s, "trusted", v.isTrusted() ? "yes" : "no");
        append(s, "archived", v.isArchived() ? "yes" : "no");
        append(s, "active", v.isActive() ? "yes" : "no");
        append(s, "disabled", v.isDisabled() ? "yes" : "no");
        append(s, "abridget", v.isAbridged() ? "yes" : "no");

        if (v.getArea() != null) {
            final Area area = v.getArea();
            final StringBuilder sb = new StringBuilder();

            sb.append("[").append(area.getCountryCode()).append("] ");
            final String reg = area.getRegionName();
            final String city = area.getCity();

            if (reg != null && city != null && !reg.contains("None") && !reg.equalsIgnoreCase(city)) {
                sb.append(reg).append(", ");
            }

            sb.append(city);

            append(s, "area", sb.toString());
        }

        if (v.getAddress() != null) {
            final Address address = v.getAddress();
            append(s, "address", address.getDisplayName());
        }

        if (v.getContactInfo() != null) {
            final ContactInfo contactInfo = v.getContactInfo();
            final String fullName = contactInfo.getFullName();
            final String comment = contactInfo.getComment();
            final String email = contactInfo.getEmail();
            final String[] phones = contactInfo.getPhones();

            final StringBuilder sb = new StringBuilder();

            if (fullName != null) {
                sb.append(fullName);
            }

            if (email != null) {
                sb.append(", ").append(email);
            }

            if (phones != null && phones.length > 0) {
                for (int i = 0; i < phones.length; i++) {
                    final String phone = phones[i];
                    sb.append(", ");
                    sb.append(phone);
                }
            }

            if (comment != null) {
                sb.append(" (").append(comment).append(") ");
            }

            append(s, "contactInfo", sb.toString());
        }

        s.append(END_LINE);

        return s.toString();
    }

    private static void append(StringBuilder stringBuilder, String key, Object object) {
        if (object != null) {
            stringBuilder.append(key).append(": ").append(object).append(END_LINE);
        }
    }

    @Override
    public void close() throws IOException {
        if (printWriter != null) {
            Log.getLogger().info("Result file: " + file.getAbsolutePath());
            printWriter.close();
        }
    }

}
