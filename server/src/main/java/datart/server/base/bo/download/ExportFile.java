package datart.server.base.bo.download;

import datart.core.base.consts.AttachmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author suxinshuo
 * @date 2026/1/7 10:16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportFile {

    private String fileName;

    private AttachmentType attachmentType;

    private byte[] content;

    public static ExportFile empty(String fileName, AttachmentType attachmentType) {
        return ExportFile.builder()
                .fileName(fileName)
                .attachmentType(attachmentType)
                .content(new byte[0])
                .build();
    }

}
