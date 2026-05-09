package com.fz.vibeverse.system.exchange.controller;

import com.fz.vibeverse.exception.Result;
import com.fz.vibeverse.system.audit.annotation.AuditLog;
import com.fz.vibeverse.system.audit.model.AuditType;
import com.fz.vibeverse.system.exchange.domain.vo.DataExchangeTaskVo;
import com.fz.vibeverse.system.exchange.service.DataExchangeEngine;
import com.fz.vibeverse.system.oss.domain.vo.OssObjectContent;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 导入导出引擎统一入口。
 */
@Validated
@RestController
@RequestMapping("/api/data-exchange")
@AllArgsConstructor
public class DataExchangeEngineController {

    private final DataExchangeEngine dataExchangeEngine;

    @Operation(summary = "下载导入模板")
    @GetMapping("/templates/{scene}")
    public ResponseEntity<Resource> downloadImportTemplate(@PathVariable String scene) {
        byte[] content = dataExchangeEngine.buildImportTemplate(scene);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .contentLength(content.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, buildAttachmentDisposition(scene + "-template.csv"))
                .body(new ByteArrayResource(content));
    }

    @Operation(summary = "触发导入")
    @PostMapping("/import/{scene}")
    @AuditLog(module = "DATA_EXCHANGE", type = AuditType.IMPORT, description = "导入数据", logParams = false)
    public Result<DataExchangeTaskVo> importData(
            @PathVariable String scene,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "remark", required = false) String remark
    ) {
        return Result.success(dataExchangeEngine.importCsv(scene, file, remark));
    }

    @Operation(summary = "触发导出")
    @GetMapping("/export/{scene}")
    @AuditLog(module = "DATA_EXCHANGE", type = AuditType.EXPORT, description = "导出数据")
    public ResponseEntity<Resource> exportData(
            @PathVariable String scene,
            @RequestParam Map<String, String> params
    ) {
        OssObjectContent content = dataExchangeEngine.export(scene, params);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.isNotBlank(content.contentType())) {
            mediaType = MediaType.parseMediaType(content.contentType());
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(content.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, buildAttachmentDisposition(content.originalName()))
                .body(content.resource());
    }

    private String buildAttachmentDisposition(String filename) {
        return ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build()
                .toString();
    }
}
