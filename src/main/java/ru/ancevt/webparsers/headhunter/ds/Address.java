/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ancevt.webparsers.headhunter.ds;

import ru.ancevt.util.string.ToStringBuilder;

/**
 *
 * @author ancevt
 */
public class Address {

    private String rawAddress;
    private String city;
    private String street;
    private String displayName;
    private String[] metroStations;
    private String building;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("city", city)
                .append("street", street)
                .append("building", building)
                .append("displayName", displayName)
                .append("metroStations", getMetroStations().length)
                .build();
    }

    public String getRawAddress() {
        return rawAddress;
    }

    public void setRawAddress(String rawAddress) {
        this.rawAddress = rawAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String[] getMetroStations() {
        return metroStations == null ? metroStations = new String[]{} : metroStations;
    }

    public void setMetroStations(String[] metroStations) {
        this.metroStations = metroStations;
    }

    public String getMetroStationsString() {
        if (metroStations == null || metroStations.length == 0) {
            return null;
        }

        final StringBuilder s = new StringBuilder();
        for(int i = 0; i < metroStations.length; i ++) {
            if(i > 0) s.append(", ");
            s.append(metroStations[i]);
        }
        
        return s.toString();
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

}
