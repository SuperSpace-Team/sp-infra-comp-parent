package com.sp.infra.comp.excel;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Liu Tao
 * @date 2018/7/19 下午1:34
 */
public class Md5Utils {


    private static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 加密输入流
     */
    public static String convert(MultipartFile multipartFile) {
        InputStream fis = null;
        try {
            fis = multipartFile.getInputStream();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            return bytesToString(md.digest());
        } catch (IOException ex) {
            return null;
        } catch (NoSuchAlgorithmException ex) {
            return null;
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
            }
        }
    }

    public static String bytesToString(byte[] encryptStr) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < encryptStr.length; i++) {
            int iRet = encryptStr[i];
            if (iRet < 0) {
                iRet += 256;
            }
            int iD1 = iRet / 16;
            int iD2 = iRet % 16;
            sb.append(hexDigits[iD1] + "" + hexDigits[iD2]);
        }
        return sb.toString();

    }
}
