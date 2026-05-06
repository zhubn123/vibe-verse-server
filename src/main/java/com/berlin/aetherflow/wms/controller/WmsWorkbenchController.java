package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.service.WmsWorkbenchService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * WMS workbench Controller.
 */
@RestController
@RequestMapping("/api/wms/workbench")
@AllArgsConstructor
public class WmsWorkbenchController {

    private final WmsWorkbenchService wmsWorkbenchService;

    @Operation(summary = "WMS工作台概览")
    @GetMapping("/overview")
    public Result<?> overview(@RequestParam(required = false) Long warehouseId,
                              @RequestParam(required = false) Integer days) {
        return Result.success(wmsWorkbenchService.getOverview(warehouseId, days));
    }
}
