package ru.ancevt.webparsers.headhunter.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ru.ancevt.util.time.TimeWatcher;
import ru.ancevt.webdatagrabber.log.Log;
import ru.ancevt.webdatagrabber.output.IOutput;
import ru.ancevt.webparsers.headhunter.ds.Area;
import ru.ancevt.webparsers.headhunter.ds.ContactInfo;
import ru.ancevt.webparsers.headhunter.ds.Salary;
import ru.ancevt.webparsers.headhunter.ds.Vacancy;

/**
 *
 * @author ancevt
 */
public class XlsFileVacancyOutput implements IOutput<Vacancy> {

    private static final int SIZE_FACTOR = 36;

    private final boolean includeInfoHtml;
    private final int rowsPerSheet;
    private Workbook workbook;
    private Sheet currentSheet;
    private int sheetCounter;
    private int counter;
    private File file;
    private final Font defaultFont;
    private final CellStyle hyperlinkStyle;
    private CellStyle headerRowStyle;
    private CellStyle dateStyle;

    public XlsFileVacancyOutput(boolean includeInfoHtml, int rowsPerSheet) {
        this.includeInfoHtml = includeInfoHtml;
        this.rowsPerSheet = rowsPerSheet;

        createWorkbook();

        defaultFont = workbook.createFont();
        defaultFont.setColor(IndexedColors.BLACK.getIndex());

        final Font linkFont = workbook.createFont();
        hyperlinkStyle = workbook.createCellStyle();
        linkFont.setUnderline(Font.U_SINGLE);
        linkFont.setColor(IndexedColors.BLUE.getIndex());
        hyperlinkStyle.setFont(linkFont);
        hyperlinkStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        hyperlinkStyle.setFillBackgroundColor(IndexedColors.WHITE.getIndex());

        

    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public void output(Vacancy v) {
        if (counter == rowsPerSheet) {
            createNewSheet();
            counter = 0;
        }

        final long id = v.getId();
        final String name = v.getName();
        final String company = v.getCompany();
        final String companyUrl = v.getCompanyUrl();
        final String pageUrl = v.getPageUrl();
        final String city = v.getCity();
        final String metroStation = v.getMetroStation();
        final String description = v.getDescription();
        final Date date = v.getDate();
        final String dateText = v.getDateText();
        int salaryFrom = 0;
        int salaryTo = 0;

        String salaryUnit = null;
        String salaryExtra = null;
        if (v.getSalary() != null && !v.getSalary().isEmpty()) {
            final Salary s = v.getSalary();
            salaryFrom = s.getFrom();
            salaryTo = s.getTo();
            salaryUnit = s.getUnit();
            salaryExtra = s.getExtra();
        }

        final String rawAddress = v.getRawAddress();
        final String info = v.getInfo();
        final String infoHtml = v.getInfoHtml();

        String contactName = null;
        String contactEmail = null;
        String contactPhones = null;
        String contactComment = null;
        if (v.getContactInfo() != null) {
            final ContactInfo ci = v.getContactInfo();
            contactName = ci.getFullName();
            contactEmail = ci.getEmail();
            contactPhones = ci.getPhonesString();
            contactComment = ci.getComment();
        }

        String address = null;
        if (v.getAddress() != null) {
            address = v.getAddress().getDisplayName();
        }

        int areaId = 0;
        String areaRegionId = null;
        String areaRegionName = null;
        String areaCity = null;
        String areaCountryCode = null;
        if (v.getArea() != null) {
            final Area a = v.getArea();
            areaId = a.getId();
            areaRegionName = a.getRegionName();
            areaCity = a.getCity();
            areaCountryCode = a.getCountryCode();
        }

        final String skills = v.getSkillsString();
        final boolean trusted = v.isTrusted();
        final boolean archived = v.isArchived();
        final boolean active = v.isActive();
        final boolean disabled = v.isDisabled();
        final boolean abridged = v.isAbridged();

        final int currentRowNum = currentSheet.getLastRowNum() + 1;
        final Row row = currentSheet.createRow(currentRowNum);

        int c = 0;

        row.createCell(c++).setCellValue(id);
        row.createCell(c++).setCellValue(name);
        row.createCell(c++).setCellValue(company);
        row.createCell(c++).setCellValue(companyUrl);
        row.createCell(c++).setCellValue(pageUrl);
        row.createCell(c++).setCellValue(city);
        row.createCell(c++).setCellValue(metroStation);
        row.createCell(c++).setCellValue(description);
        if (date != null) {
            row.createCell(c++).setCellValue(date);
        } else {
            c++;
        }
        row.createCell(c++).setCellValue(dateText);
        row.createCell(c++).setCellValue(salaryFrom);
        row.createCell(c++).setCellValue(salaryTo);
        row.createCell(c++).setCellValue(salaryUnit);
        row.createCell(c++).setCellValue(salaryExtra);
        row.createCell(c++).setCellValue(rawAddress);
        row.createCell(c++).setCellValue(info);

        if (includeInfoHtml) {
            try {
                row.createCell(c++).setCellValue(infoHtml);
            } catch(IllegalArgumentException ex) {
                Log.getLogger().warn("Can not put html to cell. " + ex.getMessage());
            }
        } else {
            c++;
        }

        row.createCell(c++).setCellValue(contactName);
        row.createCell(c++).setCellValue(contactEmail);
        row.createCell(c++).setCellValue(contactPhones);
        row.createCell(c++).setCellValue(contactComment);
        row.createCell(c++).setCellValue(address);
        row.createCell(c++).setCellValue(areaId);
        row.createCell(c++).setCellValue(areaRegionId);
        row.createCell(c++).setCellValue(areaRegionName);
        row.createCell(c++).setCellValue(areaCity);
        row.createCell(c++).setCellValue(areaCountryCode);
        row.createCell(c++).setCellValue(skills);
        row.createCell(c++).setCellValue(trusted);
        row.createCell(c++).setCellValue(archived);
        row.createCell(c++).setCellValue(active);
        row.createCell(c++).setCellValue(disabled);
        row.createCell(c++).setCellValue(abridged);

        for (int i = 0; i < 9; i++) {
            final Cell cell = row.getCell(i);

            if (cell == null) {
                continue;
            }
            
            if(dateStyle == null) {
                DataFormat format = workbook.createDataFormat();
                dateStyle = workbook.createCellStyle();
                dateStyle.setDataFormat(format.getFormat("dd.mm.yyyy"));
            }

            if (i == 3 || i == 4) {
                final Hyperlink hyperlink = new HSSFHyperlink(Hyperlink.LINK_URL);
                hyperlink.setAddress(cell.getStringCellValue());
                hyperlink.setLabel(cell.getStringCellValue());
                cell.setHyperlink(hyperlink);
                cell.setCellStyle(hyperlinkStyle);
            } else 
            if (i == 8) {
                cell.setCellStyle(dateStyle);
            }
        }

        counter++;

    }

    private void createWorkbook() {
        if (workbook == null) {
            workbook = new HSSFWorkbook();
            createNewSheet();
        }
    }

    private void createNewSheet() {
        sheetCounter++;
        currentSheet = workbook.createSheet("Sheet #" + sheetCounter);

        int c = 0;
        
        currentSheet.setColumnWidth(c++, 70 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 200 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 200 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 240 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 220 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 100 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 100 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 100 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 100 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 370 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 60 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 60 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 50 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 80 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 300 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 70 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 70 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 120 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 120 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 120 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 120 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 200 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 60 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 100 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 140 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 140 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 40 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 170 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 40 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 40 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 40 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 40 * SIZE_FACTOR);
        currentSheet.setColumnWidth(c++, 40 * SIZE_FACTOR);

        createTitleRow();
    }

    private void createTitleRow() {
        final Row row = currentSheet.createRow(0);

        int c = 0;

        row.createCell(c++).setCellValue("id");
        row.createCell(c++).setCellValue("name");
        row.createCell(c++).setCellValue("company");
        row.createCell(c++).setCellValue("companyUrl");
        row.createCell(c++).setCellValue("pageUrl");
        row.createCell(c++).setCellValue("city");
        row.createCell(c++).setCellValue("metroStation");
        row.createCell(c++).setCellValue("description");
        row.createCell(c++).setCellValue("date");
        row.createCell(c++).setCellValue("dateText");
        row.createCell(c++).setCellValue("salaryFrom");
        row.createCell(c++).setCellValue("salaryTo");
        row.createCell(c++).setCellValue("salaryUnit");
        row.createCell(c++).setCellValue("salaryExtra");
        row.createCell(c++).setCellValue("rawAddress");
        row.createCell(c++).setCellValue("info");
        row.createCell(c++).setCellValue("infoHtml");
        row.createCell(c++).setCellValue("contactName");
        row.createCell(c++).setCellValue("contactEmail");
        row.createCell(c++).setCellValue("contactPhones");
        row.createCell(c++).setCellValue("contactComment");
        row.createCell(c++).setCellValue("address");
        row.createCell(c++).setCellValue("areaId");
        row.createCell(c++).setCellValue("areaRegionId");
        row.createCell(c++).setCellValue("areaRegionName");
        row.createCell(c++).setCellValue("areaCity");
        row.createCell(c++).setCellValue("areaCountryCode");
        row.createCell(c++).setCellValue("skills");
        row.createCell(c++).setCellValue("trusted");
        row.createCell(c++).setCellValue("archived");
        row.createCell(c++).setCellValue("active");
        row.createCell(c++).setCellValue("disabled");
        row.createCell(c++).setCellValue("abridged");

        if (headerRowStyle == null) {
            headerRowStyle = workbook.createCellStyle();
            final Font font = workbook.createFont();
            font.setBold(true);
            headerRowStyle.setFont(font);
            headerRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
            headerRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            headerRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        }

        for (int i = 0; i < c; i++) {
            final Cell cell = row.getCell(i);
            cell.setCellStyle(headerRowStyle);
        }
    }

    @Override
    public void close() throws IOException {
        if (workbook != null) {
            Log.getLogger().info("Writing to XLS... please wait. It can take a while.");

            final TimeWatcher fsw = new TimeWatcher();
            fsw.setCallBack(() -> {
                Log.getLogger().info("Writing to XML, writed " + file.length() + " bytes");
            });
            fsw.start();

            workbook.write(new FileOutputStream(file));

            fsw.stop();
            workbook.close();
            Log.getLogger().info("Result file: " + file.getAbsolutePath());
        }
    }

}
