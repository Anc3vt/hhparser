package ru.ancevt.webparsers.headhunter.ds;

import ru.ancevt.util.string.ToStringBuilder;

/**
 *
 * @author ancevt
 */
public class Area {

    private final String areaNamePre;
    private final String regionName;
    private final String city;
    private final String countryCode;
    private final String regionId;
    private final int id;

    public Area(
            int id,
            String areaNamePre,
            String regionName,
            String name,
            String countryCode,
            String regionId
    ) {
        this.areaNamePre = areaNamePre;
        this.regionName = regionName;
        this.city = name;
        this.countryCode = countryCode;
        this.regionId = regionId;
        this.id = id;
    }

    public String getAreaNamePre() {
        return areaNamePre;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getCity() {
        return city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getRegionId() {
        return regionId;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("areaNamePre", areaNamePre)
                .append("regionName", regionName)
                .append("city", city)
                .append("countryCode", countryCode)
                .append("regionId", regionId)
                .build();

    }

}
