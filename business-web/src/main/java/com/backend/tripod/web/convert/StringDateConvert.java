package com.backend.tripod.web.convert;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.core.convert.converter.Converter;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class StringDateConvert implements Converter<String, Date> {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_MINUTES_FORMAT = "yyyy-MM-dd HH:mm";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_TIME_TZ_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_TIME_MILLISECOND_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String DATE_TIME_MILLISECOND_TZ_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";
    private final FastDateFormat dateFormat = FastDateFormat.getInstance(DATE_FORMAT);
    private final FastDateFormat dateMinutesFormat = FastDateFormat.getInstance(DATE_MINUTES_FORMAT);
    private final FastDateFormat dateTimeFormat = FastDateFormat.getInstance(DATE_TIME_FORMAT);
    private final FastDateFormat dateTimeTZFormat = FastDateFormat.getInstance(DATE_TIME_TZ_FORMAT, TimeZone.getTimeZone("GMT+08:00"));
    private final FastDateFormat dateTimeMillSecondFormat = FastDateFormat.getInstance(DATE_TIME_MILLISECOND_FORMAT);
    private final FastDateFormat dateTimeMillSecondTZFormat = FastDateFormat.getInstance(DATE_TIME_MILLISECOND_TZ_FORMAT, TimeZone.getTimeZone("GMT+08:00"));
    private final Pattern datePattern = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}");
    private final Pattern dateMinutesPattern = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}");
    private final Pattern dateTimePattern = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}");
    private final Pattern dateTimeTZPattern = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}[+-]\\d{4}");
    private final Pattern dateTimeMillSecondPattern = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{1,3}");
    private final Pattern dateTimeMillSecondTZPattern = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{1,3}[+-]\\d{4}");

    @Override
    public Date convert(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }
        if (source.indexOf("T") > 0 || source.indexOf("Z") > 0) {
            source = source.replace("T", " ").replace("Z", "");
        }
        source = source.trim();
        try {
            if (StringUtils.isNumeric(source)) {
                return new Date(Long.valueOf(source));
            }
            if (dateTimeMillSecondTZPattern.matcher(source).find()) {
                return dateTimeMillSecondTZFormat.parse(source);
            }
            if (dateTimeTZPattern.matcher(source).find()) {
                return dateTimeTZFormat.parse(source);
            }
            if (dateTimeMillSecondPattern.matcher(source).find()) {
                return dateTimeMillSecondFormat.parse(source);
            }
            if (dateTimePattern.matcher(source).find()) {
                return dateTimeFormat.parse(source);
            }
            if (dateMinutesPattern.matcher(source).find()) {
                return dateMinutesFormat.parse(source);
            }
            if (datePattern.matcher(source).find()) {
                return dateFormat.parse(source);
            }
        } catch (ParseException e) {
            throw new RuntimeException(source + " convert to date error", e);
        }
        return null;
    }

}
