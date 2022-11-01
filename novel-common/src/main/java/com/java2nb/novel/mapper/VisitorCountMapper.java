package com.java2nb.novel.mapper;

import com.java2nb.novel.entity.PvCount;
import com.java2nb.novel.entity.VisitorCount;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

@Mapper
public interface VisitorCountMapper {

    @Select("select * from visitor_count where ip=#{ip} and to_days(visit_time) = to_days(now())")
    VisitorCount selectByIpToday(String ip);

    @Insert("insert into visitor_count(ip) values(#{ip})")
    void insert(String ip);

    @Insert("insert into pv_count(ip) values(#{ip})")
    void insertPv(String ip);

    @Update("update pv_count set visit_num = visit_num+1 where ip=#{ip} and to_days(create_time)=to_days(now())")
    void updatePv(String ip);

    @Select("select * from pv_count where ip=#{ip} and to_days(create_time)=to_days(now())")
    PvCount selectByIp(String ip);
}
