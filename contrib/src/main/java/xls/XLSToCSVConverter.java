/*
 * Copyright (c) 2015, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package xls;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
 
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XLSToCSVConverter {
    public static void main(String[] args) {
        System.out.println("XLSToCSVConverter called");
        String path =  args[0];
        System.out.println("args[0] = " + args[0]);
        
        String[] fileComponents = path.split("\\.csv");
        if (path.endsWith("csv")) {
            fileComponents = path.split("\\.csv");
        }
        if (path.endsWith("xls")) {
            fileComponents = path.split("\\.xls");
        }
        if (path.endsWith("xlsx")) {
            fileComponents = path.split("\\.xlsx");
        }
        
        String xlsName = fileComponents[0] + ".xls";
        if (!Files.exists(Path.of(xlsName))) {
            xlsName = fileComponents[0] + ".xlsx";
        }
        String csvName = fileComponents[0] + ".csv";
        if (!Files.exists(Path.of(xlsName))) {
            System.out.println("File " + xlsName + " not found at " + Path.of(xlsName));
            System.exit(-1); // did not find a xls or xlsx file
        }

        
        // Creating an inputFile object with specific file path
        File inputFile = new File(xlsName);
 
        // Creating an outputFile object to write excel data to csv
        File outputFile = new File(csvName);
 
        // For storing data into CSV files
        StringBuffer data = new StringBuffer();
 
        try {
            // Creating input stream
            FileInputStream fis = new FileInputStream(inputFile);
            Workbook workbook = null;
 
            // Get the workbook object for Excel file based on file format
            if (inputFile.getName().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (inputFile.getName().endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis);
            } else {
                fis.close();
                throw new Exception("File not supported!");
            }
 
            // Get first sheet from the workbook
            Sheet sheet = workbook.getSheetAt(0);
 
            // Iterate through each rows from first sheet
            Iterator<Row> rowIterator = sheet.iterator();
 
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                boolean firstCellInRow = true;
                // For each row, iterate through each columns
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    if (firstCellInRow) {
                        firstCellInRow = false;
                    } else {
                        data.append(",");
                    }
 
                    Cell cell = cellIterator.next();
                    data.append("\"");
                    switch (cell.getCellType()) {
                    case BOOLEAN:
                        data.append(cell.getBooleanCellValue());
                        break;
 
                    case NUMERIC:
                        data.append(cell.getNumericCellValue());
                        break;
 
                    case STRING:
                        data.append(cell.getStringCellValue());
                        break;
 
                    case BLANK:
                        data.append("");
                        break;
 
                    default:
                        data.append(cell);
                    }
                    data.append("\"");
                }
                // appending new line after each row
                data.append('\n');
            }
 
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(data.toString().getBytes());
            fos.close();
 
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        System.out.println("Conversion of an Excel file to CSV file is done!");
    }
}