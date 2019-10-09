package com.fish.bin.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2019/3/20  14:55
 * function : 测试功能
 *
 * @author mnlin
 */
public class Test {
    public static void main(String[] args) {
        String old = "et_save_qr".replaceAll("^.+?_(.+)$", "$1");
        System.out.println("old="+old);
        String reg = "_+\\w";
        Matcher matcher = Pattern.compile(reg).matcher(old);

        //针对每个匹配到的内容进行替换
        StringBuilder builder = new StringBuilder();
        int offset = 0;
        while (matcher.find()){
            int start = matcher.start();
            int end = matcher.end();
            builder.append(old.substring(offset,start))
                    .append(old.substring(end-1,end).toUpperCase());
            offset=end;
        }
        builder.append(old.substring(offset,old.length()));
        System.out.println(builder.toString().replaceAll("[^0-9a-zA-Z]","" ));
    }
}

