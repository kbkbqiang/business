package com.backend.business.shiro;

import com.backend.tripod.web.shiro.UserAuthPrincipal;
import com.backend.tripod.web.shiro.UserDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description
 * @Author zq
 * @Date 2018/3/8
 */
@Slf4j
@Component
public class ManagerUserDetailService implements UserDetailService {



    /**
     * 认证时调用，查询用户信息
     * @param userName
     * @return
     */
    @Override
    public UserAuthPrincipal loadPrincipalByUserName(String userName) {
        return null;
    }

    @Override
    public UserAuthPrincipal loadPrincipalByOpenId(String openId, Byte platformId) {
        return null;
    }

    /**
     * 授权时调用，查询用户角色列表
     * @param userId
     * @return
     */
    @Override
    public List<String> queryRoles(String userId) {
        return null;
    }

    /**
     * 授权时调用，查询用户资源列表
     * @param userId
     * @return
     */
    @Override
    public List<String> queryPermissions(String userId) {
        return null;
    }
}
