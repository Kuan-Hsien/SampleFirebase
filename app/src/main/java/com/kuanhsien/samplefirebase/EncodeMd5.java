package com.kuanhsien.samplefirebase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncodeMd5 {

    public static String md5Password(String password){
        StringBuffer sb = new StringBuffer();

        // 得到一个信息摘要器
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");

            byte[] result = digest.digest(password.getBytes());

            // 把每一個 byte 和 0xff 做 ＆ 運算
            for (byte b : result) {
                // ＆ 運算
                int number = b & 0xff;
                String str = Integer.toHexString(number);
                if (str.length() == 1) {
                    sb.append("0");
                }
                sb.append(str);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

}

