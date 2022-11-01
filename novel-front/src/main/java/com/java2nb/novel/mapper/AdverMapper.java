package com.java2nb.novel.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdverMapper {

    @Insert("insert into advertisment_click(ip) values(#{ip})")
    void insert(String ip);


    @Select("select ip from advertisment_click where ip=#{ip} and to_days(create_time) = to_days(now())")
    List<String> selectIp(String ip);

}
