/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ancevt.webparsers.headhunter.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import ru.ancevt.webdatagrabber.log.Log;
import ru.ancevt.webdatagrabber.output.IOutput;
import ru.ancevt.webparsers.headhunter.ds.Company;

/**
 *
 * @author ancevt
 */
public class TextFileCompanyOutput implements IOutput<Company> {

    private static final String END_LINE = String.format("%n");

    private PrintWriter printWriter;
    private final boolean includeInfoHtml;
    private File file;

    public TextFileCompanyOutput(boolean includeInfoHtml) {
        this.includeInfoHtml = includeInfoHtml;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
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
            printWriter.write(prettyFormat(company));
            printWriter.flush();
        }
    }

    public final String prettyFormat(Company v) {
        final StringBuilder s = new StringBuilder();
        s.append("------------------------------------------------").append(END_LINE);
        append(s, "id", v.getId());
        append(s, "type", v.getType());
        append(s, "name", v.getName());
        append(s, "info", v.getInfo());
        
        if(includeInfoHtml) {
            append(s, "infoHtml", v.getInfoHtml());
        }
        
        append(s, "area", v.getArea());
        append(s, "pageUrl", v.getPageUrl());
        append(s, "webSite", v.getWebSite());
        append(s, "image", v.getImage() != null ? v.getImage().getSource() : null);
        append(s, "trusted", v.isTrusted());
        
        return s.toString();
    }
    
    private static void append(StringBuilder stringBuilder, String key, Object object) {
        if (object != null && ((object instanceof String && !((String)object).isEmpty()))) {
            stringBuilder.append(key).append(": ").append(object).append(END_LINE);
        }
    }

    @Override
    public void close() throws IOException {
        if (printWriter != null) {
            Log.getLogger().info("Result file: " + file.getAbsolutePath());
            printWriter.close();
        }
    }
}
