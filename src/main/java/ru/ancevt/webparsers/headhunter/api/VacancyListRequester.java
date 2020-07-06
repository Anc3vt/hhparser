package ru.ancevt.webparsers.headhunter.api;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeoutException;
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
import ru.ancevt.net.httpclient.HttpVariables;
import ru.ancevt.webdatagrabber.api.IListRequester;
import ru.ancevt.webdatagrabber.config.Config;
import ru.ancevt.webdatagrabber.log.Log;
import ru.ancevt.webparsers.headhunter.HHAreas;
import ru.ancevt.webparsers.headhunter.HHConfig;

/**
 * @author ancevt
 */
public class VacancyListRequester implements IListRequester {
    
    private final HHConfig config;
    private final HttpRequestMaker requestMaker;
    private HHAreas areas;
    private String url;
    private int[] vacancyIds;
    
    public VacancyListRequester(Config config) {
        this.config = (HHConfig) config;
        
        try {
            this.areas = HHAreas.getInstance();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        
        requestMaker = new HttpRequestMaker();
        requestMaker.addDefaultHttpHeader(new HttpHeader("Host", this.config.getHost()));
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
    
    @Override
    public int request(int page) throws IOException {
        final HttpVariables vars = new HttpVariables();
        vars.add("only_with_salary", config.getVacanciesOnlyWithSalary());
        //vars.add("clusters", config.getString(Config.SEC_VACANCIES, Config.AREAS, Config.DEFAULT_AREAS));
        vars.add("enable_snippets", true);
        vars.add("salary", config.getVacanciesSalary());
        vars.add("st", "searchVacanvy");
        vars.add("text", config.getVacanciesKeyPhrase());
        vars.add("page", page);
        vars.add("items_on_page", 100);
        
        final String[] cls = config.getVacanciesAreas().split(",");
        
        for (String cluster : cls) {
            cluster = cluster.trim();
            final Integer[] areaIds = areas.getIdsByWord(cluster);
            for (final int id : areaIds) {
                vars.add("area", id);
            }
        }
        
        final String host = config.getHost();
        
        url = "https://" + host + "/search/vacancy?" + vars.toString();
        
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
        
        final boolean gzip = "gzip".equalsIgnoreCase(client.getHeaderValue(HttpHeader.CONTENT_ENCODING));
        byte[] bytes = null;
        try {
            bytes = client.readBytes();
        } catch (TimeoutException ex) {
            Log.getLogger().error(ex, ex);
        }
        final String html = gzip ? GZIPUtil.decompress(bytes) : new String(bytes);
        
        parseVacancyIds(html);
        
        client.close();
        
        return client.getStatus();
    }
    
    private void parseVacancyIds(String html) {
        final Document d = Jsoup.parse(html);
        final Elements e = d.getElementsByAttributeValue("data-qa", "vacancy-serp__vacancy-title");
        
        final int size = e.size();
        vacancyIds = new int[size];
        
        for (int i = 0; i < e.size(); i++) {
            Element el = e.get(i);
            final int id = getIdFromPageUrl(el.attr("href"));
            vacancyIds[i] = id;
        }
    }
    
    @Override
    public int[] getIds() {
        return vacancyIds;
    }
    
    private static int getIdFromPageUrl(String url) {
        if (!url.contains("vacancy")) {
            return 0;
        }
        
        url = url.substring(0, url.indexOf("?"));
        
        final String[] splitted = url.split("/");
        
        final String right = splitted[splitted.length - 1];
        final String[] splitted2 = right.split("\\?");
        return Integer.valueOf(splitted2[0]);
    }
    
}
