package com.java2nb.novel.mapper;

import com.java2nb.novel.entity.Advertisment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdverMapper {

    @Insert("insert into advertisment_click(ip, adver_id) values(#{ip}, #{id})")
    void insert(String ip, Integer id);


    @Select("select ip from advertisment_click where ip=#{ip} and adver_id=#{id} and to_days(create_time) = to_days(now())")
    List<String> selectIp(String ip, Integer id);

    @Select({"select * from advertisment s1 where status = 0",
    "and not exists(",
    "select s2.adver_id from advertisment_click s2 where s1.id = s2.adver_id and s2.ip=#{ip} and to_days(s2.create_time) = to_days(now())",
    ")"})
    List<Advertisment> selectAdverByIpId(String ip);

    @Select("select * from advertisment where status=0")
    List<Advertisment> selectAdver();
}
