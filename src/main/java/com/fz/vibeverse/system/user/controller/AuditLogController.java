package com.fz.vibeverse.system.user.controller;

import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.exception.Result;
import com.fz.vibeverse.system.user.domain.query.AuditLogQuery;
import com.fz.vibeverse.system.user.domain.vo.AuditLogVo;
import com.fz.vibeverse.system.user.service.AuditLogQueryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审计日志接口。
 */
@RestController
@RequestMapping("/api/audit-logs")
@AllArgsConstructor
public class AuditLogController {

    private final AuditLogQueryService auditLogQueryService;

    @Operation(summary = "分页查询审计日志")
    @GetMapping
    public Result<PageResult<AuditLogVo>> queryAuditLogPage(@Validated AuditLogQuery query) {
        return Result.success(auditLogQueryService.queryAuditLogPage(query));
    }
}
