package datart.core.mappers;

import datart.core.entity.SqlTaskExample.Criteria;
import datart.core.entity.SqlTaskExample.Criterion;
import datart.core.entity.SqlTaskExample;
import datart.core.entity.SqlTaskWithBLOBs;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.jdbc.SQL;

public class SqlTaskSqlProvider {
    public String countByExample(SqlTaskExample example) {
        SQL sql = new SQL();
        sql.SELECT("count(*)").FROM("sql_task");
        applyWhere(sql, example, false);
        return sql.toString();
    }

    public String deleteByExample(SqlTaskExample example) {
        SQL sql = new SQL();
        sql.DELETE_FROM("sql_task");
        applyWhere(sql, example, false);
        return sql.toString();
    }

    public String insertSelective(SqlTaskWithBLOBs record) {
        SQL sql = new SQL();
        sql.INSERT_INTO("sql_task");
        
        if (record.getId() != null) {
            sql.VALUES("id", "#{id,jdbcType=VARCHAR}");
        }
        
        if (record.getSourceId() != null) {
            sql.VALUES("source_id", "#{sourceId,jdbcType=VARCHAR}");
        }
        
        if (record.getScriptType() != null) {
            sql.VALUES("script_type", "#{scriptType,jdbcType=VARCHAR}");
        }
        
        if (record.getStatus() != null) {
            sql.VALUES("`status`", "#{status,jdbcType=VARCHAR}");
        }
        
        if (record.getPriority() != null) {
            sql.VALUES("priority", "#{priority,jdbcType=INTEGER}");
        }
        
        if (record.getTimeout() != null) {
            sql.VALUES("timeout", "#{timeout,jdbcType=INTEGER}");
        }
        
        if (record.getMaxSize() != null) {
            sql.VALUES("max_size", "#{maxSize,jdbcType=INTEGER}");
        }
        
        if (record.getStartTime() != null) {
            sql.VALUES("start_time", "#{startTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getEndTime() != null) {
            sql.VALUES("end_time", "#{endTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getDuration() != null) {
            sql.VALUES("duration", "#{duration,jdbcType=BIGINT}");
        }
        
        if (record.getFailType() != null) {
            sql.VALUES("fail_type", "#{failType,jdbcType=VARCHAR}");
        }
        
        if (record.getExecInstanceId() != null) {
            sql.VALUES("exec_instance_id", "#{execInstanceId,jdbcType=VARCHAR}");
        }
        
        if (record.getProgress() != null) {
            sql.VALUES("progress", "#{progress,jdbcType=INTEGER}");
        }
        
        if (record.getOrgId() != null) {
            sql.VALUES("org_id", "#{orgId,jdbcType=VARCHAR}");
        }
        
        if (record.getCreateBy() != null) {
            sql.VALUES("create_by", "#{createBy,jdbcType=VARCHAR}");
        }
        
        if (record.getCreateTime() != null) {
            sql.VALUES("create_time", "#{createTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getUpdateBy() != null) {
            sql.VALUES("update_by", "#{updateBy,jdbcType=VARCHAR}");
        }
        
        if (record.getUpdateTime() != null) {
            sql.VALUES("update_time", "#{updateTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getPermission() != null) {
            sql.VALUES("permission", "#{permission,jdbcType=INTEGER}");
        }
        
        if (record.getScript() != null) {
            sql.VALUES("script", "#{script,jdbcType=LONGVARCHAR}");
        }
        
        if (record.getErrorMessage() != null) {
            sql.VALUES("error_message", "#{errorMessage,jdbcType=LONGVARCHAR}");
        }
        
        if (record.getExecuteParam() != null) {
            sql.VALUES("execute_param", "#{executeParam,jdbcType=LONGVARCHAR}");
        }
        
        return sql.toString();
    }

    public String selectByExampleWithBLOBs(SqlTaskExample example) {
        SQL sql = new SQL();
        if (example != null && example.isDistinct()) {
            sql.SELECT_DISTINCT("id");
        } else {
            sql.SELECT("id");
        }
        sql.SELECT("source_id");
        sql.SELECT("script_type");
        sql.SELECT("`status`");
        sql.SELECT("priority");
        sql.SELECT("timeout");
        sql.SELECT("max_size");
        sql.SELECT("start_time");
        sql.SELECT("end_time");
        sql.SELECT("duration");
        sql.SELECT("fail_type");
        sql.SELECT("exec_instance_id");
        sql.SELECT("progress");
        sql.SELECT("org_id");
        sql.SELECT("create_by");
        sql.SELECT("create_time");
        sql.SELECT("update_by");
        sql.SELECT("update_time");
        sql.SELECT("permission");
        sql.SELECT("script");
        sql.SELECT("error_message");
        sql.SELECT("execute_param");
        sql.FROM("sql_task");
        applyWhere(sql, example, false);
        
        if (example != null && example.getOrderByClause() != null) {
            sql.ORDER_BY(example.getOrderByClause());
        }
        
        return sql.toString();
    }

    public String selectByExample(SqlTaskExample example) {
        SQL sql = new SQL();
        if (example != null && example.isDistinct()) {
            sql.SELECT_DISTINCT("id");
        } else {
            sql.SELECT("id");
        }
        sql.SELECT("source_id");
        sql.SELECT("script_type");
        sql.SELECT("`status`");
        sql.SELECT("priority");
        sql.SELECT("timeout");
        sql.SELECT("max_size");
        sql.SELECT("start_time");
        sql.SELECT("end_time");
        sql.SELECT("duration");
        sql.SELECT("fail_type");
        sql.SELECT("exec_instance_id");
        sql.SELECT("progress");
        sql.SELECT("org_id");
        sql.SELECT("create_by");
        sql.SELECT("create_time");
        sql.SELECT("update_by");
        sql.SELECT("update_time");
        sql.SELECT("permission");
        sql.FROM("sql_task");
        applyWhere(sql, example, false);
        
        if (example != null && example.getOrderByClause() != null) {
            sql.ORDER_BY(example.getOrderByClause());
        }
        
        return sql.toString();
    }

    public String updateByExampleSelective(Map<String, Object> parameter) {
        SqlTaskWithBLOBs record = (SqlTaskWithBLOBs) parameter.get("record");
        SqlTaskExample example = (SqlTaskExample) parameter.get("example");
        
        SQL sql = new SQL();
        sql.UPDATE("sql_task");
        
        if (record.getId() != null) {
            sql.SET("id = #{record.id,jdbcType=VARCHAR}");
        }
        
        if (record.getSourceId() != null) {
            sql.SET("source_id = #{record.sourceId,jdbcType=VARCHAR}");
        }
        
        if (record.getScriptType() != null) {
            sql.SET("script_type = #{record.scriptType,jdbcType=VARCHAR}");
        }
        
        if (record.getStatus() != null) {
            sql.SET("`status` = #{record.status,jdbcType=VARCHAR}");
        }
        
        if (record.getPriority() != null) {
            sql.SET("priority = #{record.priority,jdbcType=INTEGER}");
        }
        
        if (record.getTimeout() != null) {
            sql.SET("timeout = #{record.timeout,jdbcType=INTEGER}");
        }
        
        if (record.getMaxSize() != null) {
            sql.SET("max_size = #{record.maxSize,jdbcType=INTEGER}");
        }
        
        if (record.getStartTime() != null) {
            sql.SET("start_time = #{record.startTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getEndTime() != null) {
            sql.SET("end_time = #{record.endTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getDuration() != null) {
            sql.SET("duration = #{record.duration,jdbcType=BIGINT}");
        }
        
        if (record.getFailType() != null) {
            sql.SET("fail_type = #{record.failType,jdbcType=VARCHAR}");
        }
        
        if (record.getExecInstanceId() != null) {
            sql.SET("exec_instance_id = #{record.execInstanceId,jdbcType=VARCHAR}");
        }
        
        if (record.getProgress() != null) {
            sql.SET("progress = #{record.progress,jdbcType=INTEGER}");
        }
        
        if (record.getOrgId() != null) {
            sql.SET("org_id = #{record.orgId,jdbcType=VARCHAR}");
        }
        
        if (record.getCreateBy() != null) {
            sql.SET("create_by = #{record.createBy,jdbcType=VARCHAR}");
        }
        
        if (record.getCreateTime() != null) {
            sql.SET("create_time = #{record.createTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getUpdateBy() != null) {
            sql.SET("update_by = #{record.updateBy,jdbcType=VARCHAR}");
        }
        
        if (record.getUpdateTime() != null) {
            sql.SET("update_time = #{record.updateTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getPermission() != null) {
            sql.SET("permission = #{record.permission,jdbcType=INTEGER}");
        }
        
        if (record.getScript() != null) {
            sql.SET("script = #{record.script,jdbcType=LONGVARCHAR}");
        }
        
        if (record.getErrorMessage() != null) {
            sql.SET("error_message = #{record.errorMessage,jdbcType=LONGVARCHAR}");
        }
        
        if (record.getExecuteParam() != null) {
            sql.SET("execute_param = #{record.executeParam,jdbcType=LONGVARCHAR}");
        }
        
        applyWhere(sql, example, true);
        return sql.toString();
    }

    public String updateByExampleWithBLOBs(Map<String, Object> parameter) {
        SQL sql = new SQL();
        sql.UPDATE("sql_task");
        
        sql.SET("id = #{record.id,jdbcType=VARCHAR}");
        sql.SET("source_id = #{record.sourceId,jdbcType=VARCHAR}");
        sql.SET("script_type = #{record.scriptType,jdbcType=VARCHAR}");
        sql.SET("`status` = #{record.status,jdbcType=VARCHAR}");
        sql.SET("priority = #{record.priority,jdbcType=INTEGER}");
        sql.SET("timeout = #{record.timeout,jdbcType=INTEGER}");
        sql.SET("max_size = #{record.maxSize,jdbcType=INTEGER}");
        sql.SET("start_time = #{record.startTime,jdbcType=TIMESTAMP}");
        sql.SET("end_time = #{record.endTime,jdbcType=TIMESTAMP}");
        sql.SET("duration = #{record.duration,jdbcType=BIGINT}");
        sql.SET("fail_type = #{record.failType,jdbcType=VARCHAR}");
        sql.SET("exec_instance_id = #{record.execInstanceId,jdbcType=VARCHAR}");
        sql.SET("progress = #{record.progress,jdbcType=INTEGER}");
        sql.SET("org_id = #{record.orgId,jdbcType=VARCHAR}");
        sql.SET("create_by = #{record.createBy,jdbcType=VARCHAR}");
        sql.SET("create_time = #{record.createTime,jdbcType=TIMESTAMP}");
        sql.SET("update_by = #{record.updateBy,jdbcType=VARCHAR}");
        sql.SET("update_time = #{record.updateTime,jdbcType=TIMESTAMP}");
        sql.SET("permission = #{record.permission,jdbcType=INTEGER}");
        sql.SET("script = #{record.script,jdbcType=LONGVARCHAR}");
        sql.SET("error_message = #{record.errorMessage,jdbcType=LONGVARCHAR}");
        sql.SET("execute_param = #{record.executeParam,jdbcType=LONGVARCHAR}");
        
        SqlTaskExample example = (SqlTaskExample) parameter.get("example");
        applyWhere(sql, example, true);
        return sql.toString();
    }

    public String updateByExample(Map<String, Object> parameter) {
        SQL sql = new SQL();
        sql.UPDATE("sql_task");
        
        sql.SET("id = #{record.id,jdbcType=VARCHAR}");
        sql.SET("source_id = #{record.sourceId,jdbcType=VARCHAR}");
        sql.SET("script_type = #{record.scriptType,jdbcType=VARCHAR}");
        sql.SET("`status` = #{record.status,jdbcType=VARCHAR}");
        sql.SET("priority = #{record.priority,jdbcType=INTEGER}");
        sql.SET("timeout = #{record.timeout,jdbcType=INTEGER}");
        sql.SET("max_size = #{record.maxSize,jdbcType=INTEGER}");
        sql.SET("start_time = #{record.startTime,jdbcType=TIMESTAMP}");
        sql.SET("end_time = #{record.endTime,jdbcType=TIMESTAMP}");
        sql.SET("duration = #{record.duration,jdbcType=BIGINT}");
        sql.SET("fail_type = #{record.failType,jdbcType=VARCHAR}");
        sql.SET("exec_instance_id = #{record.execInstanceId,jdbcType=VARCHAR}");
        sql.SET("progress = #{record.progress,jdbcType=INTEGER}");
        sql.SET("org_id = #{record.orgId,jdbcType=VARCHAR}");
        sql.SET("create_by = #{record.createBy,jdbcType=VARCHAR}");
        sql.SET("create_time = #{record.createTime,jdbcType=TIMESTAMP}");
        sql.SET("update_by = #{record.updateBy,jdbcType=VARCHAR}");
        sql.SET("update_time = #{record.updateTime,jdbcType=TIMESTAMP}");
        sql.SET("permission = #{record.permission,jdbcType=INTEGER}");
        
        SqlTaskExample example = (SqlTaskExample) parameter.get("example");
        applyWhere(sql, example, true);
        return sql.toString();
    }

    public String updateByPrimaryKeySelective(SqlTaskWithBLOBs record) {
        SQL sql = new SQL();
        sql.UPDATE("sql_task");
        
        if (record.getSourceId() != null) {
            sql.SET("source_id = #{sourceId,jdbcType=VARCHAR}");
        }
        
        if (record.getScriptType() != null) {
            sql.SET("script_type = #{scriptType,jdbcType=VARCHAR}");
        }
        
        if (record.getStatus() != null) {
            sql.SET("`status` = #{status,jdbcType=VARCHAR}");
        }
        
        if (record.getPriority() != null) {
            sql.SET("priority = #{priority,jdbcType=INTEGER}");
        }
        
        if (record.getTimeout() != null) {
            sql.SET("timeout = #{timeout,jdbcType=INTEGER}");
        }
        
        if (record.getMaxSize() != null) {
            sql.SET("max_size = #{maxSize,jdbcType=INTEGER}");
        }
        
        if (record.getStartTime() != null) {
            sql.SET("start_time = #{startTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getEndTime() != null) {
            sql.SET("end_time = #{endTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getDuration() != null) {
            sql.SET("duration = #{duration,jdbcType=BIGINT}");
        }
        
        if (record.getFailType() != null) {
            sql.SET("fail_type = #{failType,jdbcType=VARCHAR}");
        }
        
        if (record.getExecInstanceId() != null) {
            sql.SET("exec_instance_id = #{execInstanceId,jdbcType=VARCHAR}");
        }
        
        if (record.getProgress() != null) {
            sql.SET("progress = #{progress,jdbcType=INTEGER}");
        }
        
        if (record.getOrgId() != null) {
            sql.SET("org_id = #{orgId,jdbcType=VARCHAR}");
        }
        
        if (record.getCreateBy() != null) {
            sql.SET("create_by = #{createBy,jdbcType=VARCHAR}");
        }
        
        if (record.getCreateTime() != null) {
            sql.SET("create_time = #{createTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getUpdateBy() != null) {
            sql.SET("update_by = #{updateBy,jdbcType=VARCHAR}");
        }
        
        if (record.getUpdateTime() != null) {
            sql.SET("update_time = #{updateTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getPermission() != null) {
            sql.SET("permission = #{permission,jdbcType=INTEGER}");
        }
        
        if (record.getScript() != null) {
            sql.SET("script = #{script,jdbcType=LONGVARCHAR}");
        }
        
        if (record.getErrorMessage() != null) {
            sql.SET("error_message = #{errorMessage,jdbcType=LONGVARCHAR}");
        }
        
        if (record.getExecuteParam() != null) {
            sql.SET("execute_param = #{executeParam,jdbcType=LONGVARCHAR}");
        }
        
        sql.WHERE("id = #{id,jdbcType=VARCHAR}");
        
        return sql.toString();
    }

    protected void applyWhere(SQL sql, SqlTaskExample example, boolean includeExamplePhrase) {
        if (example == null) {
            return;
        }
        
        String parmPhrase1;
        String parmPhrase1_th;
        String parmPhrase2;
        String parmPhrase2_th;
        String parmPhrase3;
        String parmPhrase3_th;
        if (includeExamplePhrase) {
            parmPhrase1 = "%s #{example.oredCriteria[%d].allCriteria[%d].value}";
            parmPhrase1_th = "%s #{example.oredCriteria[%d].allCriteria[%d].value,typeHandler=%s}";
            parmPhrase2 = "%s #{example.oredCriteria[%d].allCriteria[%d].value} and #{example.oredCriteria[%d].criteria[%d].secondValue}";
            parmPhrase2_th = "%s #{example.oredCriteria[%d].allCriteria[%d].value,typeHandler=%s} and #{example.oredCriteria[%d].criteria[%d].secondValue,typeHandler=%s}";
            parmPhrase3 = "#{example.oredCriteria[%d].allCriteria[%d].value[%d]}";
            parmPhrase3_th = "#{example.oredCriteria[%d].allCriteria[%d].value[%d],typeHandler=%s}";
        } else {
            parmPhrase1 = "%s #{oredCriteria[%d].allCriteria[%d].value}";
            parmPhrase1_th = "%s #{oredCriteria[%d].allCriteria[%d].value,typeHandler=%s}";
            parmPhrase2 = "%s #{oredCriteria[%d].allCriteria[%d].value} and #{oredCriteria[%d].criteria[%d].secondValue}";
            parmPhrase2_th = "%s #{oredCriteria[%d].allCriteria[%d].value,typeHandler=%s} and #{oredCriteria[%d].criteria[%d].secondValue,typeHandler=%s}";
            parmPhrase3 = "#{oredCriteria[%d].allCriteria[%d].value[%d]}";
            parmPhrase3_th = "#{oredCriteria[%d].allCriteria[%d].value[%d],typeHandler=%s}";
        }
        
        StringBuilder sb = new StringBuilder();
        List<Criteria> oredCriteria = example.getOredCriteria();
        boolean firstCriteria = true;
        for (int i = 0; i < oredCriteria.size(); i++) {
            Criteria criteria = oredCriteria.get(i);
            if (criteria.isValid()) {
                if (firstCriteria) {
                    firstCriteria = false;
                } else {
                    sb.append(" or ");
                }
                
                sb.append('(');
                List<Criterion> criterions = criteria.getAllCriteria();
                boolean firstCriterion = true;
                for (int j = 0; j < criterions.size(); j++) {
                    Criterion criterion = criterions.get(j);
                    if (firstCriterion) {
                        firstCriterion = false;
                    } else {
                        sb.append(" and ");
                    }
                    
                    if (criterion.isNoValue()) {
                        sb.append(criterion.getCondition());
                    } else if (criterion.isSingleValue()) {
                        if (criterion.getTypeHandler() == null) {
                            sb.append(String.format(parmPhrase1, criterion.getCondition(), i, j));
                        } else {
                            sb.append(String.format(parmPhrase1_th, criterion.getCondition(), i, j,criterion.getTypeHandler()));
                        }
                    } else if (criterion.isBetweenValue()) {
                        if (criterion.getTypeHandler() == null) {
                            sb.append(String.format(parmPhrase2, criterion.getCondition(), i, j, i, j));
                        } else {
                            sb.append(String.format(parmPhrase2_th, criterion.getCondition(), i, j, criterion.getTypeHandler(), i, j, criterion.getTypeHandler()));
                        }
                    } else if (criterion.isListValue()) {
                        sb.append(criterion.getCondition());
                        sb.append(" (");
                        List<?> listItems = (List<?>) criterion.getValue();
                        boolean comma = false;
                        for (int k = 0; k < listItems.size(); k++) {
                            if (comma) {
                                sb.append(", ");
                            } else {
                                comma = true;
                            }
                            if (criterion.getTypeHandler() == null) {
                                sb.append(String.format(parmPhrase3, i, j, k));
                            } else {
                                sb.append(String.format(parmPhrase3_th, i, j, k, criterion.getTypeHandler()));
                            }
                        }
                        sb.append(')');
                    }
                }
                sb.append(')');
            }
        }
        
        if (sb.length() > 0) {
            sql.WHERE(sb.toString());
        }
    }
}