package com.backend.business.dao.mapper.prize;

import com.backend.business.dao.entity.prize.UserPrizeStatistics;

public interface UserPrizeStatisticsMapper {
    int deleteByPrimaryKey(String userId);

    int insert(UserPrizeStatistics record);

    int insertSelective(UserPrizeStatistics record);

    UserPrizeStatistics selectByPrimaryKey(String userId);

    int updateByPrimaryKeySelective(UserPrizeStatistics record);

    int updateByPrimaryKey(UserPrizeStatistics record);
}