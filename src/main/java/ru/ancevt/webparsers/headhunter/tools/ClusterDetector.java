package ru.ancevt.webparsers.headhunter.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.ancevt.net.httpclient.GZIPUtil;
import ru.ancevt.net.httpclient.HttpClient;
import ru.ancevt.net.httpclient.HttpHeader;
import static ru.ancevt.net.httpclient.HttpMethod.GET;
import ru.ancevt.net.httpclient.HttpRequest;
import ru.ancevt.net.httpclient.HttpRequestMaker;
import ru.ancevt.util.fs.SimpleFileWriter;
import ru.ancevt.webdatagrabber.log.Log;

/**
 * @author ancevt
 */
public class ClusterDetector {
    
    public static void main(String[] args) throws IOException, InterruptedException {
        final HttpRequestMaker requestMaker = new HttpRequestMaker();
        requestMaker.addDefaultHttpHeader(new HttpHeader("Host", "spb.hh.ru"));
        requestMaker.addStandardDefaultHeaders();
        
        final File file = new File("/home/ancevt/tmp/areas.txt");
        if (file.exists()) {
            file.delete();
        }
        
        final int total = 10000;
        
        final Thread[] threads = new Thread[20];
        final List<StringBuilder> sbs = new ArrayList<>();
        
        int count = 0;
        
        for (int j = 0; j < total; j += total / threads.length) {
            
            System.out.println("j: " + j);
            
            final int J = j;
            
            threads[count] = new Thread(new Runnable() {
                
                @Override
                public void run() {
                    
                    System.out.println("run thread: " + Thread.currentThread().getName());
                    
                    final StringBuilder stringBuilder = new StringBuilder();
                    
                    for (int i = J; i < J + total / threads.length; i++) {
                        try {
                            final HttpRequest req = requestMaker.create("https://spb.hh.ru/search/vacancy?area=" + i, GET, null);
                            final HttpClient client = new HttpClient();
                            client.connect(req);
                            String html = null;
                            try {
                                html = GZIPUtil.decompress(client.readBytes());
                            } catch (TimeoutException ex) {
                                Log.getLogger().error(ex, ex);
                            }
                            
                            final Element node = Jsoup.parse(html);
                            
                            final Elements node2 = node.getElementsByAttributeValue("data-qa", "serp__criterion");
                            final String city = node2.text();
                            
                            if (city.contains("area")) {
                                // System.out.println("skip " + city);
                            } else {
                                stringBuilder.append(city + "=" + i + "\n");
                                System.out.println(city + "=" + i);
                            }
                            client.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (TimeoutException ex) {
                            Log.getLogger().error(ex, ex);
                        }
                    }
                    
                    synchronized (sbs) {
                        sbs.add(stringBuilder);
                    }
                }
                
            });
            
            threads[count].start();
            
            count++;
        }
        
        for (final Thread t : threads) {
            t.join();
        }
        
        for (final StringBuilder s : sbs) {
            SimpleFileWriter.print(file, s.toString());
        }
        
        System.out.println("END");
        
    }
}
