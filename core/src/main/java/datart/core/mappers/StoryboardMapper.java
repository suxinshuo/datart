package datart.core.mappers;

import datart.core.entity.Storyboard;
import datart.core.entity.StoryboardExample;
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

public interface StoryboardMapper extends CRUDMapper {
    @SelectProvider(type=StoryboardSqlProvider.class, method="countByExample")
    long countByExample(StoryboardExample example);

    @DeleteProvider(type=StoryboardSqlProvider.class, method="deleteByExample")
    int deleteByExample(StoryboardExample example);

    @Delete({
        "delete from storyboard",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    int deleteByPrimaryKey(String id);

    @Insert({
        "insert into storyboard (id, `name`, ",
        "org_id, parent_id, ",
        "is_folder, `index`, ",
        "config, create_by, ",
        "create_time, update_by, ",
        "update_time, `status`)",
        "values (#{id,jdbcType=VARCHAR}, #{name,jdbcType=VARCHAR}, ",
        "#{orgId,jdbcType=VARCHAR}, #{parentId,jdbcType=VARCHAR}, ",
        "#{isFolder,jdbcType=TINYINT}, #{index,jdbcType=DOUBLE}, ",
        "#{config,jdbcType=VARCHAR}, #{createBy,jdbcType=VARCHAR}, ",
        "#{createTime,jdbcType=TIMESTAMP}, #{updateBy,jdbcType=VARCHAR}, ",
        "#{updateTime,jdbcType=TIMESTAMP}, #{status,jdbcType=TINYINT})"
    })
    int insert(Storyboard record);

    @InsertProvider(type=StoryboardSqlProvider.class, method="insertSelective")
    int insertSelective(Storyboard record);

    @SelectProvider(type=StoryboardSqlProvider.class, method="selectByExample")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR, id=true),
        @Result(column="name", property="name", jdbcType=JdbcType.VARCHAR),
        @Result(column="org_id", property="orgId", jdbcType=JdbcType.VARCHAR),
        @Result(column="parent_id", property="parentId", jdbcType=JdbcType.VARCHAR),
        @Result(column="is_folder", property="isFolder", jdbcType=JdbcType.TINYINT),
        @Result(column="index", property="index", jdbcType=JdbcType.DOUBLE),
        @Result(column="config", property="config", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_by", property="createBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_by", property="updateBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="status", property="status", jdbcType=JdbcType.TINYINT)
    })
    List<Storyboard> selectByExample(StoryboardExample example);

    @Select({
        "select",
        "id, `name`, org_id, parent_id, is_folder, `index`, config, create_by, create_time, ",
        "update_by, update_time, `status`",
        "from storyboard",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR, id=true),
        @Result(column="name", property="name", jdbcType=JdbcType.VARCHAR),
        @Result(column="org_id", property="orgId", jdbcType=JdbcType.VARCHAR),
        @Result(column="parent_id", property="parentId", jdbcType=JdbcType.VARCHAR),
        @Result(column="is_folder", property="isFolder", jdbcType=JdbcType.TINYINT),
        @Result(column="index", property="index", jdbcType=JdbcType.DOUBLE),
        @Result(column="config", property="config", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_by", property="createBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_by", property="updateBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="status", property="status", jdbcType=JdbcType.TINYINT)
    })
    Storyboard selectByPrimaryKey(String id);

    @UpdateProvider(type=StoryboardSqlProvider.class, method="updateByExampleSelective")
    int updateByExampleSelective(@Param("record") Storyboard record, @Param("example") StoryboardExample example);

    @UpdateProvider(type=StoryboardSqlProvider.class, method="updateByExample")
    int updateByExample(@Param("record") Storyboard record, @Param("example") StoryboardExample example);

    @UpdateProvider(type=StoryboardSqlProvider.class, method="updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(Storyboard record);

    @Update({
        "update storyboard",
        "set `name` = #{name,jdbcType=VARCHAR},",
          "org_id = #{orgId,jdbcType=VARCHAR},",
          "parent_id = #{parentId,jdbcType=VARCHAR},",
          "is_folder = #{isFolder,jdbcType=TINYINT},",
          "`index` = #{index,jdbcType=DOUBLE},",
          "config = #{config,jdbcType=VARCHAR},",
          "create_by = #{createBy,jdbcType=VARCHAR},",
          "create_time = #{createTime,jdbcType=TIMESTAMP},",
          "update_by = #{updateBy,jdbcType=VARCHAR},",
          "update_time = #{updateTime,jdbcType=TIMESTAMP},",
          "`status` = #{status,jdbcType=TINYINT}",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    int updateByPrimaryKey(Storyboard record);
}