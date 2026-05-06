package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.common.validation.CreateGroup;
import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.domain.bo.WaveActionBo;
import com.berlin.aetherflow.wms.domain.bo.WaveBo;
import com.berlin.aetherflow.wms.domain.query.WaveQuery;
import com.berlin.aetherflow.wms.domain.vo.WaveDetailVo;
import com.berlin.aetherflow.wms.domain.vo.WaveVo;
import com.berlin.aetherflow.wms.service.WaveService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 波次规划 Controller。
 */
@RestController
@RequestMapping("/api/wms/waves")
@AllArgsConstructor
public class WaveController {

    private final WaveService waveService;

    @Operation(summary = "波次分页查询")
    @GetMapping
    public Result<PageResult<WaveVo>> page(@ParameterObject @Validated WaveQuery query) {
        return Result.success(waveService.queryList(query));
    }

    @Operation(summary = "创建波次")
    @PostMapping
    public Result<Long> create(@Validated(CreateGroup.class) @RequestBody WaveBo bo) {
        bo.setId(null);
        return Result.success(waveService.createWave(bo));
    }

    @Operation(summary = "波次详情")
    @GetMapping("/{id}")
    public Result<WaveDetailVo> getById(@PathVariable Long id) {
        return Result.success(waveService.getDetailById(id));
    }

    @Operation(summary = "编辑波次")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody WaveBo bo) {
        return Result.success(waveService.updateWave(id, bo));
    }

    @Operation(summary = "执行波次动作")
    @PostMapping("/{id}/actions")
    public Result<Boolean> applyAction(@PathVariable Long id, @Valid @RequestBody WaveActionBo bo) {
        return Result.success(waveService.applyAction(id, bo));
    }
}
