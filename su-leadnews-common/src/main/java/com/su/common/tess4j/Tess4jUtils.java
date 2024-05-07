package com.su.common.tess4j;

import com.su.common.tess4j.config.Tess4jConfig;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Import(Tess4jConfig.class)
public class Tess4jUtils {
    @Autowired
    private ITesseract iTesseract;


    //单张如片检测
    public String doOCR(byte[] bytes) throws IOException, TesseractException {
        // 1)读取图片
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        // 2)将图片转换成BufferImage
        BufferedImage bufferedImage = ImageIO.read(is);

        // 3)使用Tess4j实现图片识别
        String content = iTesseract.doOCR(bufferedImage);

        // 4)返回图片内容
        return content.replaceAll("\\r|\\n","-").replaceAll(" ","");
    }

    //多张图片检测
    public String doOCR(List<byte[]> imageBytes) throws TesseractException, IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte[] imageByte : imageBytes) {
            stringBuilder.append(doOCR(imageByte));
        }
        return stringBuilder.toString();
    }
}
