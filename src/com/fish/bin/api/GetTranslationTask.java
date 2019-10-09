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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wesley Lin on 12/1/14.
 */
public class GetTranslationTask extends Task.Backgroundable {

    private static final String BASE_TRANSLATION_URL = "http://translate.google.cn/translate_a/single?client=gtx&sl=zh-CN&tl=en&dt=t&ie=UTF-8&oe=UTF-8&q=%s";
    private List<String> sources;
    private CallBack callBack;

    /**
     * @param project 项目
     * @param title   任务名称，将显示在idea的下方任务栏中
     */
    public GetTranslationTask(Project project, String title, List<String> sources, CallBack callBack) {
        super(project, title);
        this.sources = sources;
        this.callBack = callBack;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        List<String> result = null;
        try {
            StringBuilder query = new StringBuilder();
            for (String source : sources) {
                query.append(URLEncoder.encode(source, "UTF-8"));
                query.append("%0A");//换行
            }
            String url = String.format(BASE_TRANSLATION_URL, query.toString());
            System.out.println("url=" + url);
            String jsonResult = HttpUtils.doHttpGet(url);
            System.out.println("http:" + jsonResult);
            result = handJson(jsonResult);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        callBack.onBack(result);
    }

    /**
     * 处理数据
     */
    private static List<String> handJson(String source) {
        try {
            JsonArray lJsonArray = new JsonParser().parse(source).getAsJsonArray();
            JsonArray lJsonArrayStrings = lJsonArray.get(0).getAsJsonArray();
            List<String> result = new ArrayList<>();
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
            return result;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

}
