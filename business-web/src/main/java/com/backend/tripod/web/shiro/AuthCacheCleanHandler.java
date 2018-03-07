package com.backend.tripod.web.shiro;

/**
 * 授权信息清除处理器
 */
public interface AuthCacheCleanHandler {

    /**
     * 根据身份标识清除该身份对应的认证信息缓存
     *
     * @param principal 用户身份，{@link UserAuthPrincipal#principal}
     */
    void cleanAuthenticationCache(String principal);

    /**
     * 根据身份标识清除该身份对应的权限信息缓存
     *
     * @param principal 用户身份，{@link UserAuthPrincipal#principal}
     */
    void cleanAuthorizationCache(String principal);

}
