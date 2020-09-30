import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExcelUtils {

    public static void storeDataIntoExcelFile(List<Map<String, String>> data, String outputFilePath, String sheetName) throws Exception {
        for (Map<String, String> listData : data) {
            System.out.println(listData);
        }

        if (data.size() < 1){
            return;
        }

        File file = new File(outputFilePath);
        //    if(file.exists()) file.delete();
        XSSFWorkbook workbook = null;
        if (file.exists()) {
            FileInputStream fileInputStream = new FileInputStream(file);
            workbook = new XSSFWorkbook(fileInputStream);
            int i = workbook.getSheetIndex(sheetName);
            if (i >= 0) {
                workbook.removeSheetAt(i);
                FileOutputStream output = new FileOutputStream(file);
                workbook.write(output);
                output.close();
            }
        } else {
            workbook = new XSSFWorkbook();
        }
        XSSFSheet sheet;
        sheet = workbook.createSheet(sheetName);
        Set<String> headers = data.get(0).keySet();

        XSSFFont font = workbook.createFont();
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);

        XSSFRow rowZero = sheet.createRow(0);
        int count =0;
        for (String s:headers) {
            XSSFCell cell = rowZero.createCell(count);
            cell.setCellValue(s);
            cell.setCellStyle(style);
            count++;
        }

        for (int i = 0; i < data.size(); i++) {
            XSSFRow row = sheet.createRow(i + 1);
            int count1 =0;
            for (String s:headers) {
                XSSFCell cell = row.createCell(count1);
                cell.setCellValue(data.get(i).get(s));
                count1++;
            }
        }

        try {
            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(new File(outputFilePath));
            workbook.write(out);
            out.close();
            //System.out.println("Accounts verification test completed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
