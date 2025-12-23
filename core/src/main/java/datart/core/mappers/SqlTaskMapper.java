package datart.core.mappers;

import datart.core.entity.SqlTask;
import datart.core.entity.SqlTaskExample;
import datart.core.entity.SqlTaskWithBLOBs;
import datart.core.mappers.ext.CRUDMapper;
import java.util.List;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

@Mapper
public interface SqlTaskMapper extends CRUDMapper {
    @SelectProvider(type=SqlTaskSqlProvider.class, method="countByExample")
    long countByExample(SqlTaskExample example);

    @DeleteProvider(type=SqlTaskSqlProvider.class, method="deleteByExample")
    int deleteByExample(SqlTaskExample example);

    @Delete({
        "delete from sql_task",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    int deleteByPrimaryKey(String id);

    @Insert({
        "insert into sql_task (id, source_id, ",
        "script_type, `status`, ",
        "priority, timeout, ",
        "max_size, start_time, ",
        "end_time, duration, ",
        "fail_type, exec_instance_id, ",
        "progress, org_id, ",
        "create_by, create_time, ",
        "update_by, update_time, ",
        "permission, script, ",
        "error_message, execute_param)",
        "values (#{id,jdbcType=VARCHAR}, #{sourceId,jdbcType=VARCHAR}, ",
        "#{scriptType,jdbcType=VARCHAR}, #{status,jdbcType=VARCHAR}, ",
        "#{priority,jdbcType=INTEGER}, #{timeout,jdbcType=INTEGER}, ",
        "#{maxSize,jdbcType=INTEGER}, #{startTime,jdbcType=TIMESTAMP}, ",
        "#{endTime,jdbcType=TIMESTAMP}, #{duration,jdbcType=BIGINT}, ",
        "#{failType,jdbcType=VARCHAR}, #{execInstanceId,jdbcType=VARCHAR}, ",
        "#{progress,jdbcType=INTEGER}, #{orgId,jdbcType=VARCHAR}, ",
        "#{createBy,jdbcType=VARCHAR}, #{createTime,jdbcType=TIMESTAMP}, ",
        "#{updateBy,jdbcType=VARCHAR}, #{updateTime,jdbcType=TIMESTAMP}, ",
        "#{permission,jdbcType=INTEGER}, #{script,jdbcType=LONGVARCHAR}, ",
        "#{errorMessage,jdbcType=LONGVARCHAR}, #{executeParam,jdbcType=LONGVARCHAR})"
    })
    int insert(SqlTaskWithBLOBs record);

    @InsertProvider(type=SqlTaskSqlProvider.class, method="insertSelective")
    int insertSelective(SqlTaskWithBLOBs record);

    @SelectProvider(type=SqlTaskSqlProvider.class, method="selectByExampleWithBLOBs")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR, id=true),
        @Result(column="source_id", property="sourceId", jdbcType=JdbcType.VARCHAR),
        @Result(column="script_type", property="scriptType", jdbcType=JdbcType.VARCHAR),
        @Result(column="status", property="status", jdbcType=JdbcType.VARCHAR),
        @Result(column="priority", property="priority", jdbcType=JdbcType.INTEGER),
        @Result(column="timeout", property="timeout", jdbcType=JdbcType.INTEGER),
        @Result(column="max_size", property="maxSize", jdbcType=JdbcType.INTEGER),
        @Result(column="start_time", property="startTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="end_time", property="endTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="duration", property="duration", jdbcType=JdbcType.BIGINT),
        @Result(column="fail_type", property="failType", jdbcType=JdbcType.VARCHAR),
        @Result(column="exec_instance_id", property="execInstanceId", jdbcType=JdbcType.VARCHAR),
        @Result(column="progress", property="progress", jdbcType=JdbcType.INTEGER),
        @Result(column="org_id", property="orgId", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_by", property="createBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_by", property="updateBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="permission", property="permission", jdbcType=JdbcType.INTEGER),
        @Result(column="script", property="script", jdbcType=JdbcType.LONGVARCHAR),
        @Result(column="error_message", property="errorMessage", jdbcType=JdbcType.LONGVARCHAR),
        @Result(column="execute_param", property="executeParam", jdbcType=JdbcType.LONGVARCHAR)
    })
    List<SqlTaskWithBLOBs> selectByExampleWithBLOBs(SqlTaskExample example);

    @SelectProvider(type=SqlTaskSqlProvider.class, method="selectByExample")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR, id=true),
        @Result(column="source_id", property="sourceId", jdbcType=JdbcType.VARCHAR),
        @Result(column="script_type", property="scriptType", jdbcType=JdbcType.VARCHAR),
        @Result(column="status", property="status", jdbcType=JdbcType.VARCHAR),
        @Result(column="priority", property="priority", jdbcType=JdbcType.INTEGER),
        @Result(column="timeout", property="timeout", jdbcType=JdbcType.INTEGER),
        @Result(column="max_size", property="maxSize", jdbcType=JdbcType.INTEGER),
        @Result(column="start_time", property="startTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="end_time", property="endTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="duration", property="duration", jdbcType=JdbcType.BIGINT),
        @Result(column="fail_type", property="failType", jdbcType=JdbcType.VARCHAR),
        @Result(column="exec_instance_id", property="execInstanceId", jdbcType=JdbcType.VARCHAR),
        @Result(column="progress", property="progress", jdbcType=JdbcType.INTEGER),
        @Result(column="org_id", property="orgId", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_by", property="createBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_by", property="updateBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="permission", property="permission", jdbcType=JdbcType.INTEGER)
    })
    List<SqlTask> selectByExample(SqlTaskExample example);

    @Select({
        "select",
        "id, source_id, script_type, `status`, priority, timeout, max_size, start_time, ",
        "end_time, duration, fail_type, exec_instance_id, progress, org_id, create_by, ",
        "create_time, update_by, update_time, permission, script, error_message, execute_param",
        "from sql_task",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR, id=true),
        @Result(column="source_id", property="sourceId", jdbcType=JdbcType.VARCHAR),
        @Result(column="script_type", property="scriptType", jdbcType=JdbcType.VARCHAR),
        @Result(column="status", property="status", jdbcType=JdbcType.VARCHAR),
        @Result(column="priority", property="priority", jdbcType=JdbcType.INTEGER),
        @Result(column="timeout", property="timeout", jdbcType=JdbcType.INTEGER),
        @Result(column="max_size", property="maxSize", jdbcType=JdbcType.INTEGER),
        @Result(column="start_time", property="startTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="end_time", property="endTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="duration", property="duration", jdbcType=JdbcType.BIGINT),
        @Result(column="fail_type", property="failType", jdbcType=JdbcType.VARCHAR),
        @Result(column="exec_instance_id", property="execInstanceId", jdbcType=JdbcType.VARCHAR),
        @Result(column="progress", property="progress", jdbcType=JdbcType.INTEGER),
        @Result(column="org_id", property="orgId", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_by", property="createBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_by", property="updateBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="permission", property="permission", jdbcType=JdbcType.INTEGER),
        @Result(column="script", property="script", jdbcType=JdbcType.LONGVARCHAR),
        @Result(column="error_message", property="errorMessage", jdbcType=JdbcType.LONGVARCHAR),
        @Result(column="execute_param", property="executeParam", jdbcType=JdbcType.LONGVARCHAR)
    })
    SqlTaskWithBLOBs selectByPrimaryKey(String id);

    @UpdateProvider(type=SqlTaskSqlProvider.class, method="updateByExampleSelective")
    int updateByExampleSelective(@Param("record") SqlTaskWithBLOBs record, @Param("example") SqlTaskExample example);

    @UpdateProvider(type=SqlTaskSqlProvider.class, method="updateByExampleWithBLOBs")
    int updateByExampleWithBLOBs(@Param("record") SqlTaskWithBLOBs record, @Param("example") SqlTaskExample example);

    @UpdateProvider(type=SqlTaskSqlProvider.class, method="updateByExample")
    int updateByExample(@Param("record") SqlTask record, @Param("example") SqlTaskExample example);

    @UpdateProvider(type=SqlTaskSqlProvider.class, method="updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(SqlTaskWithBLOBs record);

    @Update({
        "update sql_task",
        "set source_id = #{sourceId,jdbcType=VARCHAR},",
          "script_type = #{scriptType,jdbcType=VARCHAR},",
          "`status` = #{status,jdbcType=VARCHAR},",
          "priority = #{priority,jdbcType=INTEGER},",
          "timeout = #{timeout,jdbcType=INTEGER},",
          "max_size = #{maxSize,jdbcType=INTEGER},",
          "start_time = #{startTime,jdbcType=TIMESTAMP},",
          "end_time = #{endTime,jdbcType=TIMESTAMP},",
          "duration = #{duration,jdbcType=BIGINT},",
          "fail_type = #{failType,jdbcType=VARCHAR},",
          "exec_instance_id = #{execInstanceId,jdbcType=VARCHAR},",
          "progress = #{progress,jdbcType=INTEGER},",
          "org_id = #{orgId,jdbcType=VARCHAR},",
          "create_by = #{createBy,jdbcType=VARCHAR},",
          "create_time = #{createTime,jdbcType=TIMESTAMP},",
          "update_by = #{updateBy,jdbcType=VARCHAR},",
          "update_time = #{updateTime,jdbcType=TIMESTAMP},",
          "permission = #{permission,jdbcType=INTEGER},",
          "script = #{script,jdbcType=LONGVARCHAR},",
          "error_message = #{errorMessage,jdbcType=LONGVARCHAR},",
          "execute_param = #{executeParam,jdbcType=LONGVARCHAR}",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    int updateByPrimaryKeyWithBLOBs(SqlTaskWithBLOBs record);

    @Update({
        "update sql_task",
        "set source_id = #{sourceId,jdbcType=VARCHAR},",
          "script_type = #{scriptType,jdbcType=VARCHAR},",
          "`status` = #{status,jdbcType=VARCHAR},",
          "priority = #{priority,jdbcType=INTEGER},",
          "timeout = #{timeout,jdbcType=INTEGER},",
          "max_size = #{maxSize,jdbcType=INTEGER},",
          "start_time = #{startTime,jdbcType=TIMESTAMP},",
          "end_time = #{endTime,jdbcType=TIMESTAMP},",
          "duration = #{duration,jdbcType=BIGINT},",
          "fail_type = #{failType,jdbcType=VARCHAR},",
          "exec_instance_id = #{execInstanceId,jdbcType=VARCHAR},",
          "progress = #{progress,jdbcType=INTEGER},",
          "org_id = #{orgId,jdbcType=VARCHAR},",
          "create_by = #{createBy,jdbcType=VARCHAR},",
          "create_time = #{createTime,jdbcType=TIMESTAMP},",
          "update_by = #{updateBy,jdbcType=VARCHAR},",
          "update_time = #{updateTime,jdbcType=TIMESTAMP},",
          "permission = #{permission,jdbcType=INTEGER}",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    int updateByPrimaryKey(SqlTask record);
}