package datart.server.service.task.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.*;
import com.google.common.collect.Lists;
import datart.core.base.PageInfo;
import datart.core.base.consts.Const;
import datart.core.data.provider.Column;
import datart.core.data.provider.Dataframe;
import datart.core.entity.SqlTaskResult;
import datart.core.entity.SqlTaskResultExample;
import datart.core.mappers.SqlTaskResultMapper;
import datart.server.base.bo.task.SqlTaskResultBo;
import datart.server.base.params.BaseCreateParam;
import datart.server.base.params.task.SqlTaskResultCreateParam;
import datart.server.service.BaseService;
import datart.server.service.task.SqlTaskResultService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author suxinshuo
 * @date 2025/12/19 12:19
 */
@Slf4j
@Service
public class SqlTaskResultServiceImpl extends BaseService implements SqlTaskResultService {

    /**
     * 单例 ObjectMapper（线程安全）
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // 统一的时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        JavaTimeModule timeModule = new JavaTimeModule();

        // java.time.*
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        timeModule.addSerializer(
                ZonedDateTime.class,
                com.fasterxml.jackson.databind.ser.std.ToStringSerializer.instance
        );
        timeModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(formatter));

        MAPPER.registerModule(timeModule);

        // 禁止时间戳（long）
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // java.util.Date / java.sql.Timestamp
        MAPPER.setDateFormat(
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        );

        // 统一时区
        MAPPER.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    /**
     * HDFS 输出缓冲区大小
     */
    private static final int BUFFER_SIZE = 128 * 1024;

    private static final int BATCH_SIZE = 1000;

    @Resource
    private SqlTaskResultMapper sqlTaskResultMapper;

    @Resource
    private FileSystem hdfsFileSystem;

