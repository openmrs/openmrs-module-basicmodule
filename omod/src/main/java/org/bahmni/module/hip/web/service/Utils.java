package org.bahmni.module.hip.web.service;

public class Utils {
    public static String ensureTrailingSlash(String url) {
        return url.endsWith("/") ? url : url + "/";
    }

    public static boolean isBlank(String value) {
        if (value == null) {
            return true;
        }

        if ("".equals(value.trim())) {
            return true;
        }

        return false;
    }
}
