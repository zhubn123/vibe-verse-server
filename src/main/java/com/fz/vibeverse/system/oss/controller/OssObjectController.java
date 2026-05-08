package com.fz.vibeverse.system.oss.controller;

import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.exception.Result;
import com.fz.vibeverse.system.audit.annotation.AuditLog;
import com.fz.vibeverse.system.audit.model.AuditType;
import com.fz.vibeverse.system.oss.domain.query.OssObjectQuery;
import com.fz.vibeverse.system.oss.domain.vo.OssObjectContent;
import com.fz.vibeverse.system.oss.domain.vo.OssObjectVo;
import com.fz.vibeverse.system.oss.service.OssObjectService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 通用对象存储接口。
 */
@Validated
@RestController
@RequestMapping("/api/oss-objects")
@AllArgsConstructor
public class OssObjectController {

    private final OssObjectService ossObjectService;

    @Operation(summary = "分页查询对象元数据")
    @GetMapping
    public Result<PageResult<OssObjectVo>> queryObjectPage(@Validated OssObjectQuery query) {
        return Result.success(ossObjectService.queryObjectPage(query));
    }

    @Operation(summary = "查询对象元数据详情")
    @GetMapping("/{id}")
    public Result<OssObjectVo> getObjectDetail(@PathVariable Long id) {
        return Result.success(ossObjectService.getObjectDetail(id));
    }

    @Operation(summary = "上传对象")
    @PostMapping
    @AuditLog(module = "OSS", type = AuditType.CREATE, description = "上传文件", logParams = false)
    public Result<OssObjectVo> uploadObject(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bucket", required = false) String bucket,
            @RequestParam(value = "remark", required = false) String remark
    ) {
        return Result.success(ossObjectService.uploadObject(bucket, file, remark));
    }

    @Operation(summary = "下载对象")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadObject(@PathVariable Long id) {
        return buildFileResponse(ossObjectService.loadObjectContent(id), false);
    }

    @Operation(summary = "预览对象")
    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewObject(@PathVariable Long id) {
        return buildFileResponse(ossObjectService.loadObjectContent(id), true);
    }

    @Operation(summary = "批量删除对象")
    @DeleteMapping
    @AuditLog(module = "OSS", type = AuditType.DELETE, description = "删除文件")
    public Result<Void> deleteObjects(@RequestParam List<Long> ids) {
        ossObjectService.deleteObjects(ids);
        return Result.success();
    }

    private ResponseEntity<Resource> buildFileResponse(OssObjectContent content, boolean inline) {
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.isNotBlank(content.contentType())) {
            try {
                mediaType = MediaType.parseMediaType(content.contentType());
            } catch (IllegalArgumentException ignored) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        ContentDisposition contentDisposition = (inline ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(content.originalName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(content.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(content.resource());
    }
}
