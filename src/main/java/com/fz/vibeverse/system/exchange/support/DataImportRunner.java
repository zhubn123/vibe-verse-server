package com.fz.vibeverse.system.exchange.support;

import com.fz.vibeverse.exception.ApiException;
import com.fz.vibeverse.system.exchange.domain.entity.DataExchangeTask;
import com.fz.vibeverse.system.exchange.domain.vo.DataExchangeTaskVo;
import com.fz.vibeverse.system.oss.domain.vo.OssObjectVo;
import com.fz.vibeverse.system.oss.service.OssObjectService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 通用 CSV 导入运行器。
 */
@Service
@RequiredArgsConstructor
public class DataImportRunner {

    private final CsvExchangeCodec csvExchangeCodec;
    private final OssObjectService ossObjectService;
    private final DataExchangeTaskRecorder dataExchangeTaskRecorder;

    public byte[] buildTemplate(DataImportHandler<?> handler) {
        return csvExchangeCodec.buildTemplate(handler.headers(), handler.sampleRows());
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> DataExchangeTaskVo importCsv(DataImportHandler<T> handler, MultipartFile file, String remark) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("请选择导入文件");
        }

        OssObjectVo sourceObject = ossObjectService.uploadObject(handler.bucket(), file, handler.sourceRemark());
        DataExchangeTask task = dataExchangeTaskRecorder.startImport(
                handler.scene(),
                sourceObject.getId(),
                remark,
                handler.name() + "导入处理中"
        );

        List<DataImportRow> rows = readRows(handler, file);
        DataImportSummary summary = applyRows(handler, rows);
        Long errorObjectId = saveErrorFile(handler, task.getId(), summary.errors());

        return dataExchangeTaskRecorder.finishImport(
                task.getId(),
                summary,
                errorObjectId,
                handler.name() + "导入成功",
                "导入完成，存在失败记录"
        );
    }

    private <T> List<DataImportRow> readRows(DataImportHandler<T> handler, MultipartFile file) {
        try {
            return csvExchangeCodec.readRows(file.getInputStream(), handler.headers());
        } catch (IOException ex) {
            throw ApiException.business("读取导入文件失败：" + ex.getMessage());
        }
    }

    private <T> DataImportSummary applyRows(DataImportHandler<T> handler, List<DataImportRow> rows) {
        int successCount = 0;
        List<DataImportError> errors = new ArrayList<>();
        Set<String> uniqueKeysInFile = new HashSet<>();

        for (DataImportRow row : rows) {
            String identity = handler.rowIdentity(row);
            try {
                T data = handler.parse(row);
                identity = StringUtils.defaultString(handler.uniqueKey(data), identity);
                if (StringUtils.isNotBlank(identity) && !uniqueKeysInFile.add(identity)) {
                    throw ApiException.badRequest("导入文件内" + handler.uniqueKeyName() + "重复：" + identity);
                }
                handler.save(data, handler.mode());
                successCount++;
            } catch (RuntimeException ex) {
                errors.add(new DataImportError(row.rowNo(), identity, ex.getMessage()));
            }
        }

        return new DataImportSummary(rows.size(), successCount, errors.size(), errors);
    }

    private <T> Long saveErrorFile(DataImportHandler<T> handler, Long taskId, List<DataImportError> errors) {
        if (errors == null || errors.isEmpty()) {
            return null;
        }
        String errorCsv = csvExchangeCodec.writeRows(
                List.of("rowNo", "identity", "errorMessage"),
                errors.stream()
                        .map(error -> java.util.Arrays.asList(error.rowNo(), error.identity(), error.errorMessage()))
                        .toList()
        );
        OssObjectVo errorObject = ossObjectService.saveObject(
                handler.bucket(),
                handler.errorFilename(taskId),
                "text/csv;charset=UTF-8",
                new ByteArrayInputStream(errorCsv.getBytes(StandardCharsets.UTF_8)),
                handler.errorRemark()
        );
        return errorObject.getId();
    }
}
