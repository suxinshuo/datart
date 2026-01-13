package datart.server.service.task.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.*;
import com.google.common.collect.Lists;
import datart.core.common.DateUtils;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
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

    @Value("${hadoop.task-result-dir}")
    private String taskResultDir;

    @Resource
    private SqlTaskResultMapper sqlTaskResultMapper;

    @Resource
    private FileSystem hdfsFileSystem;

    /**
     * 根据任务 ID 获取 SQL 任务结果
     *
     * @param taskId 任务 ID
     * @return SQL 任务结果
     */
    @Override
    public List<SqlTaskResultBo> getByTaskId(String taskId) {
        if (StringUtils.isBlank(taskId)) {
            return Lists.newArrayList();
        }

        SqlTaskResultExample example = new SqlTaskResultExample();
        example.createCriteria().andTaskIdEqualTo(taskId);
        List<SqlTaskResult> sqlTaskResults = sqlTaskResultMapper.selectByExample(example);

        if (CollUtil.isEmpty(sqlTaskResults)) {
            return Lists.newArrayList();
        }

        // 从 hdfs 读取具体数据填充
        return sqlTaskResults.stream()
                .filter(sqlTaskResult -> StringUtils.isNotBlank(sqlTaskResult.getData()))
                .map(sqlTaskResult -> {
                    String data = sqlTaskResult.getData();
                    Dataframe dataframe = readDataframe(hdfsFileSystem, data);
                    SqlTaskResultBo sqlTaskResultBo = new SqlTaskResultBo();
                    BeanUtil.copyProperties(sqlTaskResult, sqlTaskResultBo);
                    sqlTaskResultBo.setDataframe(dataframe);
                    return sqlTaskResultBo;
                }).collect(Collectors.toList());
    }

    /**
     * 获取N天前的 SQL 任务结果
     *
     * @param days 天数
     * @return N天前的 SQL 任务结果
     */
    @Override
    public List<SqlTaskResult> getDaysBeforeResults(Integer days) {
        SqlTaskResultExample example = new SqlTaskResultExample();
        example.createCriteria().andCreateTimeLessThan(DateUtils.getDaysAgo(days));
        return sqlTaskResultMapper.selectByExample(example);
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

        // 把数据存储到 hdfs
        Dataframe dataframe = ((SqlTaskResultCreateParam) createParam).getDataframe();
        String hdfsPath = getHdfsPath(sqlTaskResult.getTaskId());
        writeDataframe(hdfsFileSystem, hdfsPath, dataframe);
        log.info("数据写入 HDFS 成功. hdfsPath: {}", hdfsPath);

        // 把数据存储到数据库
        sqlTaskResult.setData(hdfsPath);
        sqlTaskResultMapper.insertSelective(sqlTaskResult);
        return sqlTaskResult;
    }

    @Override
    public void requirePermission(SqlTaskResult entity, int permission) {

    }

    private String getHdfsPath(String taskId) {
        return taskResultDir + taskId + ".json";
    }

    /**
     * 将 Dataframe 流式写为 JSON 到 HDFS
     *
     * @param fs        HDFS 文件系统
     * @param hdfsPath  HDFS 路径
     * @param dataframe 数据
     */
    private void writeDataframe(FileSystem fs, String hdfsPath, Dataframe dataframe) {
        Path path = new Path(hdfsPath);
        try (FSDataOutputStream out = fs.create(path, true);
             BufferedOutputStream bos = new BufferedOutputStream(out, BUFFER_SIZE);
             JsonGenerator gen = MAPPER.getFactory().createGenerator(bos, com.fasterxml.jackson.core.JsonEncoding.UTF8)) {

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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从 HDFS 读取 JSON 为 Dataframe
     *
     * @param fs       HDFS 文件系统
     * @param hdfsPath HDFS 路径
     * @return 数据
     */
    private Dataframe readDataframe(FileSystem fs, String hdfsPath) {
        Path path = new Path(hdfsPath);
        try (FSDataInputStream in = fs.open(path);
             BufferedInputStream bis = new BufferedInputStream(in, BUFFER_SIZE)) {
            return MAPPER.readValue(bis, Dataframe.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

}
