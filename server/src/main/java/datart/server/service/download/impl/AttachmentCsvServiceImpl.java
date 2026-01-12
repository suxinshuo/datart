package datart.server.service.download.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import datart.core.base.consts.AttachmentType;
import datart.core.data.provider.Column;
import datart.core.data.provider.Dataframe;
import datart.core.entity.SqlTaskResult;
import datart.core.utils.JsonUtils;
import datart.server.base.bo.download.ExportFile;
import datart.server.service.download.AttachmentService;
import datart.server.service.task.SqlTaskResultService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * @author suxinshuo
 * @date 2026/1/7 10:45
 */
@Slf4j
@Service("csvAttachmentService")
public class AttachmentCsvServiceImpl implements AttachmentService {

    @Getter
    protected final AttachmentType attachmentType = AttachmentType.CSV;

    @Resource
    private SqlTaskResultService sqlTaskResultService;

    @Override
    public ExportFile getFile(String taskId) {
        List<SqlTaskResult> sqlTaskResults = sqlTaskResultService.getByTaskId(taskId);
        if (CollUtil.isEmpty(sqlTaskResults)) {
            return ExportFile.empty(taskId, getAttachmentType());
        }

        Dataframe dataframe = JsonUtils.toBean(sqlTaskResults.get(0).getData(), Dataframe.class);
        List<Column> columns = dataframe.getColumns();
        String[] headers = columns.stream().map(col -> {
            String[] names = col.getName();
            if (Objects.isNull(names) || names.length == 0) {
                return "";
            }
            return names[0];
        }).toArray(String[]::new);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        CsvWriter writer = CsvUtil.getWriter(osw);

        try {
            // 写 BOM, 防止 Excel 中文乱码
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            writer.write(headers);

            List<List<Object>> rows = dataframe.getRows();
            for (List<Object> lineRow : rows) {
                String[] row = lineRow.stream().map(this::wrapperColVal).toArray(String[]::new);
                writer.write(row);
            }

            writer.flush();
        } finally {
            IoUtil.close(writer);
        }

        return ExportFile.builder()
                .fileName(generateFileName(taskId, getAttachmentType()))
                .attachmentType(getAttachmentType())
                .content(out.toByteArray())
                .build();
    }

}
