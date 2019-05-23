package com.ganlvtech.kahlanotify.kahla.lib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateParser {
    public static final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSX");

    public static Date parse(String time) throws ParseException {
        return parser.parse(time);
    }

    public static Date tryParse(String time) {
        try {
            parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date(0);
    }
}
