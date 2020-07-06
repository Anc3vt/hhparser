package ru.ancevt.webparsers.headhunter.api;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.ancevt.net.httpclient.GZIPUtil;
import ru.ancevt.net.httpclient.HttpClient;
import ru.ancevt.net.httpclient.HttpHeader;
import ru.ancevt.net.httpclient.HttpMethod;
import ru.ancevt.net.httpclient.HttpRequest;
import ru.ancevt.net.httpclient.HttpRequestMaker;
import ru.ancevt.util.json.JSONNullHelper;
import ru.ancevt.webdatagrabber.api.IRequester;
import ru.ancevt.webdatagrabber.config.Config;
import ru.ancevt.webdatagrabber.log.Log;
import ru.ancevt.webparsers.headhunter.HHConfig;
import ru.ancevt.webparsers.headhunter.ds.Address;
import ru.ancevt.webparsers.headhunter.ds.Area;
import ru.ancevt.webparsers.headhunter.ds.ContactInfo;
import ru.ancevt.webparsers.headhunter.ds.Salary;
import ru.ancevt.webparsers.headhunter.ds.Vacancy;

/**
 *
 * @author ancevt
 */
public class VacancyRequester implements IRequester<Vacancy> {
    
    private static final int MAX_TRIES = 10;
    
    private final String host;
    private static HttpRequestMaker requestMaker;
    private int status;
    private int vacancyId;
    private String url;
    private int tries;
    
