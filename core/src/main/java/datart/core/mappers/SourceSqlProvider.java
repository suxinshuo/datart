package datart.core.mappers;

import datart.core.entity.Source;
import datart.core.entity.SourceExample.Criteria;
import datart.core.entity.SourceExample.Criterion;
import datart.core.entity.SourceExample;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.jdbc.SQL;

public class SourceSqlProvider {
    public String countByExample(SourceExample example) {
        SQL sql = new SQL();
        sql.SELECT("count(*)").FROM("source");
        applyWhere(sql, example, false);
        return sql.toString();
    }

    public String deleteByExample(SourceExample example) {
        SQL sql = new SQL();
        sql.DELETE_FROM("source");
        applyWhere(sql, example, false);
        return sql.toString();
    }

    public String insertSelective(Source record) {
        SQL sql = new SQL();
        sql.INSERT_INTO("source");
        
        if (record.getId() != null) {
            sql.VALUES("id", "#{id,jdbcType=VARCHAR}");
        }
        
        if (record.getName() != null) {
            sql.VALUES("`name`", "#{name,jdbcType=VARCHAR}");
        }
        
        if (record.getConfig() != null) {
            sql.VALUES("config", "#{config,jdbcType=VARCHAR}");
        }
        
        if (record.getType() != null) {
            sql.VALUES("`type`", "#{type,jdbcType=VARCHAR}");
        }
        
        if (record.getOrgId() != null) {
            sql.VALUES("org_id", "#{orgId,jdbcType=VARCHAR}");
        }
        
        if (record.getParentId() != null) {
            sql.VALUES("parent_id", "#{parentId,jdbcType=VARCHAR}");
        }
        
        if (record.getIsFolder() != null) {
            sql.VALUES("is_folder", "#{isFolder,jdbcType=TINYINT}");
        }
        
        if (record.getIndex() != null) {
            sql.VALUES("`index`", "#{index,jdbcType=DOUBLE}");
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
        
        if (record.getStatus() != null) {
            sql.VALUES("`status`", "#{status,jdbcType=TINYINT}");
        }
        
        return sql.toString();
    }

    public String selectByExample(SourceExample example) {
        SQL sql = new SQL();
        if (example != null && example.isDistinct()) {
            sql.SELECT_DISTINCT("id");
        } else {
            sql.SELECT("id");
        }
        sql.SELECT("`name`");
        sql.SELECT("config");
        sql.SELECT("`type`");
        sql.SELECT("org_id");
        sql.SELECT("parent_id");
        sql.SELECT("is_folder");
        sql.SELECT("`index`");
        sql.SELECT("create_by");
        sql.SELECT("create_time");
        sql.SELECT("update_by");
        sql.SELECT("update_time");
        sql.SELECT("`status`");
        sql.FROM("source");
        applyWhere(sql, example, false);
        
        if (example != null && example.getOrderByClause() != null) {
            sql.ORDER_BY(example.getOrderByClause());
        }
        
        return sql.toString();
    }

    public String updateByExampleSelective(Map<String, Object> parameter) {
        Source record = (Source) parameter.get("record");
        SourceExample example = (SourceExample) parameter.get("example");
        
        SQL sql = new SQL();
        sql.UPDATE("source");
        
        if (record.getId() != null) {
            sql.SET("id = #{record.id,jdbcType=VARCHAR}");
        }
        
        if (record.getName() != null) {
            sql.SET("`name` = #{record.name,jdbcType=VARCHAR}");
        }
        
        if (record.getConfig() != null) {
            sql.SET("config = #{record.config,jdbcType=VARCHAR}");
        }
        
        if (record.getType() != null) {
            sql.SET("`type` = #{record.type,jdbcType=VARCHAR}");
        }
        
        if (record.getOrgId() != null) {
            sql.SET("org_id = #{record.orgId,jdbcType=VARCHAR}");
        }
        
        if (record.getParentId() != null) {
            sql.SET("parent_id = #{record.parentId,jdbcType=VARCHAR}");
        }
        
        if (record.getIsFolder() != null) {
            sql.SET("is_folder = #{record.isFolder,jdbcType=TINYINT}");
        }
        
        if (record.getIndex() != null) {
            sql.SET("`index` = #{record.index,jdbcType=DOUBLE}");
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
        
        if (record.getStatus() != null) {
            sql.SET("`status` = #{record.status,jdbcType=TINYINT}");
        }
        
        applyWhere(sql, example, true);
        return sql.toString();
    }

    public String updateByExample(Map<String, Object> parameter) {
        SQL sql = new SQL();
        sql.UPDATE("source");
        
        sql.SET("id = #{record.id,jdbcType=VARCHAR}");
        sql.SET("`name` = #{record.name,jdbcType=VARCHAR}");
        sql.SET("config = #{record.config,jdbcType=VARCHAR}");
        sql.SET("`type` = #{record.type,jdbcType=VARCHAR}");
        sql.SET("org_id = #{record.orgId,jdbcType=VARCHAR}");
        sql.SET("parent_id = #{record.parentId,jdbcType=VARCHAR}");
        sql.SET("is_folder = #{record.isFolder,jdbcType=TINYINT}");
        sql.SET("`index` = #{record.index,jdbcType=DOUBLE}");
        sql.SET("create_by = #{record.createBy,jdbcType=VARCHAR}");
        sql.SET("create_time = #{record.createTime,jdbcType=TIMESTAMP}");
        sql.SET("update_by = #{record.updateBy,jdbcType=VARCHAR}");
        sql.SET("update_time = #{record.updateTime,jdbcType=TIMESTAMP}");
        sql.SET("`status` = #{record.status,jdbcType=TINYINT}");
        
        SourceExample example = (SourceExample) parameter.get("example");
        applyWhere(sql, example, true);
        return sql.toString();
    }

    public String updateByPrimaryKeySelective(Source record) {
        SQL sql = new SQL();
        sql.UPDATE("source");
        
        if (record.getName() != null) {
            sql.SET("`name` = #{name,jdbcType=VARCHAR}");
        }
        
        if (record.getConfig() != null) {
            sql.SET("config = #{config,jdbcType=VARCHAR}");
        }
        
        if (record.getType() != null) {
            sql.SET("`type` = #{type,jdbcType=VARCHAR}");
        }
        
        if (record.getOrgId() != null) {
            sql.SET("org_id = #{orgId,jdbcType=VARCHAR}");
        }
        
        if (record.getParentId() != null) {
            sql.SET("parent_id = #{parentId,jdbcType=VARCHAR}");
        }
        
        if (record.getIsFolder() != null) {
            sql.SET("is_folder = #{isFolder,jdbcType=TINYINT}");
        }
        
        if (record.getIndex() != null) {
            sql.SET("`index` = #{index,jdbcType=DOUBLE}");
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
        
        if (record.getStatus() != null) {
            sql.SET("`status` = #{status,jdbcType=TINYINT}");
        }
        
        sql.WHERE("id = #{id,jdbcType=VARCHAR}");
        
        return sql.toString();
    }

    protected void applyWhere(SQL sql, SourceExample example, boolean includeExamplePhrase) {
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