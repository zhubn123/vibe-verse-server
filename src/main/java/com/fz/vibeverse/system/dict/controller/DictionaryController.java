package com.fz.vibeverse.system.dict.controller;

import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.exception.Result;
import com.fz.vibeverse.system.audit.annotation.AuditLog;
import com.fz.vibeverse.system.audit.model.AuditType;
import com.fz.vibeverse.system.dict.domain.bo.DictItemSaveBo;
import com.fz.vibeverse.system.dict.domain.bo.DictTypeSaveBo;
import com.fz.vibeverse.system.dict.domain.query.DictTypeQuery;
import com.fz.vibeverse.system.dict.domain.vo.DictItemVo;
import com.fz.vibeverse.system.dict.domain.vo.DictTypeVo;
import com.fz.vibeverse.system.dict.service.DictionaryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统字典接口。
 */
@Validated
@RestController
@RequestMapping("/api/dictionaries")
@AllArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @Operation(summary = "分页查询字典类型")
    @GetMapping
    public Result<PageResult<DictTypeVo>> queryTypePage(@Validated DictTypeQuery query) {
        return Result.success(dictionaryService.queryTypePage(query));
    }

    @Operation(summary = "查询字典类型详情")
    @GetMapping("/{id}")
    public Result<DictTypeVo> getTypeDetail(@PathVariable Integer id) {
        return Result.success(dictionaryService.getTypeDetail(id));
    }

    @Operation(summary = "查询字典项")
    @GetMapping("/{dictCode}/items")
    public Result<List<DictItemVo>> listItems(
            @PathVariable
            @Size(max = 128, message = "字典编码长度不能超过128位")
            @Pattern(regexp = "^[A-Za-z0-9_:-]+$", message = "字典编码格式非法")
            String dictCode,
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(dictionaryService.listItems(dictCode, includeDisabled));
    }

    @Operation(summary = "创建字典类型")
    @PostMapping
    @AuditLog(module = "DICT", type = AuditType.CREATE, description = "创建字典类型")
    public Result<Void> createType(@RequestBody @Valid DictTypeSaveBo bo) {
        dictionaryService.createType(bo);
        return Result.success();
    }

    @Operation(summary = "更新字典类型")
    @PutMapping("/{id}")
    @AuditLog(module = "DICT", type = AuditType.UPDATE, description = "更新字典类型")
    public Result<Void> updateType(@PathVariable Integer id, @RequestBody @Valid DictTypeSaveBo bo) {
        dictionaryService.updateType(id, bo);
        return Result.success();
    }

    @Operation(summary = "批量删除字典类型")
    @DeleteMapping
    @AuditLog(module = "DICT", type = AuditType.DELETE, description = "删除字典类型")
    public Result<Void> deleteTypes(@RequestParam List<Integer> ids) {
        dictionaryService.deleteTypes(ids);
        return Result.success();
    }

    @Operation(summary = "创建字典项")
    @PostMapping("/{dictCode}/items")
    @AuditLog(module = "DICT", type = AuditType.CREATE, description = "创建字典项")
    public Result<Void> createItem(
            @PathVariable
            @Size(max = 128, message = "字典编码长度不能超过128位")
            @Pattern(regexp = "^[A-Za-z0-9_:-]+$", message = "字典编码格式非法")
            String dictCode,
            @RequestBody @Valid DictItemSaveBo bo) {
        dictionaryService.createItem(dictCode, bo);
        return Result.success();
    }

    @Operation(summary = "更新字典项")
    @PutMapping("/items/{id}")
    @AuditLog(module = "DICT", type = AuditType.UPDATE, description = "更新字典项")
    public Result<Void> updateItem(@PathVariable Integer id, @RequestBody @Valid DictItemSaveBo bo) {
        dictionaryService.updateItem(id, bo);
        return Result.success();
    }

    @Operation(summary = "批量删除字典项")
    @DeleteMapping("/items")
    @AuditLog(module = "DICT", type = AuditType.DELETE, description = "删除字典项")
    public Result<Void> deleteItems(@RequestParam List<Integer> ids) {
        dictionaryService.deleteItems(ids);
        return Result.success();
    }
}
