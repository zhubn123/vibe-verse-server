package com.fz.vibeverse.system.exchange.service;

import com.fz.vibeverse.exception.ApiException;
import com.fz.vibeverse.system.exchange.domain.vo.DataExchangeTaskVo;
import com.fz.vibeverse.system.exchange.support.*;
import com.fz.vibeverse.system.oss.domain.vo.OssObjectContent;
import com.fz.vibeverse.system.oss.domain.vo.OssObjectVo;
import com.fz.vibeverse.system.oss.service.OssObjectService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 导入导出引擎，按 scene 路由到对应 Handler。
 */
@Service
@RequiredArgsConstructor
public class DataExchangeEngine {

    private final List<DataImportHandler<?>> importHandlerList;
    private final List<DataExportHandler<?>> exportHandlerList;
    private final DataImportRunner dataImportRunner;
    private final CsvExchangeCodec csvExchangeCodec;
    private final DataExchangeTaskRecorder dataExchangeTaskRecorder;
    private final OssObjectService ossObjectService;

    private Map<String, DataImportHandler<?>> importHandlers;
    private Map<String, DataExportHandler<?>> exportHandlers;

    @PostConstruct
    void init() {
        importHandlers = new LinkedHashMap<>();
        for (DataImportHandler<?> handler : importHandlerList) {
            importHandlers.put(handler.scene(), handler);
        }
        exportHandlers = new LinkedHashMap<>();
        for (DataExportHandler<?> handler : exportHandlerList) {
            exportHandlers.put(handler.scene(), handler);
        }
    }

    public byte[] buildImportTemplate(String scene) {
        DataImportHandler<?> handler = requireImportHandler(scene);
        return dataImportRunner.buildTemplate(handler);
    }

    @SuppressWarnings("unchecked")
    public DataExchangeTaskVo importCsv(String scene, MultipartFile file, String remark) {
        DataImportHandler<Object> handler = (DataImportHandler<Object>) requireImportHandler(scene);
        return dataImportRunner.importCsv(handler, file, remark);
    }

    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    public OssObjectContent export(String scene, Map<String, String> params) {
        DataExportHandler<Object> handler = (DataExportHandler<Object>) requireExportHandler(scene);

        List<Object> data = handler.queryData(params);
        List<? extends List<?>> rows = data.stream()
                .map(handler::buildRow)
                .toList();
        String csv = csvExchangeCodec.writeRows(handler.headers(), rows);

        String filename = handler.resultFilename();
        OssObjectVo resultObject = ossObjectService.saveObject(
                handler.bucket(),
                filename,
                "text/csv;charset=UTF-8",
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)),
                handler.resultRemark()
        );

        dataExchangeTaskRecorder.recordSuccessfulExport(
                handler.scene(),
                resultObject.getId(),
                data.size(),
                "",
                handler.name() + "导出成功"
        );

        return ossObjectService.loadObjectContent(resultObject.getId());
    }

    private DataImportHandler<?> requireImportHandler(String scene) {
        return Optional.ofNullable(importHandlers.get(scene))
                .orElseThrow(() -> ApiException.badRequest("不支持的导入场景：" + scene));
    }

    private DataExportHandler<?> requireExportHandler(String scene) {
        return Optional.ofNullable(exportHandlers.get(scene))
                .orElseThrow(() -> ApiException.badRequest("不支持的导出场景：" + scene));
    }
}
