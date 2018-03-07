package com.backend.business.base.component;

import com.backend.business.base.helper.SpringResourceHelper;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.List;
import java.util.Locale;

public class ReloadableMessageSource extends ReloadableResourceBundleMessageSource {

    private static final String MESSAGE_DIR = "/message/";  //消息资源文件存放目录
    private static final String KEY = "message";    //消息资源文件关键名称
    private static final String KEY_PREFIX = "*.";  //关键名称前缀
    private static final String KEY_SUFFIX = "*";   //关键名称后缀，可能是语言区域标识：en_US,zh_CN等
    private static final String EXTENSION = ".xml";  //消息资源文件扩展名
    private static final String MESSAGE_PATH = "classpath*:" + MESSAGE_DIR + KEY_PREFIX + KEY + KEY_SUFFIX + EXTENSION;  //ant style matcher's path

    public ReloadableMessageSource() {
        setDefaultEncoding("UTF-8");
        setUseCodeAsDefaultMessage(false);
        setCacheSeconds(60 * 60);
        setBasenames(MESSAGE_PATH);
    }

    @Override
    public void setBasenames(String... baseNames) {
        List<String> resourceBaseNames = SpringResourceHelper.getResourceBasenamesByWildcard(MESSAGE_DIR, baseNames);
        super.setBasenames(resourceBaseNames.stream().map(name -> {
            int index = name.lastIndexOf(KEY);
            logger.info("加载消息资源文件" + name + EXTENSION + "成功.");
            return name.substring(0, index + KEY.length());
        }).toArray(String[]::new));
    }

    public String getMessage(String code, Object... args) {
        return getMessage(code, Locale.getDefault(), args);
    }


    public String getMessage(String code, Locale locale, Object... args) {
        return getMessage(code, args, locale);
    }

    @Override
    protected String getMessageInternal(String code, Object[] args, Locale locale) {
        //        String message = super.getMessageInternal(code, new String[]{""}, locale);
        String message = super.getMessageInternal(code, args, locale);
        return message;
        //        if (message != null) {
        //            if (args == null || args.length == 0 || args[0] == null) {
        //                return message;
        //            } else {
        //                return message + ":" + Joiner.on(" ").skipNulls().join(args);
        //            }
        //        }
        //        return code;
    }

    @Override
    protected String getDefaultMessage(String code) {
        return "";
    }

}
