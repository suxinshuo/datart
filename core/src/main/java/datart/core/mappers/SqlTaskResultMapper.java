package datart.core.mappers;

import datart.core.entity.SqlTaskResult;
import datart.core.entity.SqlTaskResultExample;
import datart.core.mappers.ext.CRUDMapper;
import java.util.List;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

@Mapper
public interface SqlTaskResultMapper extends CRUDMapper {
    @SelectProvider(type=SqlTaskResultSqlProvider.class, method="countByExample")
    long countByExample(SqlTaskResultExample example);

    @DeleteProvider(type=SqlTaskResultSqlProvider.class, method="deleteByExample")
    int deleteByExample(SqlTaskResultExample example);

    @Delete({
        "delete from sql_task_result",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    int deleteByPrimaryKey(String id);

    @Insert({
        "insert into sql_task_result (id, task_id, ",
        "`data`, `row_count`, ",
        "column_count, create_by, ",
        "create_time, update_by, ",
        "update_time, permission)",
        "values (#{id,jdbcType=VARCHAR}, #{taskId,jdbcType=VARCHAR}, ",
        "#{data,jdbcType=VARCHAR}, #{rowCount,jdbcType=INTEGER}, ",
        "#{columnCount,jdbcType=INTEGER}, #{createBy,jdbcType=VARCHAR}, ",
        "#{createTime,jdbcType=TIMESTAMP}, #{updateBy,jdbcType=VARCHAR}, ",
        "#{updateTime,jdbcType=TIMESTAMP}, #{permission,jdbcType=INTEGER})"
    })
    int insert(SqlTaskResult record);

    @InsertProvider(type=SqlTaskResultSqlProvider.class, method="insertSelective")
    int insertSelective(SqlTaskResult record);

    @SelectProvider(type=SqlTaskResultSqlProvider.class, method="selectByExample")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR, id=true),
        @Result(column="task_id", property="taskId", jdbcType=JdbcType.VARCHAR),
        @Result(column="data", property="data", jdbcType=JdbcType.VARCHAR),
        @Result(column="row_count", property="rowCount", jdbcType=JdbcType.INTEGER),
        @Result(column="column_count", property="columnCount", jdbcType=JdbcType.INTEGER),
        @Result(column="create_by", property="createBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_by", property="updateBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="permission", property="permission", jdbcType=JdbcType.INTEGER)
    })
    List<SqlTaskResult> selectByExample(SqlTaskResultExample example);

    @Select({
        "select",
        "id, task_id, `data`, `row_count`, column_count, create_by, create_time, update_by, ",
        "update_time, permission",
        "from sql_task_result",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR, id=true),
        @Result(column="task_id", property="taskId", jdbcType=JdbcType.VARCHAR),
        @Result(column="data", property="data", jdbcType=JdbcType.VARCHAR),
        @Result(column="row_count", property="rowCount", jdbcType=JdbcType.INTEGER),
        @Result(column="column_count", property="columnCount", jdbcType=JdbcType.INTEGER),
        @Result(column="create_by", property="createBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_by", property="updateBy", jdbcType=JdbcType.VARCHAR),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="permission", property="permission", jdbcType=JdbcType.INTEGER)
    })
    SqlTaskResult selectByPrimaryKey(String id);

    @UpdateProvider(type=SqlTaskResultSqlProvider.class, method="updateByExampleSelective")
    int updateByExampleSelective(@Param("record") SqlTaskResult record, @Param("example") SqlTaskResultExample example);

    @UpdateProvider(type=SqlTaskResultSqlProvider.class, method="updateByExample")
    int updateByExample(@Param("record") SqlTaskResult record, @Param("example") SqlTaskResultExample example);

    @UpdateProvider(type=SqlTaskResultSqlProvider.class, method="updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(SqlTaskResult record);

    @Update({
        "update sql_task_result",
        "set task_id = #{taskId,jdbcType=VARCHAR},",
          "`data` = #{data,jdbcType=VARCHAR},",
          "`row_count` = #{rowCount,jdbcType=INTEGER},",
          "column_count = #{columnCount,jdbcType=INTEGER},",
          "create_by = #{createBy,jdbcType=VARCHAR},",
          "create_time = #{createTime,jdbcType=TIMESTAMP},",
          "update_by = #{updateBy,jdbcType=VARCHAR},",
          "update_time = #{updateTime,jdbcType=TIMESTAMP},",
          "permission = #{permission,jdbcType=INTEGER}",
        "where id = #{id,jdbcType=VARCHAR}"
    })
    int updateByPrimaryKey(SqlTaskResult record);
}