package datart.server.service.download;

import datart.core.base.consts.AttachmentType;
import datart.core.base.consts.Const;
import datart.core.base.exception.Exceptions;
import datart.core.base.exception.ParamException;
import datart.core.common.Application;
import datart.core.common.FileUtils;
import datart.server.base.bo.download.ExportFile;
import datart.server.base.params.DownloadCreateParam;
import datart.server.service.download.impl.AttachmentCsvServiceImpl;
import datart.server.service.download.impl.AttachmentExcelServiceImpl;
import datart.server.service.download.impl.AttachmentImageServiceImpl;
import datart.server.service.download.impl.AttachmentPdfServiceImpl;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.util.Calendar;
import java.util.Objects;

public interface AttachmentService {

    String SHARE_USER = "SCHEDULER_";

    default File getFile(DownloadCreateParam downloadCreateParam, String path, String fileName) throws Exception {
        Exceptions.tr(ParamException.class, "message.unsupported.format", getAttachmentType().name());
        return null;
    }

    default ExportFile getFile(String taskId) {
        Exceptions.tr(ParamException.class, "message.unsupported.format", getAttachmentType().name());
        return null;
    }

    AttachmentType getAttachmentType();

    default String generateFileName(String path, String fileName, AttachmentType attachmentType) {
        path = FileUtils.withBasePath(path);
        String timeStr = DateFormatUtils.format(Calendar.getInstance(), Const.FILE_SUFFIX_DATE_FORMAT);
        String randomStr = RandomStringUtils.randomNumeric(3);
        fileName = fileName + "_" + timeStr + "_" + randomStr + attachmentType.getSuffix();
        return FileUtils.concatPath(path, fileName);
    }

    default String generateFileName(String fileName, AttachmentType attachmentType) {
        String timeStr = DateFormatUtils.format(Calendar.getInstance(), Const.FILE_SUFFIX_DATE_FORMAT);
        String randomStr = RandomStringUtils.randomNumeric(3);
        return fileName + "_" + timeStr + "_" + randomStr + attachmentType.getSuffix();
    }

    default String wrapperColVal(Object colVal) {
        if (Objects.isNull(colVal)) {
            return "NULL";
        }
        return colVal.toString();
    }

    static AttachmentService matchAttachmentService(AttachmentType type) {
        switch (type) {
            case EXCEL:
                return Application.getBean(AttachmentExcelServiceImpl.class);
            case IMAGE:
                return Application.getBean(AttachmentImageServiceImpl.class);
            case PDF:
                return Application.getBean(AttachmentPdfServiceImpl.class);
            case CSV:
                return Application.getBean(AttachmentCsvServiceImpl.class);
            default:
                Exceptions.msg("unsupported download type." + type);
                return null;
        }
    }

}
