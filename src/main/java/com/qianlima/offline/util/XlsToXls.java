package com.qianlima.offline.util;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2021/1/12.
 */
public class XlsToXls {

    /**
     * 读取Excel文件
     * @param url 文件的路径
     * @return
     */
    public static void readXls(String url,Integer sheetNum) {
        File[] fs = getFiles(url);//文件夹名称（该文件夹下面包含多个excel文件）
        for (File temp : fs) {
            String name = temp.getName();//得到excel的文件名称，包含后缀名称，例如excel文件名称为a1;
            String n = name.substring(0, name.lastIndexOf(".")).toUpperCase();//去掉后缀名，并转换为大写，可以在xml文件中起id名(例如：<tags id="A1">)
            try {
                // 创建输入流，读取Excel
                InputStream is = new FileInputStream(temp.getAbsolutePath());

                //生成xml文件的路径和文件名称
                FileWriter fw = new FileWriter("E:\\a.xml",true);

                fw.write("");
                // jxl提供的Workbook类
                Workbook wb = Workbook.getWorkbook(is);//创建excel
                Sheet[] sheets = wb.getSheets();//一个excel文件中有多个sheet
                for (int i = 0; i < sheets.length; i++) {//遍历每个sheet中的内容
                    if (sheetNum.intValue() == i){
                        Sheet sheet = sheets[i];
                        for (int j = 1; j < sheet.getRows(); j++) {// 循环进行读写(excel中的列)
                            Cell cell1 = sheet.getCell(0, j);//第一列
                           /* Cell cell2 = sheet.getCell(1, j);//第二列
                            Cell cell3 = sheet.getCell(2, j);//第三列
                            Cell cell4 = sheet.getCell(3, j);//第四列
                            Cell cell5 = sheet.getCell(4, j);//第五列
                            Cell cell6 = sheet.getCell(5, j);//第六列*/

                            String contents1 = cell1.getContents();
                            /*String contents2 = cell2.getContents();
                            String contents3 = cell3.getContents();
                            String contents4 = cell4.getContents();
                            String contents5 = cell5.getContents();
                            String contents6 = cell6.getContents();*/
                            fw.write( "\""+contents1+ "\""+",");
                        }
                    }
                }
                fw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (BiffException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取一个excel文件
     * @param url 带后缀的文件全路径
     * @param sheetNum 要读取第几个sheet文件
     */
    public static Map<String,Object> readXlsOne(String url, Integer sheetNum) {
        File f = new File(url);
        //文件夹名称（该文件夹下面包含多个excel文件）
        String name = f.getName();//得到excel的文件名称，包含后缀名称，例如excel文件名称为a1;
        String n = name.substring(0, name.lastIndexOf(".")).toUpperCase();//去掉后缀名，并转换为大写，可以在xml文件中起id名(例如：<tags id="A1">)
        Map<String,Object> resultMap = new HashMap<>();
        StringBuffer buffer = new StringBuffer();
        buffer.append("\"");
        try {
            // 创建输入流，读取Excel
            InputStream is = new FileInputStream(f.getAbsolutePath());

            //生成xml文件的路径和文件名称
            FileWriter fw = new FileWriter("E:\\a.xml",true);

            fw.write("");
            // jxl提供的Workbook类
            Workbook wb = Workbook.getWorkbook(is);//创建excel
            Sheet[] sheets = wb.getSheets();//一个excel文件中有多个sheet
            for (int i = 0; i < sheets.length; i++) {//遍历每个sheet中的内容
                if (sheetNum.intValue() == i){
                    Sheet sheet = sheets[i];
                    for (int j = 1; j < sheet.getRows(); j++) {// 循环进行读写(excel中的列)
                        Cell cell1 = sheet.getCell(0, j);//第一列
                       /* Cell cell2 = sheet.getCell(1, j);//第二列
                        Cell cell3 = sheet.getCell(2, j);//第三列
                        Cell cell4 = sheet.getCell(3, j);//第四列
                        Cell cell5 = sheet.getCell(4, j);//第五列
                        Cell cell6 = sheet.getCell(5, j);//第六列*/

                        String contents1 = cell1.getContents();
                        /*String contents2 = cell2.getContents();
                        String contents3 = cell3.getContents();
                        String contents4 = cell4.getContents();
                        String contents5 = cell5.getContents();
                        String contents6 = cell6.getContents();*/
                        fw.write( "\""+contents1+ "\""+",");
                        buffer.append(contents1+",");
                    }
                }
            }
            fw.close();
            buffer.deleteCharAt(buffer.length() - 1);
            buffer.append("\"");
            resultMap.put("1",buffer.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    /**
     * @param dir  指定的路径
     * @param //currentPage 类似于分页
     * @return
     */
    static File[] getFiles(String dir) {
        File f = new File(dir);
        File[] allFiles = f.listFiles(new FileFilter() {//过滤掉目录
            public boolean accept(File f) {
                return f.isFile() ? true : false;
            }
        });
        return allFiles;
    }
}