package com.demo.aicas.face;

import com.arcsoft.face.*;
import com.arcsoft.face.enums.ImageFormat;
import com.demo.aicas.common.Student;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FaceChecker {

    private static FaceChecker singleton;
    private final double threshold = 0.6;
    private FaceEngine faceEngine;

    private FaceChecker() {
        String os = System.getProperty("os.name");
        String appId;
        String sdkKey;
        if (os.toLowerCase().startsWith("win")) {
            appId = "97MsXPi2obzXLTRbcNX3YphnAzUmvzaV8PC1VCzH8wQM";
            sdkKey = "2i1fKDcsY5p68PkFhg1Qr9BvEX8qD9d9BFAYUu7S1YkS";
        } else {
            appId = "4KevqHSVxregJMLNdQ1LFkwpuqKp2AuQ5VaXHb2fCTFs";
            sdkKey = "EjkdgvqXxEu4sCvMSiPZjWjCdXtzFwFPgDhFqvmZHdbr";
        }


        faceEngine = new FaceEngine();
        //激活引擎
        faceEngine.active(appId, sdkKey);
        EngineConfiguration engineConfiguration = EngineConfiguration.builder().functionConfiguration(
                FunctionConfiguration.builder()
                        .supportAge(true)
                        .supportFace3dAngle(true)
                        .supportFaceDetect(true)
                        .supportFaceRecognition(true)
                        .supportGender(true)
                        .supportLiveness(true)
                        .build()).build();
        //初始化引擎
        faceEngine.init(engineConfiguration);
    }

    public static FaceChecker getInstance() {
        if (singleton == null)
            singleton = new FaceChecker();

        return singleton;
    }

    public ArrayList<Student> faceSearch(String photoInClass, ArrayList<Student> dataset) {

        ArrayList<Student> checkedStudent = new ArrayList<>();

        ImageInfo imageInfo = getRGBData(new File(photoInClass));
        //人脸检测
        List<FaceInfo> faceInfoList = new ArrayList<>();
        faceEngine.detectFaces(imageInfo.getRgbData(), imageInfo.getWidth(), imageInfo.getHeight(), ImageFormat.CP_PAF_BGR24, faceInfoList);


        for (int i = 0; i < faceInfoList.size(); i = i + 1) {
            //提取人脸特征
            FaceFeature faceFeature = new FaceFeature();
            faceEngine.extractFaceFeature(imageInfo.getRgbData(), imageInfo.getWidth(), imageInfo.getHeight(), ImageFormat.CP_PAF_BGR24, faceInfoList.get(i), faceFeature);

            //人脸对比
            FaceFeature targetFaceFeature = new FaceFeature();
            targetFaceFeature.setFeatureData(faceFeature.getFeatureData());

            int x = -1;
            float maxScore = 0;

            for (int j = 0; j < dataset.size(); j = j + 1) {

                ImageInfo imageInfo2 = getRGBData(new File(dataset.get(j).getPicturePath()));

                List<FaceInfo> faceInfoList2 = new ArrayList<>();
                faceEngine.detectFaces(imageInfo2.getRgbData(), imageInfo2.getWidth(), imageInfo2.getHeight(), ImageFormat.CP_PAF_BGR24, faceInfoList2);

                FaceFeature faceFeature2 = new FaceFeature();
                faceEngine.extractFaceFeature(imageInfo2.getRgbData(), imageInfo2.getWidth(), imageInfo2.getHeight(), ImageFormat.CP_PAF_BGR24, faceInfoList2.get(0), faceFeature2);

                FaceFeature sourceFaceFeature = new FaceFeature();
                sourceFaceFeature.setFeatureData(faceFeature2.getFeatureData());

                FaceSimilar faceSimilar = new FaceSimilar();
                faceEngine.compareFaceFeature(targetFaceFeature, sourceFaceFeature, faceSimilar);
                if (faceSimilar.getScore() > maxScore && faceSimilar.getScore() > threshold) {
                    maxScore = faceSimilar.getScore();
                    x = j;
                }
            }
            if (x >= 0)
                checkedStudent.add(dataset.get(x));
        }
        return checkedStudent;
    }

    private ImageInfo getRGBData(File file) {
        if (file == null)
            return null;
        ImageInfo imageInfo;
        try {
            //将图片文件加载到内存缓冲区
            BufferedImage image = ImageIO.read(file);
            imageInfo = bufferedImage2ImageInfo(image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return imageInfo;
    }

    private ImageInfo bufferedImage2ImageInfo(BufferedImage image) {
        ImageInfo imageInfo = new ImageInfo();
        int width = image.getWidth();
        int height = image.getHeight();
        // 使图片居中
        width = width & (~3);
        height = height & (~3);
        imageInfo.setWidth(width);
        imageInfo.setHeight(height);
        //根据原图片信息新建一个图片缓冲区
        BufferedImage resultImage = new BufferedImage(width, height, image.getType());
        //得到原图的rgb像素矩阵
        int[] rgb = image.getRGB(0, 0, width, height, null, 0, width);
        //将像素矩阵 绘制到新的图片缓冲区中
        resultImage.setRGB(0, 0, width, height, rgb, 0, width);
        //进行数据格式化为可用数据
        BufferedImage dstImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        if (resultImage.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
            ColorConvertOp colorConvertOp = new ColorConvertOp(cs, dstImage.createGraphics().getRenderingHints());
            colorConvertOp.filter(resultImage, dstImage);
        } else {
            dstImage = resultImage;
        }
        //获取rgb数据
        imageInfo.setRgbData(((DataBufferByte) (dstImage.getRaster().getDataBuffer())).getData());
        return imageInfo;
    }


    class ImageInfo {
        byte[] rgbData;
        int width;
        int height;

        byte[] getRgbData() {
            return rgbData;
        }

        void setRgbData(byte[] rgbData) {
            this.rgbData = rgbData;
        }

        int getWidth() {
            return width;
        }

        void setWidth(int width) {
            this.width = width;
        }

        int getHeight() {
            return height;
        }

        void setHeight(int height) {
            this.height = height;
        }
    }

}