    /**
     * 根据任务 ID 获取 SQL 任务结果
     *
     * @param taskId   任务 ID
     * @param truncate 是否截断结果
     * @return SQL 任务结果
     */
    @Override
    public List<SqlTaskResultBo> getByTaskId(String taskId, Boolean truncate) {
        log.info("getByTaskId taskId: {}, truncate: {}", taskId, truncate);
        if (StringUtils.isBlank(taskId)) {
            return Lists.newArrayList();
        }

        SqlTaskResultExample example = new SqlTaskResultExample();
        example.createCriteria().andTaskIdEqualTo(taskId);
        List<SqlTaskResult> sqlTaskResults = sqlTaskResultMapper.selectByExample(example);

        if (CollUtil.isEmpty(sqlTaskResults)) {
            return Lists.newArrayList();
        }

        return sqlTaskResults.stream()
                .filter(sqlTaskResult -> StringUtils.isNotBlank(sqlTaskResult.getData()))
                .map(sqlTaskResult -> {
                    String hdfsPath = sqlTaskResult.getData();
                    Integer maxCells = null;
                    if (BooleanUtils.isTrue(truncate)) {
                        maxCells = Const.SQL_RESULT_SHOW_MAX_CELLS;
                    }
                    Dataframe dataframe = readDataframeStream(hdfsPath, maxCells);
                    SqlTaskResultBo sqlTaskResultBo = new SqlTaskResultBo();
                    BeanUtil.copyProperties(sqlTaskResult, sqlTaskResultBo);
                    sqlTaskResultBo.setDataframe(dataframe);
                    return sqlTaskResultBo;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String id) {
        if (StringUtils.isBlank(id)) {
            return false;
        }
        // 删除 hdfs 文件
        SqlTaskResult sqlTaskResult = sqlTaskResultMapper.selectByPrimaryKey(id);
        if (Objects.isNull(sqlTaskResult)) {
            return false;
        }
        String data = sqlTaskResult.getData();

        sqlTaskResultMapper.deleteByPrimaryKey(id);
        delHfs(hdfsFileSystem, data);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SqlTaskResult createSelective(BaseCreateParam createParam) {
        SqlTaskResult sqlTaskResult = convertParam(createParam);

        Dataframe dataframe = ((SqlTaskResultCreateParam) createParam).getDataframe();
        String hdfsPath = dataframe.getHdfsPath();

        sqlTaskResult.setData(hdfsPath);
        sqlTaskResultMapper.insertSelective(sqlTaskResult);
        return sqlTaskResult;
    }

    @Override
    public void requirePermission(SqlTaskResult entity, int permission) {

    }

    private void delHfs(FileSystem fs, String hdfsPath) {
        try {
            Path path = new Path(hdfsPath);
            if (fs.exists(path)) {
                fs.delete(path, true);
            }
        } catch (IOException e) {
            log.error("删除 HDFS 文件失败. hdfsPath: {}", hdfsPath, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 流式保存结果到 HDFS
     *
     * @param taskId   任务 ID
     * @param rs       ResultSet
     * @param columns  列定义
     * @param hdfsPath HDFS 保存路径
     * @return 保存的记录行数
     */
    @Override
    public int writeDataframeStream(String taskId, ResultSet rs, List<Column> columns, String hdfsPath) {
        int rowCount = 0;
        Path path = new Path(hdfsPath);
        try (FSDataOutputStream out = hdfsFileSystem.create(path, true);
             BufferedOutputStream bos = new BufferedOutputStream(out, BUFFER_SIZE);
             JsonGenerator gen = MAPPER.getFactory().createGenerator(bos, JsonEncoding.UTF8)) {

            gen.writeStartObject();

            gen.writeStringField("id", "DF" + taskId);

            gen.writeFieldName("columns");
            gen.writeObject(columns);

            gen.writeFieldName("rows");
            gen.writeStartArray();

            while (rs.next()) {
                gen.writeStartArray();
                for (int i = 1; i <= columns.size(); i++) {
                    Object obj = getObjFromResultSet(rs, i);
                    gen.writeObject(obj);
                }
                gen.writeEndArray();

                rowCount++;
                if (rowCount % BATCH_SIZE == 0) {
                    gen.flush();
                    out.hflush();
                }
            }

            gen.writeEndArray();

            gen.writeEndObject();

            gen.flush();
            out.hflush();
            out.hsync();

            log.info("流式写入 HDFS 完成. hdfsPath: {}, rowCount: {}", hdfsPath, rowCount);
        } catch (IOException | SQLException e) {
            log.error("流式写入 HDFS 失败. hdfsPath: {}", hdfsPath, e);
            throw new RuntimeException(e);
        }
        return rowCount;
    }

    /**
     * 将 Dataframe 流式写为 JSON 到 HDFS
     *
     * @param hdfsPath  HDFS 路径
     * @param dataframe 数据
     */
    @Override
    public void writeDataframe(String hdfsPath, Dataframe dataframe) {
        Path path = new Path(hdfsPath);
        try (FSDataOutputStream out = hdfsFileSystem.create(path, true);
             BufferedOutputStream bos = new BufferedOutputStream(out, BUFFER_SIZE);
             JsonGenerator gen = MAPPER.getFactory().createGenerator(bos, JsonEncoding.UTF8)) {

            gen.writeStartObject();

            // id (final)
            gen.writeStringField("id", dataframe.getId());

            // name
            if (Objects.nonNull(dataframe.getName())) {
                gen.writeStringField("name", dataframe.getName());
            }

            // vizType
            if (Objects.nonNull(dataframe.getVizType())) {
                gen.writeStringField("vizType", dataframe.getVizType());
            }

            // vizId
            if (Objects.nonNull(dataframe.getVizId())) {
                gen.writeStringField("vizId", dataframe.getVizId());
            }

            // columns
            if (Objects.nonNull(dataframe.getColumns())) {
                gen.writeFieldName("columns");
                gen.writeObject(dataframe.getColumns());
            }

            // rows（重点：大数据）
            if (Objects.nonNull(dataframe.getRows())) {
                gen.writeFieldName("rows");
                gen.writeStartArray();
                for (List<Object> row : dataframe.getRows()) {
                    gen.writeObject(row);
                }
                gen.writeEndArray();
            }

            // pageInfo
            if (Objects.nonNull(dataframe.getPageInfo())) {
                gen.writeFieldName("pageInfo");
                gen.writeObject(dataframe.getPageInfo());
            }

            // script
            if (Objects.nonNull(dataframe.getScript())) {
                gen.writeStringField("script", dataframe.getScript());
            }

            gen.writeEndObject();

            gen.flush();
            // flush 到 DataNode
            out.hflush();
            // 可选：强一致（性能略低）
            out.hsync();

            log.info("写入 HDFS 完成. hdfsPath: {}, rowCount: {}", hdfsPath, dataframe.getRowCount());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Dataframe readDataframeStream(String hdfsPath, Integer maxCells) {
        Path path = new Path(hdfsPath);
        try (FSDataInputStream in = hdfsFileSystem.open(path);
             BufferedInputStream bis = new BufferedInputStream(in, BUFFER_SIZE);
             JsonParser parser = new JsonFactory().createParser(bis)) {

            parser.setCodec(MAPPER);

            boolean truncated = false;
            Dataframe dataframe = new Dataframe();
            List<List<Object>> rows = new ArrayList<>();
            int totalCells = 0;

            // 期待 START_OBJECT
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalStateException("JSON root is not an object");
            }

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                // 只在 FIELD_NAME 上处理
                if (parser.currentToken() != JsonToken.FIELD_NAME) {
                    continue;
                }

                String fieldName = parser.getCurrentName();
                // 移动到 value token
                parser.nextToken();

                switch (fieldName) {
                    case "id":
                        String id = parser.getText();
                        try {
                            java.lang.reflect.Field field = Dataframe.class.getDeclaredField("id");
                            field.setAccessible(true);
                            field.set(dataframe, id);
                        } catch (Exception e) {
                            log.warn("设置 Dataframe id 失败", e);
                        }
                        break;

                    case "name":
                        dataframe.setName(parser.getValueAsString());
                        break;

                    case "vizType":
                        dataframe.setVizType(parser.getValueAsString());
                        break;

                    case "vizId":
                        dataframe.setVizId(parser.getValueAsString());
                        break;

                    case "columns": {
                        List<Column> columns = new ArrayList<>();
                        if (parser.currentToken() == JsonToken.START_ARRAY) {
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                Column column = MAPPER.readValue(parser, Column.class);
                                columns.add(column);
                            }
                        } else {
                            parser.skipChildren();
                        }
                        dataframe.setColumns(columns);
                        break;
                    }

                    case "rows": {
                        if (parser.currentToken() == JsonToken.START_ARRAY) {
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                if (Objects.nonNull(maxCells) && totalCells >= maxCells) {
                                    truncated = true;
                                    parser.skipChildren();
                                    break;
                                }
                                if (parser.currentToken() == JsonToken.START_ARRAY) {
                                    List<Object> row = new ArrayList<>();
                                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                                        Object value = parser.readValueAs(Object.class);
                                        row.add(value);
                                    }
                                    rows.add(row);
                                    totalCells += row.size();
                                } else {
                                    parser.skipChildren();
                                }
                            }
                        } else {
                            parser.skipChildren();
                        }

                        dataframe.setRows(rows);
                        break;
                    }

                    case "pageInfo":
                        PageInfo pageInfo = MAPPER.readValue(parser, PageInfo.class);
                        dataframe.setPageInfo(pageInfo);
                        break;

                    case "script":
                        dataframe.setScript(parser.getValueAsString());
                        break;

                    default:
                        parser.skipChildren();
                }
            }
            dataframe.setTruncated(truncated);
            log.info("流式读取 HDFS 成功. hdfsPath: {}, cells: {}", hdfsPath, totalCells);
            return dataframe;
        } catch (IOException e) {
            log.error("流式读取 HDFS 失败. hdfsPath: {}", hdfsPath, e);
            throw new RuntimeException(e);
        }
    }


    private Object getObjFromResultSet(ResultSet rs, int columnIndex) throws SQLException {
        Object obj = rs.getObject(columnIndex);
        if (obj instanceof Boolean) {
            obj = rs.getObject(columnIndex).toString();
        } else if (obj instanceof LocalDateTime) {
            obj = rs.getTimestamp(columnIndex);
        }
        return obj;
    }

}
