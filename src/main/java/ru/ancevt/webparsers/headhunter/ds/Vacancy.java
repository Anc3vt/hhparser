package ru.ancevt.webparsers.headhunter.ds;

import java.util.Date;
import ru.ancevt.util.string.ToStringBuilder;
import ru.ancevt.webdatagrabber.ds.IEntity;

/**
 *
 * @author ancevt
 */
public class Vacancy implements IEntity {

    private long id;
    private String name;
    private String company;
    private String companyUrl;
    private String city;
    private String description;
    private Date date;
    private Salary salary;
    private String pageUrl;

    private String rawAddress;
    private String info;
    private String infoHtml;
    private ContactInfo contactInfo;
    private Address address;
    private Area area;
    private String[] skills;

    private boolean trusted;
    private boolean archived;
    private boolean active = true;
    private boolean disabled;

    private String metroStation;
    private String dateText;
    private boolean abridged;

    public boolean isAbridged() {
        return abridged;
    }

    public void setAbridged(boolean abridged) {
        this.abridged = abridged;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disable) {
        this.disabled = disable;
    }

    public String[] getSkills() {
        return skills;
    }

    public String getSkillsString() {
        if (skills == null || skills.length == 0) {
            return null;
        }

        final StringBuilder s = new StringBuilder();
        for (int i = 0; i < skills.length; i++) {
            if (i > 0) {
                s.append(", ");
            }
            s.append(skills[i]);
        }
        return s.toString();
    }

    public void setSkills(String[] skills) {
        this.skills = skills;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCompanyUrl() {
        return companyUrl;
    }

    public void setCompanyUrl(String companyUrl) {
        this.companyUrl = companyUrl;
    }

    public String getInfoHtml() {
        return infoHtml;
    }

    public void setInfoHtml(String infoHtml) {
        this.infoHtml = infoHtml;
    }

    public String getRawAddress() {
        return rawAddress;
    }

    public void setRawAddress(String rawAddress) {
        this.rawAddress = rawAddress;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contact) {
        this.contactInfo = contact;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setMetroStation(String metroStation) {
        this.metroStation = metroStation;
    }

    public final String getMetroStation() {
        if (address == null) {
            return null;
        }
        if (address.getMetroStations() != null && address.getMetroStations().length > 0) {
            return address.getMetroStations()[0];
        }
        return metroStation;
    }

    public void setDescription(String decription) {
        this.description = decription;
    }

    public String getDateText() {
        return dateText;
    }

    public void setDateText(String dateText) {
        this.dateText = dateText;
    }

    public Salary getSalary() {
        return salary;
    }

    public void setSalary(Salary salary) {
        this.salary = salary;
    }

    public void setSalary(String salarySource) {
        this.salary = new Salary(salarySource);
    }

    @Override
    public String toString() {

        final StringBuilder sbSkills = new StringBuilder("[");

        if (skills != null) {
            for (int i = 0; i < skills.length; i++) {
                final String skill = skills[i];
                sbSkills.append(skill);
                if (i != skills.length - 1) {
                    sbSkills.append(',').append(' ');
                }
            }
            sbSkills.append(']');
        } else {
            sbSkills.setLength(0);
            sbSkills.append("null");
        }

        return new ToStringBuilder(this, true)
                .appendAll(
                        "id",
                        "name",
                        "pageUrl",
                        "company",
                        "date",
                        "dateText",
                        "companyUrl",
                        "city",
                        "metroStation",
                        "description",
                        "date",
                        "salary",
                        "contactInfo",
                        "rawAddress",
                        "address",
                        "area")
                .append("skills", sbSkills.toString())
                .appendAll(
                        "archived",
                        "active",
                        "disabled",
                        "info",
                        "abridged")
                .build();

    }

    @Override
    public String getShortDisplayName() {
        return String.format("%d %s [%s]", id, name, company);
    }

}
