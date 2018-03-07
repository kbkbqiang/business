package com.backend.business.service.userprize;

import com.alibaba.fastjson.JSON;
import com.backend.business.dao.entity.prize.UserPrizeStatistics;
import com.backend.business.dao.mapper.prize.UserPrizeStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description
 * @Author zq
 * @Date 2018/3/7
 */
@Service
public class UserPrizeStatisticsService {

    @Autowired
    UserPrizeStatisticsMapper userPrizeStatisticsMapper;

    @Transactional
    public void testQueryData(String userId){
        UserPrizeStatistics userPrizeStatistics = userPrizeStatisticsMapper.selectByPrimaryKey(userId);
        System.out.println(JSON.toJSONString(userPrizeStatistics));
    }
}
