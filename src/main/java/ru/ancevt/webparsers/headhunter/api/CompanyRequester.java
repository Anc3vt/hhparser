package ru.ancevt.webparsers.headhunter.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import ru.ancevt.net.httpclient.GZIPUtil;
import ru.ancevt.net.httpclient.HttpClient;
import ru.ancevt.net.httpclient.HttpHeader;
import ru.ancevt.net.httpclient.HttpMethod;
import ru.ancevt.net.httpclient.HttpRequest;
import ru.ancevt.net.httpclient.HttpRequestMaker;
import ru.ancevt.webdatagrabber.api.Doc;
import ru.ancevt.webdatagrabber.api.IRequester;
import ru.ancevt.webdatagrabber.config.Config;
import ru.ancevt.webdatagrabber.ds.Image;
import ru.ancevt.webdatagrabber.log.Log;
import ru.ancevt.webparsers.headhunter.HHConfig;
import ru.ancevt.webparsers.headhunter.ds.Company;

/**
 *
 * @author ancevt
 */
public class CompanyRequester implements IRequester<Company> {
    
    private final String host;
    private static HttpRequestMaker requestMaker;
    private int status;
    private int companyId;
    private String url;
    private final HHConfig config;
    
    public CompanyRequester(Config config) {
        this.config = (HHConfig) config;
        this.host = this.config.getHost();
        
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
    public Company get(int id) throws IOException {
        this.companyId = id;
        
        url = String.format("https://%s/employer/%d", host, id);
        final String html = requestHtml(url);
        
        return parseCompany(html);
    }
    
    private String requestHtml(String url) throws IOException {
        final HttpRequest req = requestMaker.create(url, HttpMethod.GET, null);
        
        final HttpClient client = new HttpClient();
        try {
            client.connect(req);
        } catch (TimeoutException ex) {
            Log.getLogger().error(ex, ex);
        }
        
        status = client.getStatus();
        
        if (status == 302) {
            final String newUrl = client.getHeaderValue("Location");
            req.setUrl(url = newUrl);
            req.setHeader("Host", new URL(newUrl).getHost());
            
            client.close();
            
            try {
                client.connect(req);
            } catch (TimeoutException ex) {
                Log.getLogger().error(ex, ex);
            }
        }
        
        if (client.getStatus() == 200) {
            final boolean gzip = "gzip".equalsIgnoreCase(client.getHeaderValue(HttpHeader.CONTENT_ENCODING));
            byte[] bytes = null;
            try {
                bytes = client.readBytes();
            } catch (TimeoutException ex) {
                Logger.getLogger(CompanyRequester.class.getName()).log(Level.SEVERE, null, ex);
            }
            final String html = gzip ? GZIPUtil.decompress(bytes) : new String(bytes);
            client.close();
            return html;
        }
        
        client.close();
        
        return null;
    }
    
    private Company parseCompany(String html) throws IOException {
        if (html == null) {
            return null;
        }

        //HTMLFileUtil.browse(html, new File("/tmp/result.html"));
        final Document d = Jsoup.parse(html);
        final Doc doc = new Doc(d);
        
        int id = companyId;
        String type = null;
        String name = null;
        String info;
        String infoHtml;
        String area = null;
        String pageUrl = url;
        String webSite = null;
        boolean trusted = false;
        Image image = null;
        
        type = doc.getTextByClass("company-type");
        
        final int h1size = d.getElementsByTag("h1").size();
        for (int i = 0; i < h1size; i++) {
            final Element e = d.getElementsByTag("h1").get(i);
            if (e.className().equals("header")) {
                name = e.text();
            }
        }
        
        if (name == null) {
            return null;
        }
        
        info = doc.getTextByAttrValue("data-qa", "company-description-text");
        infoHtml = d.getElementsByAttributeValue("data-qa", "company-description-text").html();
        
        if (d.getElementsByClass("company-info").size() > 0) {
            final Element companyInfoElement = d.getElementsByClass("company-info").get(0);
            final Element child = companyInfoElement.child(0);
            area = child.text();
        }
        
        if (d.getElementsByClass("bloko-icon bloko-icon_done bloko-icon_initial-action").size() > 0) {
            trusted = true;
        }
        
        if (area == null) {
            area = doc.getTextByClass("employer-sidebar-block");
        }
        
        if (d.getElementsByClass("g-user-content").size() > 0) {
            webSite = d.getElementsByClass("g-user-content").get(0).attr("href");
            if (d.getElementsByClass("g-user-content").size() > 1) {
                info = d.getElementsByClass("g-user-content").get(1).text();
                infoHtml = d.getElementsByClass("g-user-content").get(1).html();
            }
        }
        
        if (webSite == null) {
            webSite = d.getElementsByClass("company-url").attr("href");
        }
        
        if (d.getElementsByClass("employer-sidebar__logo").size() > 0) {
            final Element e = d.getElementsByClass("employer-sidebar__logo").get(0);
            final String src = e.attr("src");
            final String alt = e.attr("alt");
            image = new Image();
            image.setAlt(alt);
            image.setSource(src);
            
            if (config.getCompaniesReadImage()) {
                readImage(image);
            }
        }
        
        final Company c = new Company(
            id,
            type,
            name,
            info,
            infoHtml,
            area,
            pageUrl = url,
            webSite,
            trusted,
            image
        );
        
        return c;
    }
    
    private static void readImage(Image image) throws IOException {
        final HttpRequest req = requestMaker.create(image.getSource(), HttpMethod.GET, null);
        
        final HttpClient client = new HttpClient();
        try {
            client.connect(req);
        } catch (TimeoutException ex) {
            Log.getLogger().error(ex, ex);
        }
        
        if (client.getStatus() == 200) {
            try {
                image.setBytes(client.readBytes());
            } catch (TimeoutException ex) {
                Log.getLogger().error(ex, ex);
            }
            final ByteArrayInputStream bais = new ByteArrayInputStream(image.getBytes());
            final String mimeType = URLConnection.guessContentTypeFromStream(bais);
            image.setMimeType(mimeType);
            bais.close();
        }
        
        client.close();
    }
}
