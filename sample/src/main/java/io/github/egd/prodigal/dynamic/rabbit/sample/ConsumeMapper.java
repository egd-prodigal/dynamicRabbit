package io.github.egd.prodigal.dynamic.rabbit.sample;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConsumeMapper {

    @Insert("insert into consume(id) values (#{id})")
    int save(@Param("id") String id);

}
