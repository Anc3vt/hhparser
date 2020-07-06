package ru.ancevt.webparsers.headhunter.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ru.ancevt.util.time.TimeWatcher;
import ru.ancevt.webdatagrabber.ds.Image;
import ru.ancevt.webdatagrabber.log.Log;
import ru.ancevt.webdatagrabber.output.IOutput;
import ru.ancevt.webparsers.headhunter.ds.Company;

/**
 *
 * @author ancevt
 */
public class XlsFileCompanyOutput implements IOutput<Company> {

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

    public XlsFileCompanyOutput(boolean includeInfoHtml, int rowsPerSheet) {
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
    public void output(Company company) {
        if (counter == rowsPerSheet) {
            createNewSheet();
            counter = 0;
        }

        final long id = company.getId();
        final String type = company.getType();
        final String name = company.getName();
        final String info = company.getInfo();
        final String infoHtml = company.getInfoHtml();
        final String area = company.getArea();
        final String pageUrl = company.getPageUrl();
        final String webSite = company.getWebSite();
        final Image image = company.getImage();
        final boolean trusted = company.isTrusted();

        final int currentRowNum = currentSheet.getLastRowNum() + 1;
        final Row row = currentSheet.createRow(currentRowNum);

        row.createCell(0).setCellValue(id);
        row.createCell(1).setCellValue(type);
        row.createCell(2).setCellValue(name);
        row.createCell(3).setCellValue(info);

        if (includeInfoHtml) {
            row.createCell(4).setCellValue(infoHtml);
        }

        row.createCell(5).setCellValue(area);
        row.createCell(6).setCellValue(pageUrl);
        row.createCell(7).setCellValue(webSite);
        row.createCell(8).setCellValue(image == null ? "" : image.getSource());
        row.createCell(9).setCellValue(trusted);

        for (int i = 0; i < 9; i++) {
            final Cell cell = row.getCell(i);

            if (cell == null) {
                continue;
            }

            if (i == 6 || i == 7 || i == 8) {
                final Hyperlink hyperlink = new HSSFHyperlink(Hyperlink.LINK_URL);
                hyperlink.setAddress(cell.getStringCellValue());
                hyperlink.setLabel(cell.getStringCellValue());
                cell.setHyperlink(hyperlink);
                cell.setCellStyle(hyperlinkStyle);
            } else {
                cell.getCellStyle().setFont(defaultFont);
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

        currentSheet.setColumnWidth(0, 90 * SIZE_FACTOR);
        currentSheet.setColumnWidth(1, 144 * SIZE_FACTOR);
        currentSheet.setColumnWidth(2, 300 * SIZE_FACTOR);
        currentSheet.setColumnWidth(3, 160 * SIZE_FACTOR);
        currentSheet.setColumnWidth(4, 160 * SIZE_FACTOR);
        currentSheet.setColumnWidth(5, 130 * SIZE_FACTOR);
        currentSheet.setColumnWidth(6, 220 * SIZE_FACTOR);
        currentSheet.setColumnWidth(7, 160 * SIZE_FACTOR);
        currentSheet.setColumnWidth(8, 160 * SIZE_FACTOR);
        currentSheet.setColumnWidth(9, 40 * SIZE_FACTOR);

        createTitleRow();
    }

    private void createTitleRow() {
        final Row row = currentSheet.createRow(0);

        final CellStyle s = workbook.createCellStyle();

        final Font font = workbook.createFont();
        font.setBold(true);
        s.setFont(font);
        s.setAlignment(CellStyle.ALIGN_CENTER);
        s.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        s.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);

        row.createCell(0).setCellValue("id");
        row.createCell(1).setCellValue("type");
        row.createCell(2).setCellValue("name");
        row.createCell(3).setCellValue("info");
        row.createCell(4).setCellValue("infoHtml");
        row.createCell(5).setCellValue("area");
        row.createCell(6).setCellValue("pageUrl");
        row.createCell(7).setCellValue("webSite");
        row.createCell(8).setCellValue("imageUrl");
        row.createCell(9).setCellValue("trusted");

        for (int i = 0; i < 10; i++) {
            final Cell cell = row.getCell(i);
            cell.setCellStyle(s);
        }
    }

    @Override
    public void close() throws IOException {
        if (workbook != null) {
            Log.getLogger().info("Writing to XLS... please wait. It can take a while.");

            final TimeWatcher fsw = new TimeWatcher();
            fsw.setCallBack(() -> {
                Log.getLogger().info("Writing to XLS, writed " + file.length() + " bytes");
            });
            fsw.start();

            workbook.write(new FileOutputStream(file));

            fsw.stop();
            workbook.close();
            Log.getLogger().info("Result file: " + file.getAbsolutePath());
        }
    }

}
