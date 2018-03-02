package com.backend.business.helper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpringResourceHelper {

    /**
     * 根据通配符资源路径获取资源列表。
     *
     * @param wildcardResourcePaths 通配符资源路径
     * @return 返回资源列表。
     */
    public static List<Resource> getResourcesByWildcard(String... wildcardResourcePaths) {
        List<Resource> resources = new ArrayList<>();
        try {
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            for (String basename : wildcardResourcePaths) {
                resources.addAll(Arrays.asList(resourcePatternResolver.getResources(basename)));
            }
        } catch (Exception e) {
            throw new RuntimeException("获取资源列表时反生异常", e);
        }
        return resources;
    }

    /**
     * 根据通配符资源路径获取资源路径列表。
     *
     * @param resourceDir           资源目录
     * @param wildcardResourcePaths 通配符资源路径
     * @return 返回实际匹配的资源路径列表。
     */
    public static List<String> getResourcePathsByWildcard(String resourceDir, String... wildcardResourcePaths) {
        List<String> resourcePaths = new ArrayList<>();
        try {
            for (Resource resource : getResourcesByWildcard(wildcardResourcePaths)) {
                String uri = resource.getURI().toString();
                if (resource instanceof FileSystemResource || resource instanceof UrlResource) {
                    String resourcePath = "classpath:" + resourceDir + substringAfter(uri, resourceDir);
                    resourcePaths.add(resourcePath);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("获取资源文件时发生异常", e);
        }
        return resourcePaths;
    }

    /**
     * 根据通配符资源路径获取资源基本名称列表。
     *
     * @param resourceDir           资源目录
     * @param wildcardResourcePaths 通配符资源路径
     * @return 返回实际匹配的资源基本名称列表。
     */
    public static List<String> getResourceBasenamesByWildcard(String resourceDir, String... wildcardResourcePaths) {
        List<String> resourceBaseNames = new ArrayList<>();
        for (String resourcePath : getResourcePathsByWildcard(resourceDir, wildcardResourcePaths)) {
            resourceBaseNames.add(substringBeforeLast(resourcePath, "."));
        }
        return resourceBaseNames;
    }

    /**
     * 截取最后一个分隔符前的字符串内容。
     *
     * @param str       待截取的字符串
     * @param separator 分隔符
     * @return 返回最后一个分隔符前的字符串内容。
     */
    public static String substringBeforeLast(String str, String separator) {
        Assert.isTrue(StringUtils.isNotEmpty(str), "待截取内容为空");
        Assert.isTrue(StringUtils.isNotEmpty(separator), "分隔符为空");
        int pos = str.lastIndexOf(separator);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * 截取指定分隔符后的字符串内容。
     *
     * @param str       待截取的字符串
     * @param separator 分隔符
     * @return 返回指定分隔符后的字符串内容。
     */
    public static String substringAfter(String str, String separator) {
        Assert.isTrue(StringUtils.isNotEmpty(str), "待截取内容为空");
        Assert.isTrue(StringUtils.isNotEmpty(separator), "分隔符为空");
        int pos = str.indexOf(separator);
        if (pos == -1) {
            return "";
        }
        return str.substring(pos + separator.length());
    }

}
