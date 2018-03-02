package com.backend.business.web.shiro;

import com.backend.business.web.properties.ShiroProperties;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.List;

public class UsernamePasswordDeviceRealm extends AuthorizingRealm implements ApplicationListener<ApplicationReadyEvent> {

    private static final String REALM_NAME = UsernamePasswordDeviceRealm.class.getName();

    private ShiroProperties shiroProperties;
    private UserDetailService userDetailService;


    public UsernamePasswordDeviceRealm(ShiroProperties shiroProperties) {
        this.shiroProperties = shiroProperties;
        initProperties();
    }

    private void initProperties() {
        setCachingEnabled(shiroProperties.getCache().isEnabled());
        setAuthenticationCachingEnabled(shiroProperties.getCache().isAuthenticationCachingEnabled());
        setAuthorizationCachingEnabled(shiroProperties.getCache().isAuthorizationCachingEnabled());
    }

    @Override
    public void setName(String name) {
        super.setName(REALM_NAME);
    }

    @Override
    public String getAuthenticationCacheName() {
        return shiroProperties.getCache().getAuthenticationCachePrefix();
    }

    @Override
    public String getAuthorizationCacheName() {
        return shiroProperties.getCache().getAuthorizationCachePrefix();
    }

    @Override
    protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
        UserAuthPrincipal userAuthPrincipal = (UserAuthPrincipal) principals.getPrimaryPrincipal();
        return userAuthPrincipal.getPrincipal();
    }

    @Override
    protected Object getAuthenticationCacheKey(AuthenticationToken token) {
        UsernamePasswordDeviceToken tk = (UsernamePasswordDeviceToken) token;
        return tk.getUsername();
    }

    /**
     * 授权
     *
     * @param principals 当前用户身份信息
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        UserAuthPrincipal principal = (UserAuthPrincipal) principals.getPrimaryPrincipal();
        String userId = principal.getUserId();
        List<String> roles = userDetailService.queryRoles(userId);
        List<String> permissions = userDetailService.queryPermissions(userId);
        boolean existRole = roles != null && !roles.isEmpty();
        boolean existPermission = permissions != null && !permissions.isEmpty();
        SimpleAuthorizationInfo authorizationInfo = null;
        if (existRole || existPermission) {
            authorizationInfo = new SimpleAuthorizationInfo();
            if (existRole) {
                authorizationInfo.addRoles(roles);
            }
            if (existPermission) {
                authorizationInfo.addStringPermissions(permissions);
            }
        }
        return authorizationInfo;
    }

    /**
     * 认证
     *
     * @param token 认证token
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordDeviceToken tk = (UsernamePasswordDeviceToken) token;
        SimpleAuthenticationInfo authenticationInfo = null;
        UserAuthPrincipal userAuthPrincipal = userDetailService.loadPrincipalByUserName(tk.getUsername());
        if (userAuthPrincipal != null) {
            userAuthPrincipal.setDeviceType(tk.getDeviceType());
            authenticationInfo = new SimpleAuthenticationInfo(userAuthPrincipal, userAuthPrincipal.getCredentials(), REALM_NAME);
            authenticationInfo.setCredentialsSalt(new CustomByteSource(userAuthPrincipal.getSalt()));
        }
        return authenticationInfo;
    }

    @Override
    public Class getAuthenticationTokenClass() {
        return UsernamePasswordDeviceToken.class;
    }

    @Override
    protected void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
    }

    @Override
    protected void clearCachedAuthenticationInfo(PrincipalCollection principals) {
        super.clearCachedAuthenticationInfo(principals);
    }

    @Override
    protected void clearCachedAuthorizationInfo(PrincipalCollection principals) {
        super.clearCachedAuthorizationInfo(principals);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        this.userDetailService = event.getApplicationContext().getBean(UserDetailService.class);
    }

}
