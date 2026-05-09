package com.fz.vibeverse.system.reminder.controller;

import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.exception.Result;
import com.fz.vibeverse.system.audit.annotation.AuditLog;
import com.fz.vibeverse.system.audit.model.AuditType;
import com.fz.vibeverse.system.reminder.domain.bo.ReminderSaveBo;
import com.fz.vibeverse.system.reminder.domain.query.ReminderQuery;
import com.fz.vibeverse.system.reminder.domain.vo.ReminderVo;
import com.fz.vibeverse.system.reminder.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 站内提醒接口。
 */
@Validated
@RestController
@RequestMapping("/api/reminders")
@AllArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @Operation(summary = "分页查询提醒")
    @GetMapping
    public Result<PageResult<ReminderVo>> queryReminderPage(@Validated ReminderQuery query) {
        return Result.success(reminderService.queryReminderPage(query));
    }

    @Operation(summary = "查询提醒详情")
    @GetMapping("/{id}")
    public Result<ReminderVo> getReminderDetail(@PathVariable Long id) {
        return Result.success(reminderService.getReminderDetail(id));
    }

    @Operation(summary = "查询到期提醒数量")
    @GetMapping("/due-count")
    public Result<Map<String, Long>> countDueReminders() {
        return Result.success(Map.of("count", reminderService.countDueReminders()));
    }

    @Operation(summary = "创建提醒")
    @PostMapping
    @AuditLog(module = "REMINDER", type = AuditType.CREATE, description = "创建提醒")
    public Result<Void> createReminder(@RequestBody @Valid ReminderSaveBo bo) {
        reminderService.createReminder(bo);
        return Result.success();
    }

    @Operation(summary = "更新提醒")
    @PutMapping("/{id}")
    @AuditLog(module = "REMINDER", type = AuditType.UPDATE, description = "更新提醒")
    public Result<Void> updateReminder(@PathVariable Long id, @RequestBody @Valid ReminderSaveBo bo) {
        reminderService.updateReminder(id, bo);
        return Result.success();
    }

    @Operation(summary = "完成提醒")
    @PutMapping("/{id}/complete")
    @AuditLog(module = "REMINDER", type = AuditType.UPDATE, description = "完成提醒")
    public Result<Void> completeReminder(@PathVariable Long id) {
        reminderService.completeReminder(id);
        return Result.success();
    }

    @Operation(summary = "取消提醒")
    @PutMapping("/{id}/cancel")
    @AuditLog(module = "REMINDER", type = AuditType.UPDATE, description = "取消提醒")
    public Result<Void> cancelReminder(@PathVariable Long id) {
        reminderService.cancelReminder(id);
        return Result.success();
    }

    @Operation(summary = "批量删除提醒")
    @DeleteMapping
    @AuditLog(module = "REMINDER", type = AuditType.DELETE, description = "删除提醒")
    public Result<Void> deleteReminders(@RequestParam List<Long> ids) {
        reminderService.deleteReminders(ids);
        return Result.success();
    }
}