    public VacancyRequester(Config config) {
        this.host = ((HHConfig) config).getHost();
        
        tries = MAX_TRIES;
        
        if (requestMaker == null) {
            requestMaker = new HttpRequestMaker();
            requestMaker.addDefaultHttpHeader(new HttpHeader("Host", host));
            requestMaker.addDefaultHttpHeader(new HttpHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:68.0) Gecko/20100101 Firefox/68.0"));
            requestMaker.addDefaultHttpHeader(new HttpHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
            requestMaker.addDefaultHttpHeader(new HttpHeader("Accept-Language", "ru,en-US;q=0.7,en;q=0.3"));
            requestMaker.addDefaultHttpHeader(new HttpHeader("Accept-Encoding", "gzip, deflate"));
            requestMaker.addDefaultHttpHeader(new HttpHeader("DNT", "1"));
            requestMaker.addDefaultHttpHeader(new HttpHeader("Connection", "keep-alive"));
            requestMaker.addDefaultHttpHeader(new HttpHeader("Upgrade-Insecure-Requests", "1"));
            requestMaker.addDefaultHttpHeader(new HttpHeader("Pragma", "no-cache"));
            requestMaker.addDefaultHttpHeader(new HttpHeader("Cache-Control", "no-cache"));
        }
    }
    
    @Override
    public int getStatus() {
        return status;
    }
    
    @Override
    public Vacancy get(int id) throws IOException {
        this.vacancyId = id;
        
        url = String.format("https://%s/vacancy/%d", host, id);
        final String html = requestHtml(url);
        
        return parseVacancyPage(html);
    }
    
    private String requestHtml(String url) throws IOException {
        final HttpRequest req = requestMaker.create(url, HttpMethod.GET, null);
        
        final HttpClient client = new HttpClient();
        try {
            client.connect(req);
        } catch (TimeoutException ex) {
            Log.getLogger().error(ex, ex);
        }
        
        if (client.getStatus() == 302) {
            final String newUrl = client.getHeaderValue("Location");
            req.setUrl(newUrl);
            req.setHeader("Host", new URL(newUrl).getHost());
            
            client.close();
            
            try {
                client.connect(req);
            } catch (TimeoutException ex) {
                Log.getLogger().error(ex, ex);
            }
        }
        
        status = client.getStatus();
        
        if (client.getStatus() == 200) {
            final boolean gzip = "gzip".equalsIgnoreCase(client.getHeaderValue(HttpHeader.CONTENT_ENCODING));
            byte[] bytes = null;
            try {
                bytes = client.readBytes();
            } catch (TimeoutException ex) {
                Log.getLogger().error(ex, ex);
            }
            final String html = gzip ? GZIPUtil.decompress(bytes) : new String(bytes);
            client.close();
            return html;
        }
        
        return null;
    }
    
    private Vacancy retry() throws IOException {
        tries--;
        if (tries < 0) {
            return null;
        }
        
        Log.getLogger().warn("Unexpected another page responsed, retry request " + url + ", tries: " + tries);
        return get(vacancyId);
    }
    
    private Vacancy parseVacancyPage(String html) throws IOException {
        if (html == null) {
            return null;
        }
        
        final Vacancy v = new Vacancy();
        
        final Document d = Jsoup.parse(html);
        
        String[] metroStationsFromJson = null;

        //for test repeating
        //if(Math.random() > 0.9) return retry();
        if (d.getElementsByAttributeValue("data-qa", "vacancy-title").size() > 0) {
            v.setName(d.getElementsByAttributeValue("data-qa", "vacancy-title").text());
        } else {
            ///HTMLFileUtil.browse(html, new File("/tmp/html.html"));
            return retry();
        }
        v.setId(vacancyId);
        
        if (d.getElementsByAttributeValue("data-qa", "vacancy-view-raw-address").size() > 0) {
            v.setRawAddress(d.getElementsByAttributeValue("data-qa", "vacancy-view-raw-address").get(0).text());
        }
        
        if (d.getElementsByClass("vacancy-creation-time").size() > 0) {
            v.setDateText(d.getElementsByClass("vacancy-creation-time").get(0).text());
        }
        
        if (d.getElementsByClass("vacancy-company-name").size() > 0) {
            final Element el = d.getElementsByClass("vacancy-company-name").get(0);
            v.setCompany(el.text());
            v.setCompanyUrl("https://" + host + el.attr("href"));
        } else if (d.getElementsByClass("vacancy-company-name-wrapper").size() > 0) {
            final Element el = d.getElementsByClass("vacancy-company-name-wrapper").get(0);
            v.setCompany(el.text());
        }
        
        v.setPageUrl(url);
        
        final Element el = d.getElementById("HH-Lux-InitialState");
        if (el != null) {
            
            final String json = el.text();
            final JSONObject root = new JSONObject(json);

            //System.out.println(root.toString(3));
            final JSONObject vacancyView = JSONNullHelper.getJSONObjectOrNull(root, "vacancyView");
            if (vacancyView == null) {
                
            } else {
                
                final JSONObject addressJson = JSONNullHelper.getJSONObjectOrNull(vacancyView, "address");
                if (addressJson != null) {
                    final String city = JSONNullHelper.getStringOrNull(addressJson, "city");
                    v.setCity(JSONNullHelper.getStringOrNull(addressJson, "city"));
                    
                    final Address address = new Address();
                    address.setRawAddress(JSONNullHelper.getStringOrNull(addressJson, "rawAddress"));
                    address.setStreet(JSONNullHelper.getStringOrNull(addressJson, "street"));
                    address.setDisplayName(JSONNullHelper.getStringOrNull(addressJson, "displayName"));
                    address.setBuilding(JSONNullHelper.getStringOrNull(addressJson, "building"));
                    address.setCity(city);
                    
                    if (!JSONNullHelper.isNullOrUndefined(addressJson, "metroStations")) {
                        
                        final JSONObject metroStationsJson = addressJson.getJSONObject("metroStations");
                        if (!JSONNullHelper.isNullOrUndefined(metroStationsJson, "metro")) {
                            
                            final JSONArray a = metroStationsJson.getJSONArray("metro");
                            
                            final int size = a.length();
                            metroStationsFromJson = new String[size];
                            for (int i = 0; i < size; i++) {
                                final JSONObject o = a.getJSONObject(i);
                                metroStationsFromJson[i] = JSONNullHelper.getStringOrNull(o, "name");
                                
                            }
                            address.setMetroStations(metroStationsFromJson);
                        }
                        
                    }
                    
                    if (address.getRawAddress() != null) {
                        v.setAddress(address);
                    }
                    
                }
            }
            
            final String salaryString = d.getElementsByClass("vacancy-salary").text();
            final Salary salary = new Salary(salaryString);
            if (!salary.isEmpty()) {
                v.setSalary(salary);
            }
            
            final String description = vacancyView.getString("description");
            v.setDescription(description);
            
            final boolean trusted = vacancyView.getJSONObject("company").getBoolean("@trusted");
            v.setTrusted(trusted);
            
            final Date publicationDate = convertToDate(vacancyView.getString("publicationDate"));
            v.setDate(publicationDate);
            
            if (!JSONNullHelper.isNullOrUndefined(vacancyView, "contactInfo")) {
                final JSONObject contactInfoJson = vacancyView.getJSONObject("contactInfo");
                final ContactInfo contactInfo = new ContactInfo();
                if (!JSONNullHelper.isNullOrUndefined(contactInfoJson, "phones")) {
                    
                    final JSONArray phonesJson = contactInfoJson.getJSONObject("phones").getJSONArray("phones");
                    
                    final String[] phones = new String[phonesJson.length()];
                    for (int i = 0; i < phonesJson.length(); i++) {
                        final JSONObject phoneJson = phonesJson.getJSONObject(i);
                        
                        final String country = phoneJson.getString("country");
                        final String phoneCity = phoneJson.getString("city");
                        final String number = phoneJson.getString("number");
                        
                        String comment = "";
                        if (!phoneJson.isNull("comment")) {
                            comment = phoneJson.getString("comment");
                        }
                        final String phone = "+" + country + " (" + phoneCity + ") " + number + " " + comment;
                        
                        phones[i] = phone;
                    }
                    contactInfo.setPhones(phones);
                }
                
                contactInfo.setFullName(JSONNullHelper.getStringOrNull(contactInfoJson, "fio"));
                contactInfo.setEmail(JSONNullHelper.getStringOrNull(contactInfoJson, "email"));
                
                v.setContactInfo(contactInfo);
            }
            
            if (!JSONNullHelper.isNullOrUndefined(vacancyView, "keySkills")) {
                
                final JSONObject o = vacancyView.getJSONObject("keySkills");
                if (!JSONNullHelper.isNullOrUndefined(o, "keySkill")) {
                    final JSONArray a = o.getJSONArray("keySkill");
                    final int size = a.length();
                    final String[] skills = new String[size];
                    for (int i = 0; i < size; i++) {
                        skills[i] = a.getString(i);
                    }
                    
                    v.setSkills(skills);
                }
            }
            
            if (!JSONNullHelper.isNullOrUndefined(vacancyView, "area")) {
                final JSONObject o = vacancyView.getJSONObject("area");
                final Area area = new Area(
                    JSONNullHelper.getIntOrZero(o, "@id"),
                    JSONNullHelper.getStringOrNull(o, "areaNamePre"),
                    JSONNullHelper.getStringOrNull(o, "regionName"),
                    JSONNullHelper.getStringOrNull(o, "name"),
                    JSONNullHelper.getStringOrNull(o, "@countryIsoCode"),
                    JSONNullHelper.getStringOrNull(o, "@regionId"));
                
                if (v.getCity() == null || v.getCity().isEmpty()) {
                    v.setCity(area.getCity());
                }
                
                v.setArea(area);
            }
            
            final JSONObject statusJson = JSONNullHelper.getJSONObjectOrNull(vacancyView, "status");
            v.setArchived(statusJson.getBoolean("archived"));
            v.setActive(statusJson.getBoolean("active"));
            v.setDisabled(statusJson.getBoolean("disabled"));
        } else {
            v.setAbridged(true);
            
            v.setSalary(new Salary(d.getElementsByClass("vacancy-salary").text()));
            
            final Elements addresLocalityEl = d.getElementsByAttributeValue("itemprop", "addressLocality");
            if (addresLocalityEl != null && addresLocalityEl.size() > 0) {
                v.setCity(addresLocalityEl.attr("content"));
            }
            
            final Elements metroEl = d.getElementsByClass("metro-point");
            if (metroEl.size() > 0) {
                v.setMetroStation(metroEl.get(0).text());
            }
            
            final Elements addrBlockEl = d.getElementsByAttributeValue("data-name", "HH/Employer/VacancyCard/VacancyMap");
            if (addrBlockEl != null && addrBlockEl.size() > 0) {
                final JSONObject root = new JSONObject(addrBlockEl.attr("data-params"));
                final JSONObject addressJson = JSONNullHelper.getJSONObjectOrNull(root, "address");
                if (addressJson != null) {
                    final Address address = new Address();
                    address.setRawAddress(JSONNullHelper.getStringOrNull(addressJson, "rawAddress"));
                    address.setStreet(JSONNullHelper.getStringOrNull(addressJson, "street"));
                    address.setBuilding(JSONNullHelper.getStringOrNull(addressJson, "building"));
                    address.setCity(JSONNullHelper.getStringOrNull(addressJson, "city"));
                    address.setMetroStations(new String[]{JSONNullHelper.getStringOrNull(addressJson, "metro")});
                    v.setCity(JSONNullHelper.getStringOrNull(addressJson, "city"));
                    if (address.getRawAddress() != null) {
                        v.setAddress(address);
                    }
                }
                
            }

            // data-name="HH/Employer/VacancyCard/VacancyMap"
        }
        
        v.setInfo(d.getElementsByClass("vacancy-description").text());
        v.setInfoHtml(d.getElementsByClass("vacancy-description").html());
        
        if (metroStationsFromJson != null && metroStationsFromJson.length > 0) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < metroStationsFromJson.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(metroStationsFromJson[i]);
                v.setMetroStation(sb.toString());
            }
        }
        
        return v;
    }
    
    private static Date convertToDate(final String source) {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyyy-MM-dd'T'HH:mm:ss");
            return sdf.parse(source);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
}
