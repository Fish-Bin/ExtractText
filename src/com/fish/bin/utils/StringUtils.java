package com.fish.bin.utils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StringUtils {
    public static int count = 0;

    /**
     * 把字符串中的空格替换为_
     */
    public static String formatStr(String str) {
        str = str.toLowerCase();
        String[] split = str.split(" ");
        if (split.length < 5) {
            return str.replace(" ", "_");
        }
        count++;
        return count + "";
    }

    /**
     * 将string按需要格式化,前面加缩进符,后面加换行符
     *
     * @param tabNum    缩进量
     * @param srcString
     * @return
     */
    public static String formatSingleLine(int tabNum, String srcString) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tabNum; i++) {
            sb.append("\t");
        }
        sb.append(srcString);
        sb.append("\n");
        return sb.toString();
    }

    public static String firstToUpperCase(String key) {
        return key.substring(0, 1).toUpperCase(Locale.getDefault()) + key.substring(1);
    }

    /**
     * 驼峰转下划线命名
     */
    public static String camel2underline(String src) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbWord = new StringBuilder();
        char[] chars = src.trim().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c >= 'A' && c <= 'Z') {
                // 一旦遇到大写单词，保存之前已有字符组成的单词
                if (sbWord.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append("_");
                    }
                    sb.append(sbWord.toString());
                }
                sbWord = new StringBuilder();
            }
            sbWord.append(c);
        }

        if (sbWord.length() > 0) {
            if (sb.length() > 0) {
                sb.append("_");
            }
            sb.append(sbWord.toString());
        }

        return sb.toString();
    }

    /**
     * 将已有id转换wi变量值
     */
    public static String conventIdToParam(String id) {
        String old = id.replaceAll("^.+?_(.+)$", "$1");

        //如果长度 多短,格式不规范,则 不进行处理
        if (old.length() <= 1) {
            return id;
        }

        String reg = "_+\\w";
        Matcher matcher = Pattern.compile(reg).matcher(old);

        //针对每个匹配到的内容进行替换
        StringBuilder builder = new StringBuilder();
        int offset = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            builder.append(old.substring(offset, start))
                    .append(old.substring(end - 1, end).toUpperCase());
            offset = end;
        }
        builder.append(old.substring(offset, old.length()));
        String result = builder.toString().replaceAll("[^0-9a-zA-Z]", "");
        return result.substring(0, 1).toLowerCase() + result.substring(1, result.length());
    }

}
