package datart.core.mappers;

import datart.core.entity.SqlTaskLog;
import datart.core.entity.SqlTaskLogExample;
import datart.core.mappers.ext.CRUDMapper;
import java.util.List;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

@Mapper
public interface SqlTaskLogMapper extends CRUDMapper {
    @SelectProvider(type=SqlTaskLogSqlProvider.class, method="countByExample")
    long countByExample(SqlTaskLogExample example);

    @DeleteProvider(type=SqlTaskLogSqlProvider.class, method="deleteByExample")
    int deleteByExample(SqlTaskLogExample example);

    @Delete({
        "delete from sql_task_log",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    int deleteByPrimaryKey(String id);

    @Insert({
        "insert into sql_task_log (id, task_id, ",
        "log_time, log_level, ",
        "create_by, create_time, ",
        "update_by, update_time, ",
        "permission, log_content)",
        "values (#{id,jdbcType=VARCHAR}, #{taskId,jdbcType=VARCHAR}, ",
        "#{logTime,jdbcType=TIMESTAMP}, #{logLevel,jdbcType=VARCHAR}, ",
        "#{createBy,jdbcType=VARCHAR}, #{createTime,jdbcType=TIMESTAMP}, ",
        "#{updateBy,jdbcType=VARCHAR}, #{updateTime,jdbcType=TIMESTAMP}, ",
        "#{permission,jdbcType=INTEGER}, #{logContent,jdbcType=LONGVARCHAR})"
    })
    int insert(SqlTaskLog record);

    @InsertProvider(type=SqlTaskLogSqlProvider.class, method="insertSelective")
    int insertSelective(SqlTaskLog record);

    @SelectProvider(type=SqlTaskLogSqlProvider.class, method="selectByExampleWithBLOBs")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR, id=true),
        @Result(column="task_id", property="taskId", jdbcType=JdbcType.VARCHAR),
        @Result(column="log_time", property="logTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="log_level", property="logLevel", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_by", property="createBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_by", property="updateBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="permission", property="permission", jdbcType=JdbcType.INTEGER),
        @Result(column="log_content", property="logContent", jdbcType=JdbcType.LONGVARCHAR)
    })
    List<SqlTaskLog> selectByExampleWithBLOBs(SqlTaskLogExample example);

    @SelectProvider(type=SqlTaskLogSqlProvider.class, method="selectByExample")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR, id=true),
        @Result(column="task_id", property="taskId", jdbcType=JdbcType.VARCHAR),
        @Result(column="log_time", property="logTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="log_level", property="logLevel", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_by", property="createBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_by", property="updateBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="permission", property="permission", jdbcType=JdbcType.INTEGER)
    })
    List<SqlTaskLog> selectByExample(SqlTaskLogExample example);

    @Select({
        "select",
        "id, task_id, log_time, log_level, create_by, create_time, update_by, update_time, ",
        "permission, log_content",
        "from sql_task_log",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR, id=true),
        @Result(column="task_id", property="taskId", jdbcType=JdbcType.VARCHAR),
        @Result(column="log_time", property="logTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="log_level", property="logLevel", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_by", property="createBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_by", property="updateBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="permission", property="permission", jdbcType=JdbcType.INTEGER),
        @Result(column="log_content", property="logContent", jdbcType=JdbcType.LONGVARCHAR)
    })
    SqlTaskLog selectByPrimaryKey(String id);

    @UpdateProvider(type=SqlTaskLogSqlProvider.class, method="updateByExampleSelective")
    int updateByExampleSelective(@Param("record") SqlTaskLog record, @Param("example") SqlTaskLogExample example);

    @UpdateProvider(type=SqlTaskLogSqlProvider.class, method="updateByExampleWithBLOBs")
    int updateByExampleWithBLOBs(@Param("record") SqlTaskLog record, @Param("example") SqlTaskLogExample example);

    @UpdateProvider(type=SqlTaskLogSqlProvider.class, method="updateByExample")
    int updateByExample(@Param("record") SqlTaskLog record, @Param("example") SqlTaskLogExample example);

    @UpdateProvider(type=SqlTaskLogSqlProvider.class, method="updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(SqlTaskLog record);

    @Update({
        "update sql_task_log",
        "set task_id = #{taskId,jdbcType=VARCHAR},",
          "log_time = #{logTime,jdbcType=TIMESTAMP},",
          "log_level = #{logLevel,jdbcType=VARCHAR},",
          "create_by = #{createBy,jdbcType=VARCHAR},",
          "create_time = #{createTime,jdbcType=TIMESTAMP},",
          "update_by = #{updateBy,jdbcType=VARCHAR},",
          "update_time = #{updateTime,jdbcType=TIMESTAMP},",
          "permission = #{permission,jdbcType=INTEGER},",
          "log_content = #{logContent,jdbcType=LONGVARCHAR}",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    int updateByPrimaryKeyWithBLOBs(SqlTaskLog record);

    @Update({
        "update sql_task_log",
        "set task_id = #{taskId,jdbcType=VARCHAR},",
          "log_time = #{logTime,jdbcType=TIMESTAMP},",
          "log_level = #{logLevel,jdbcType=VARCHAR},",
          "create_by = #{createBy,jdbcType=VARCHAR},",
          "create_time = #{createTime,jdbcType=TIMESTAMP},",
          "update_by = #{updateBy,jdbcType=VARCHAR},",
          "update_time = #{updateTime,jdbcType=TIMESTAMP},",
          "permission = #{permission,jdbcType=INTEGER}",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    int updateByPrimaryKey(SqlTaskLog record);
}