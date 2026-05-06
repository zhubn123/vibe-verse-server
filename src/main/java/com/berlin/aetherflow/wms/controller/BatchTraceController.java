package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.domain.query.BatchTraceDetailQuery;
import com.berlin.aetherflow.wms.domain.query.BatchTraceQuery;
import com.berlin.aetherflow.wms.domain.vo.BatchTraceDetailVo;
import com.berlin.aetherflow.wms.domain.vo.BatchTraceVo;
import com.berlin.aetherflow.wms.service.BatchTraceService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 批次追溯 Controller。
 */
@RestController
@RequestMapping("/api/wms/batch-traces")
@AllArgsConstructor
public class BatchTraceController {

    private final BatchTraceService batchTraceService;

    @Operation(summary = "批次追溯分页查询")
    @GetMapping
    public Result<PageResult<BatchTraceVo>> page(@Validated @ParameterObject BatchTraceQuery query) {
        return Result.success(batchTraceService.queryList(query));
    }

    @Operation(summary = "批次追溯详情")
    @GetMapping("/detail")
    public Result<BatchTraceDetailVo> detail(@Validated @ParameterObject BatchTraceDetailQuery query) {
        return Result.success(batchTraceService.queryDetail(query));
    }
}
