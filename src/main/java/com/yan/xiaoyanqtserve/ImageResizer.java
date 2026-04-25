package com.yan.xiaoyanqtserve;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@Slf4j
public class ImageResizer {
    public static void main(String[] args) throws Exception {
        String dir = "E:/yanchen/test";  // 修改为你的目录
        File[] files = new File(dir).listFiles((d, n) ->
                n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png"));
        if (files == null || files.length == 0){
            log.error("目录({})中没有找到图片文件", dir);
            return;
        }
        int count = 1;
        for (File f : files) {
            BufferedImage img = ImageIO.read(f);
            BufferedImage resized = new BufferedImage(600, 360, BufferedImage.TYPE_INT_RGB);
            resized.getGraphics().drawImage(img, 0, 0, 600, 360, null);
            ImageIO.write(resized, "jpg", new File(dir + "/pg" + count++ + ".jpg"));
        }
    }
}