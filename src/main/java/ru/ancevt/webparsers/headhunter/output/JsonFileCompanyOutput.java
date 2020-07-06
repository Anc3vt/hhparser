package ru.ancevt.webparsers.headhunter.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.JSONObject;
import ru.ancevt.webdatagrabber.log.Log;
import ru.ancevt.webdatagrabber.output.IOutput;
import ru.ancevt.webparsers.headhunter.ds.Company;

/**
 *
 * @author ancevt
 */
public class JsonFileCompanyOutput implements IOutput<Company> {

    private File file;
    private final boolean includeInfoHtml;
    private PrintWriter printWriter;
    private int count;
    private final boolean indentation;

    public JsonFileCompanyOutput(boolean includeInfoHtml, boolean indentation) {
        this.includeInfoHtml = includeInfoHtml;
        this.indentation = indentation;

        count = 0;
    }

    @Override
    public void output(Company company) {
        if (company.getName() == null || company.getName().isEmpty()) {
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
            if (count == 0) {
                printWriter.write("{\"companies\":[");
            } else {
                printWriter.write(indentation ? ",\n\n" : ",");
            }

            printWriter.write(toJsonCompany(company));
            printWriter.flush();
        }

        count++;
    }

    public final String toJsonCompany(Company c) {
        final JSONObject root = new JSONObject();
        root.put("id", c.getId());
        root.put("name", handleNull(c.getName()));
        root.put("type", handleNull(c.getType()));
        root.put("info", handleNull(c.getInfo()));
        root.put("area", handleNull(c.getArea()));
        root.put("pageUrl", handleNull(c.getPageUrl()));
        root.put("webSite", handleNull(c.getWebSite()));
        root.put("imageUrl", c.getImage() != null ? c.getImage().getSource() : JSONObject.NULL);
        root.put("trusted", c.isTrusted());

        if (includeInfoHtml) {
            root.put("infoHtml", handleNull(c.getInfoHtml()));
        } else {
            root.put("infoHtml", JSONObject.NULL);
        }

        return indentation ? root.toString(3) : root.toString();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    private static Object handleNull(Object object) {
        if (object == null || (object instanceof String) && ((String)object).length() == 0) {
            return JSONObject.NULL;
        }
        return object;
    }

    @Override
    public void close() throws IOException {
        if (printWriter != null) {
            printWriter.write("]}");
            Log.getLogger().info("Result file: " + file.getAbsolutePath());
            printWriter.close();
        }
    }

}
