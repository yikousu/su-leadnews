package com.su.utils.common;

import net.sourceforge.tess4j.TesseractException;

import java.io.*;

public class Test4jDemo {
    public static void main(String[] args) throws TesseractException, IOException {
        /*
        //1
        ITesseract iTesseract = new Tesseract();
        //2指定字体库位置
        iTesseract.setDatapath("D:\\Applications\\a_java\\tess4j");
        //3指定语言
        iTesseract.setLanguage("chi_sim");
        //4
        String content = iTesseract.doOCR(new File("D:/1.png"));
        System.out.println(content);
*/

        FileInputStream fileInputStream = new FileInputStream("1.txt");
        //InputStreamReader将字节流转换为字符流 以便更方便地处理字符数据
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        // BufferedReader用于逐行读取字符数据
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }




    }
}
