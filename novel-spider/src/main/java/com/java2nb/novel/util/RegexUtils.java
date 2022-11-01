package com.java2nb.novel.util;

import org.apache.commons.codec.language.bm.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

    public static List<String> parseExpressList(String express, String content) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(express);
        Matcher m = pattern.matcher(content);
        while (m.find()) {
            result.add(m.group(1));
        }
        return result;
    }


    public static String parseExpressContent(String express, String content) {
        Pattern pattern = Pattern.compile(express);
        Matcher m = pattern.matcher(content);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

}
