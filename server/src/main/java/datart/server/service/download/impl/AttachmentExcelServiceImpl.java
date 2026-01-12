package datart.server.service.download.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import datart.core.base.PageInfo;
import datart.core.base.consts.AttachmentType;
import datart.core.common.Application;
import datart.core.common.POIUtils;
import datart.core.data.provider.Column;
import datart.core.data.provider.Dataframe;
import datart.core.entity.SqlTaskResult;
import datart.core.entity.View;
import datart.core.entity.poi.POISettings;
import datart.core.utils.JsonUtils;
import datart.server.base.bo.download.ExportFile;
import datart.server.base.params.DownloadCreateParam;
import datart.server.base.params.ViewExecuteParam;
import datart.server.common.PoiConvertUtils;
import datart.server.service.*;
import datart.server.service.download.AttachmentService;
import datart.server.service.task.SqlTaskResultService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service("excelAttachmentService")
public class AttachmentExcelServiceImpl implements AttachmentService {

    @Getter
    protected final AttachmentType attachmentType = AttachmentType.EXCEL;

    @Resource
    private VizService vizService;

    @Resource
    private SqlTaskResultService sqlTaskResultService;

    @Override
    public File getFile(DownloadCreateParam downloadParams, String path, String fileName) throws Exception {
        DataProviderService dataProviderService = Application.getBean(DataProviderService.class);
        OrgSettingService orgSettingService = Application.getBean(OrgSettingService.class);
        ViewService viewService = Application.getBean(ViewService.class);

        Workbook workbook = POIUtils.createEmpty();
        for (int i = 0; i < downloadParams.getDownloadParams().size(); i++) {
            ViewExecuteParam viewExecuteParam = downloadParams.getDownloadParams().get(i);
            View view = viewService.retrieve(viewExecuteParam.getViewId(), false);
            viewExecuteParam.setPageInfo(PageInfo.builder().pageNo(1).pageSize(orgSettingService.getDownloadRecordLimit(view.getOrgId())).build());
            Dataframe dataframe = dataProviderService.execute(downloadParams.getDownloadParams().get(i));
            String chartConfigStr = vizService.getChartConfigByVizId(viewExecuteParam.getVizType(), viewExecuteParam.getVizId());
            POISettings poiSettings = PoiConvertUtils.covertToPoiSetting(chartConfigStr, dataframe);
            String sheetName = StringUtils.isNotBlank(viewExecuteParam.getVizName()) ? viewExecuteParam.getVizName() : "Sheet" + i;
            POIUtils.withSheet(workbook, sheetName, dataframe, poiSettings);
        }
        path = generateFileName(path, fileName, attachmentType);
        File file = new File(path);
        POIUtils.save(workbook, file.getPath(), true);
        log.info("create excel file complete.");
        return file;
    }

    @Override
    public ExportFile getFile(String taskId) {
        List<SqlTaskResult> sqlTaskResults = sqlTaskResultService.getByTaskId(taskId);
        if (CollUtil.isEmpty(sqlTaskResults)) {
            return ExportFile.empty(taskId, getAttachmentType());
        }

        Dataframe dataframe = JsonUtils.toBean(sqlTaskResults.get(0).getData(), Dataframe.class);
        List<Column> columns = dataframe.getColumns();
        List<String> headers = columns.stream().map(col -> {
            String[] names = col.getName();
            if (Objects.isNull(names) || names.length == 0) {
                return "";
            }
            return names[0];
        }).collect(Collectors.toList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ExcelWriter writer = ExcelUtil.getWriter(true);

        try {
            writer.writeHeadRow(headers);

            List<List<Object>> rows = dataframe.getRows();
            for (List<Object> lineRow : rows) {
                List<String> row = lineRow.stream().map(this::wrapperColVal).collect(Collectors.toList());
                writer.writeRow(row);
            }

            writer.flush(out, true);
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
