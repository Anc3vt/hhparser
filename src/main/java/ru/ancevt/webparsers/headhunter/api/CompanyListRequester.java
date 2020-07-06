package ru.ancevt.webparsers.headhunter.api;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
 *
 * @author ancevt
 */
public class CompanyListRequester implements IListRequester {

    private final HHConfig config;
    private final HttpRequestMaker requestMaker;
    private HHAreas areas;
    private String url;
    private int[] companyIds;

    public CompanyListRequester(Config config) {
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
        vars.add("st", "employersList");
        vars.add("vacanciesNotRequired", true);
        vars.add("query", config.getCompaniesKeyPhrase());

        final Integer[] areaIds = areas.getIdsByWord(config.getCompaniesArea());
        if (areaIds != null && areaIds.length > 0) {
            vars.add("areaId", areaIds[0]);
        }
        vars.add("page", page);

        final String host = config.getHost();

        url = "https://" + host + "/employers_list?" + vars.toString();

        Log.getLogger().info(url);
        
        
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
        final byte[] bytes;
        try {
            bytes = client.readBytes();
            final String html = gzip ? GZIPUtil.decompress(bytes) : new String(bytes);
            parseCompanyIds(html);
        } catch (TimeoutException ex) {
            Log.getLogger().error(ex, ex);
        }
        

        client.close();

        return client.getStatus();
    }

    private void parseCompanyIds(String html) {
        final Document d = Jsoup.parse(html);

        final List<Integer> ids = new ArrayList<>();

        final Elements es = d.getElementsByTag("a");
        final int size = es.size();
        for (int i = 0; i < size; i++) {
            final Element e = es.get(i);
            final String href = e.attr("href");
            if (href.startsWith("/employer/") && !e.hasClass("supernova-link_secondary")) {
                final int id = getIdFromPageUrl(href);
                ids.add(id);
            }
        }

        final int[] result = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            result[i] = ids.get(i);
        }

        companyIds = result;
    }

    @Override
    public int[] getIds() {
        return companyIds;
    }

    private static int getIdFromPageUrl(String url) {
        final String[] splitted = url.split("/");
        final String right = splitted[splitted.length - 1];
        final String[] splitted2 = right.split("\\?");
        return Integer.valueOf(splitted2[0]);
    }

}
