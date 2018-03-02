package com.backend.business.web.shiro;

import java.util.List;

public interface UserDetailService {

    UserAuthPrincipal loadPrincipalByUserName(String userName);

    UserAuthPrincipal loadPrincipalByOpenId(String openId, Byte platformId);

    List<String> queryRoles(String userId);

    List<String> queryPermissions(String userId);

}
