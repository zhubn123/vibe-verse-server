package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.service.OutboundAllocationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 出库库存分配 Controller。
 */
@RestController
@RequestMapping("/api/wms/outbound-orders")
@AllArgsConstructor
public class OutboundAllocationController {

    private final OutboundAllocationService outboundAllocationService;

    @Operation(summary = "预览出库单库存分配结果")
    @GetMapping("/{id}/allocation-preview")
    public Result<?> previewAllocation(@PathVariable Long id) {
        return Result.success(outboundAllocationService.previewAllocation(id));
    }

    @Operation(summary = "分配并锁定出库单库存")
    @PostMapping("/{id}/allocate")
    public Result<?> allocate(@PathVariable Long id) {
        return Result.success(outboundAllocationService.allocate(id));
    }

    @Operation(summary = "释放出库单库存分配")
    @PostMapping("/{id}/release-allocation")
    public Result<?> releaseAllocation(@PathVariable Long id) {
        return Result.success(outboundAllocationService.releaseAllocation(id));
    }
}
