package datart.core.mappers;

import datart.core.entity.DorisUserMapping;
import datart.core.entity.DorisUserMappingExample.Criteria;
import datart.core.entity.DorisUserMappingExample.Criterion;
import datart.core.entity.DorisUserMappingExample;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.jdbc.SQL;

public class DorisUserMappingSqlProvider {
    public String countByExample(DorisUserMappingExample example) {
        SQL sql = new SQL();
        sql.SELECT("count(*)").FROM("doris_user_mapping");
        applyWhere(sql, example, false);
        return sql.toString();
    }

    public String deleteByExample(DorisUserMappingExample example) {
        SQL sql = new SQL();
        sql.DELETE_FROM("doris_user_mapping");
        applyWhere(sql, example, false);
        return sql.toString();
    }

    public String insertSelective(DorisUserMapping record) {
        SQL sql = new SQL();
        sql.INSERT_INTO("doris_user_mapping");
        
        if (record.getId() != null) {
            sql.VALUES("id", "#{id,jdbcType=VARCHAR}");
        }
        
        if (record.getSysUsername() != null) {
            sql.VALUES("sys_username", "#{sysUsername,jdbcType=VARCHAR}");
        }
        
        if (record.getSourceId() != null) {
            sql.VALUES("source_id", "#{sourceId,jdbcType=VARCHAR}");
        }
        
        if (record.getDorisUsername() != null) {
            sql.VALUES("doris_username", "#{dorisUsername,jdbcType=VARCHAR}");
        }
        
        if (record.getEncryptedPassword() != null) {
            sql.VALUES("encrypted_password", "#{encryptedPassword,jdbcType=VARCHAR}");
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
        
        return sql.toString();
    }

    public String selectByExample(DorisUserMappingExample example) {
        SQL sql = new SQL();
        if (example != null && example.isDistinct()) {
            sql.SELECT_DISTINCT("id");
        } else {
            sql.SELECT("id");
        }
        sql.SELECT("sys_username");
        sql.SELECT("source_id");
        sql.SELECT("doris_username");
        sql.SELECT("encrypted_password");
        sql.SELECT("create_by");
        sql.SELECT("create_time");
        sql.SELECT("update_by");
        sql.SELECT("update_time");
        sql.FROM("doris_user_mapping");
        applyWhere(sql, example, false);
        
        if (example != null && example.getOrderByClause() != null) {
            sql.ORDER_BY(example.getOrderByClause());
        }
        
        return sql.toString();
    }

    public String updateByExampleSelective(Map<String, Object> parameter) {
        DorisUserMapping record = (DorisUserMapping) parameter.get("record");
        DorisUserMappingExample example = (DorisUserMappingExample) parameter.get("example");
        
        SQL sql = new SQL();
        sql.UPDATE("doris_user_mapping");
        
        if (record.getId() != null) {
            sql.SET("id = #{record.id,jdbcType=VARCHAR}");
        }
        
        if (record.getSysUsername() != null) {
            sql.SET("sys_username = #{record.sysUsername,jdbcType=VARCHAR}");
        }
        
        if (record.getSourceId() != null) {
            sql.SET("source_id = #{record.sourceId,jdbcType=VARCHAR}");
        }
        
        if (record.getDorisUsername() != null) {
            sql.SET("doris_username = #{record.dorisUsername,jdbcType=VARCHAR}");
        }
        
        if (record.getEncryptedPassword() != null) {
            sql.SET("encrypted_password = #{record.encryptedPassword,jdbcType=VARCHAR}");
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
        
        applyWhere(sql, example, true);
        return sql.toString();
    }

    public String updateByExample(Map<String, Object> parameter) {
        SQL sql = new SQL();
        sql.UPDATE("doris_user_mapping");
        
        sql.SET("id = #{record.id,jdbcType=VARCHAR}");
        sql.SET("sys_username = #{record.sysUsername,jdbcType=VARCHAR}");
        sql.SET("source_id = #{record.sourceId,jdbcType=VARCHAR}");
        sql.SET("doris_username = #{record.dorisUsername,jdbcType=VARCHAR}");
        sql.SET("encrypted_password = #{record.encryptedPassword,jdbcType=VARCHAR}");
        sql.SET("create_by = #{record.createBy,jdbcType=VARCHAR}");
        sql.SET("create_time = #{record.createTime,jdbcType=TIMESTAMP}");
        sql.SET("update_by = #{record.updateBy,jdbcType=VARCHAR}");
        sql.SET("update_time = #{record.updateTime,jdbcType=TIMESTAMP}");
        
        DorisUserMappingExample example = (DorisUserMappingExample) parameter.get("example");
        applyWhere(sql, example, true);
        return sql.toString();
    }

    public String updateByPrimaryKeySelective(DorisUserMapping record) {
        SQL sql = new SQL();
        sql.UPDATE("doris_user_mapping");
        
        if (record.getSysUsername() != null) {
            sql.SET("sys_username = #{sysUsername,jdbcType=VARCHAR}");
        }
        
        if (record.getSourceId() != null) {
            sql.SET("source_id = #{sourceId,jdbcType=VARCHAR}");
        }
        
        if (record.getDorisUsername() != null) {
            sql.SET("doris_username = #{dorisUsername,jdbcType=VARCHAR}");
        }
        
        if (record.getEncryptedPassword() != null) {
            sql.SET("encrypted_password = #{encryptedPassword,jdbcType=VARCHAR}");
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
        
        sql.WHERE("id = #{id,jdbcType=VARCHAR}");
        
        return sql.toString();
    }

    protected void applyWhere(SQL sql, DorisUserMappingExample example, boolean includeExamplePhrase) {
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