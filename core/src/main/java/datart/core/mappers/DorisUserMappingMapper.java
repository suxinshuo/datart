package datart.core.mappers;

import datart.core.entity.DorisUserMapping;
import datart.core.entity.DorisUserMappingExample;
import datart.core.mappers.ext.CRUDMapper;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;

public interface DorisUserMappingMapper extends CRUDMapper {
    @SelectProvider(type=DorisUserMappingSqlProvider.class, method="countByExample")
    long countByExample(DorisUserMappingExample example);

    @DeleteProvider(type=DorisUserMappingSqlProvider.class, method="deleteByExample")
    int deleteByExample(DorisUserMappingExample example);

    @Delete({
        "delete from doris_user_mapping",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    int deleteByPrimaryKey(String id);

    @Insert({
        "insert into doris_user_mapping (id, sys_username, ",
        "source_id, doris_username, ",
        "encrypted_password, create_by, ",
        "create_time, update_by, ",
        "update_time)",
        "values (#{id,jdbcType=VARCHAR}, #{sysUsername,jdbcType=VARCHAR}, ",
        "#{sourceId,jdbcType=VARCHAR}, #{dorisUsername,jdbcType=VARCHAR}, ",
        "#{encryptedPassword,jdbcType=VARCHAR}, #{createBy,jdbcType=VARCHAR}, ",
        "#{createTime,jdbcType=TIMESTAMP}, #{updateBy,jdbcType=VARCHAR}, ",
        "#{updateTime,jdbcType=TIMESTAMP})"
    })
    int insert(DorisUserMapping record);

    @InsertProvider(type=DorisUserMappingSqlProvider.class, method="insertSelective")
    int insertSelective(DorisUserMapping record);

    @SelectProvider(type=DorisUserMappingSqlProvider.class, method="selectByExample")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR, id=true),
        @Result(column="sys_username", property="sysUsername", jdbcType=JdbcType.VARCHAR),
        @Result(column="source_id", property="sourceId", jdbcType=JdbcType.VARCHAR),
        @Result(column="doris_username", property="dorisUsername", jdbcType=JdbcType.VARCHAR),
        @Result(column="encrypted_password", property="encryptedPassword", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_by", property="createBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_by", property="updateBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP)
    })
    List<DorisUserMapping> selectByExample(DorisUserMappingExample example);

    @Select({
        "select",
        "id, sys_username, source_id, doris_username, encrypted_password, create_by, ",
        "create_time, update_by, update_time",
        "from doris_user_mapping",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR, id=true),
        @Result(column="sys_username", property="sysUsername", jdbcType=JdbcType.VARCHAR),
        @Result(column="source_id", property="sourceId", jdbcType=JdbcType.VARCHAR),
        @Result(column="doris_username", property="dorisUsername", jdbcType=JdbcType.VARCHAR),
        @Result(column="encrypted_password", property="encryptedPassword", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_by", property="createBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_by", property="updateBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP)
    })
    DorisUserMapping selectByPrimaryKey(String id);

    @UpdateProvider(type=DorisUserMappingSqlProvider.class, method="updateByExampleSelective")
    int updateByExampleSelective(@Param("record") DorisUserMapping record, @Param("example") DorisUserMappingExample example);

    @UpdateProvider(type=DorisUserMappingSqlProvider.class, method="updateByExample")
    int updateByExample(@Param("record") DorisUserMapping record, @Param("example") DorisUserMappingExample example);

    @UpdateProvider(type=DorisUserMappingSqlProvider.class, method="updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(DorisUserMapping record);

    @Update({
        "update doris_user_mapping",
        "set sys_username = #{sysUsername,jdbcType=VARCHAR},",
          "source_id = #{sourceId,jdbcType=VARCHAR},",
          "doris_username = #{dorisUsername,jdbcType=VARCHAR},",
          "encrypted_password = #{encryptedPassword,jdbcType=VARCHAR},",
          "create_by = #{createBy,jdbcType=VARCHAR},",
          "create_time = #{createTime,jdbcType=TIMESTAMP},",
          "update_by = #{updateBy,jdbcType=VARCHAR},",
          "update_time = #{updateTime,jdbcType=TIMESTAMP}",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    int updateByPrimaryKey(DorisUserMapping record);
}