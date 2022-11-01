package com.java2nb.system.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface CountMapper {


    @Select("select count(0) from book;")
    int bookCount();

    @Select("select count(0) from visitor_count where to_days(visit_time) = to_days(now())")
    int todayCount();

    @Select("select sum(visit_num) from pv_count where to_days(create_time) = to_days(now())")
    Long pvCount();

    @Select({"select DATE_FORMAT( visit_time, '%Y-%m-%d' ) AS staDate , COUNT(1) ip from visitor_count where visit_time >= #{minDate} GROUP BY DATE_FORMAT( visit_time, '%Y-%m-%d') ORDER BY staDate"})
    List<Map<Object,Object>> ipSta(Date minDate);

    @Select("select DATE_FORMAT( create_time, '%Y-%m-%d' ) AS staDate , sum(visit_num) ip from pv_count where create_time >= #{minDate} GROUP BY DATE_FORMAT( create_time, '%Y-%m-%d') ORDER BY staDate")
    List<Map<Object, Object>> pvSta(Date minDate);

    @Select("select count(0) from user_feedback where deal_status=0")
    int feedBack();
}
