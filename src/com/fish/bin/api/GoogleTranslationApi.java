/*
 * Copyright 2014-2015 Wesley Lin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fish.bin.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Wesley Lin on 12/1/14.
 */
public class GoogleTranslationApi {
    /**
     * 需要传入的参数，源语言，目标语言，需要翻译的文字
     */
    private static final String BASE_TRANSLATION_URL = "http://translate.google.cn/translate_a/single?client=gtx&sl=zh-CN&tl=en&dt=t&ie=UTF-8&oe=UTF-8&q=%s";

    /**
     * 一次查多个
     */
    public static String getTranslationJSON(@NotNull String text) {
        try {
            String url = String.format(BASE_TRANSLATION_URL, URLEncoder.encode(text, "UTF-8"));
            String jsonResult = HttpUtils.doHttpGet(url);
            return handJson(jsonResult);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 处理数据
     */
    private static String handJson(String arg) {
        try {
            JsonArray lJsonArray = new JsonParser().parse(arg).getAsJsonArray();
            JsonArray lJsonArrayStrings = lJsonArray.get(0).getAsJsonArray();
            List<String> result = new ArrayList<String>();
            int size = lJsonArrayStrings.size();
            for (int i = 0; i < size; i++) {
                JsonArray lJsonArrayOneString = lJsonArrayStrings.get(i).getAsJsonArray();
                String translate = lJsonArrayOneString.get(0).toString();
                if (i == size - 1) {

                    translate = translate.substring(1, translate.length() - 1);
                } else {
                    translate = translate.substring(1, translate.length() - 3);
                }

                translate = translate
                        .replace("\\\"", "\"")
                        .replace("\'", "\\\'")
                        .replace("\\\\ n", "\\n");
                result.add(translate);
            }

            return result.get(0);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 一次查多个
     */
    public static List<String> getTranslationJSON(@NotNull List<String> querys) {

        String query = "";
        try {
            for (String s : querys) {
                query += (URLEncoder.encode(s, "UTF-8"));
                query += "%0A";//换行
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = String.format(BASE_TRANSLATION_URL, query);


        System.out.println("url=" + url);

        String getResult = HttpUtils.doHttpGet(url);
        System.out.println("google translation getResult: " + getResult);
        try {
            //lJsonArray = [[["I am going to school\n","我要去上学\n",null,null,3],["Put back to watch TV","放回回家看电视",null,null,3]],null,"zh-CN"]
            JsonArray lJsonArray = new JsonParser().parse(getResult).getAsJsonArray();

            //[["I am going to school\n","我要去上学\n",null,null,3],["Put back to watch TV","放回回家看电视",null,null,3]]
            JsonArray lJsonArrayStrings = lJsonArray.get(0).getAsJsonArray();
            List<String> result = new ArrayList<String>();

            int size = lJsonArrayStrings.size();
            for (int i = 0; i < size; i++) {
                //["I am going to school\n","我要去上学\n",null,null,3]
                JsonArray lJsonArrayOneString = lJsonArrayStrings.get(i).getAsJsonArray();
//                String translate = lJsonArrayOneString.get(0).toString().replace("\\n", "").replace("\"", "");
                String translate = lJsonArrayOneString.get(0).toString();
                if (i == size - 1) {
                    //最后一条翻译没有\n,所以要取第二位到倒数第二位
                    translate = translate.substring(1, translate.length() - 1);
                } else {
                    translate = translate.substring(1, translate.length() - 3);
                }
                //去除双引号前的反斜杠，单引号前加入反斜杠，保留\n(\n 翻译为了 \\\\ n)
                translate = translate
                        .replace("\\\"", "\"")
                        .replace("\'", "\\\'")
                        .replace("\\\\ n", "\\n");
                result.add(translate);
            }
            System.out.println("result=" + result);
            return result;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        } finally {
        }
    }
}
