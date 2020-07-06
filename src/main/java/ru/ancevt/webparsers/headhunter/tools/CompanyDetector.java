package ru.ancevt.webparsers.headhunter.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
import ru.ancevt.webdatagrabber.log.Log;

/**
 * @author ancevt
 */
public class CompanyDetector {

    public static void main(String[] args) throws IOException, InterruptedException {
        final HttpRequestMaker requestMaker = new HttpRequestMaker();
        requestMaker.addDefaultHttpHeader(new HttpHeader("Host", "spb.hh.ru"));
        requestMaker.addStandardDefaultHeaders();

        final File dir = new File("/home/ancevt/tmp/companies/");
        if (dir.exists()) {
            deleteDirectory(dir);
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }

        final int total = 5000000;

        final Thread[] threads = new Thread[50];

        int count = 0;

        for (int j = 0; j < total; j += total / threads.length) {

            final int J = j;

            threads[count] = new Thread(new Runnable() {

                @Override
                public void run() {

                    PrintWriter printWriter = null;
                    try {
                        System.out.println("run thread: " + Thread.currentThread().getName());
                        final File file = new File(dir.getAbsolutePath() + "/result" + J + ".txt");
                        printWriter = new PrintWriter(file);
                        for (int i = J; i < J + total / threads.length; i++) {
                            try {
                                final HttpRequest req = requestMaker.create("https://spb.hh.ru/search/vacancy?st=searchVacancy&from=employerPage&employer_id=" + i, GET, null);
                                final HttpClient client = new HttpClient();
                                client.connect(req);

                                if (client.getStatus() != 200) {
                                    System.out.println(client.getStatus() + " " + client.getStatusText());
                                    i--;
                                    continue;
                                }

                                String html = null;
                                try {
                                    html = GZIPUtil.decompress(client.readBytes());
                                } catch (TimeoutException ex) {
                                    Log.getLogger().error(ex, ex);
                                }

                                final Element node = Jsoup.parse(html);

                                final Elements node2 = node.getElementsByClass("clusters-value__name");
                                final String company = node2.get(0).text();

                                if (J == 0) {
                                    final float f = i;
                                    System.out.println("progress " + ((f / (total / threads.length)) * 100 + "%"));
                                }

                                if (company.isEmpty()) {
                                    System.out.println("skip " + i);
                                } else {
                                    final String s = i + "=" + company + "\r\n";
                                    printWriter.println(s);
                                    printWriter.flush();
                                    System.out.print(s);
                                }
                                client.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            } catch (TimeoutException ex) {
                                Log.getLogger().error(ex, ex);
                            }
                        }
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } finally {
                        printWriter.close();
                    }
                }

            });

            threads[count].start();

            count++;
        }

        for (final Thread t : threads) {
            t.join();
        }

        System.out.println("END");
    }

    private static void deleteDirectory(final File dir) throws IOException {
        if (dir.isDirectory()) {
            for (File c : dir.listFiles()) {
                deleteDirectory(c);
            }
        }
        if (!dir.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + dir.getAbsolutePath());
        }
    }
}
