package com.fz.vibeverse.system.reminder.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.common.utils.OrderUtil;
import com.fz.vibeverse.exception.ApiException;
import com.fz.vibeverse.system.reminder.domain.bo.ReminderSaveBo;
import com.fz.vibeverse.system.reminder.domain.entity.SysReminder;
import com.fz.vibeverse.system.reminder.domain.query.ReminderQuery;
import com.fz.vibeverse.system.reminder.domain.vo.ReminderVo;
import com.fz.vibeverse.system.reminder.mapper.SysReminderMapper;
import com.fz.vibeverse.system.reminder.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 站内提醒服务实现。
 */
@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_DONE = "DONE";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final SysReminderMapper sysReminderMapper;

    @Override
    public PageResult<ReminderVo> queryReminderPage(ReminderQuery query) {
        ReminderQuery normalized = query == null ? new ReminderQuery() : query;
        IPage<SysReminder> page = new Page<>(normalized.getPageNo(), normalized.getPageSize());
        OrderUtil.addOrder(page, normalized.getSortBy(), normalized.getIsAsc());

        LambdaQueryWrapper<SysReminder> wrapper = buildOwnerQuery();
        String title = normalizeOptional(normalized.getTitle());
        if (StringUtils.isNotBlank(title)) {
            wrapper.like(SysReminder::getTitle, title);
        }

        String status = normalizeStatus(normalized.getStatus());
        if (Boolean.TRUE.equals(normalized.getDueOnly())) {
            wrapper.eq(SysReminder::getStatus, STATUS_PENDING)
                    .le(SysReminder::getRemindTime, LocalDateTime.now());
        } else if (StringUtils.isNotBlank(status)) {
            wrapper.eq(SysReminder::getStatus, status);
        }

        if (StringUtils.isBlank(normalized.getSortBy())) {
            wrapper.orderByAsc(SysReminder::getRemindTime);
        }

        IPage<SysReminder> result = sysReminderMapper.selectPage(page, wrapper);
        List<ReminderVo> records = result.getRecords()
                .stream()
                .map(this::toVo)
                .toList();
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public ReminderVo getReminderDetail(Long id) {
        return toVo(requireOwnReminder(id));
    }

    @Override
    public long countDueReminders() {
        LambdaQueryWrapper<SysReminder> wrapper = buildOwnerQuery()
                .eq(SysReminder::getStatus, STATUS_PENDING)
                .le(SysReminder::getRemindTime, LocalDateTime.now());
        return sysReminderMapper.selectCount(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReminder(ReminderSaveBo bo) {
        SysReminder reminder = new SysReminder();
        reminder.setUserId(StpUtil.getLoginIdAsLong());
        applySave(reminder, bo);
        reminder.setStatus(STATUS_PENDING);
        reminder.setDoneTime(null);
        reminder.setCancelTime(null);
        reminder.setSourceType("");
        reminder.setSourceId(null);
        sysReminderMapper.insert(reminder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReminder(Long id, ReminderSaveBo bo) {
        SysReminder existing = requireOwnReminder(id);
        if (!Objects.equals(existing.getStatus(), STATUS_PENDING)) {
            throw ApiException.badRequest("只有待提醒状态可以编辑");
        }

        SysReminder reminder = new SysReminder();
        reminder.setId(id);
        applySave(reminder, bo);
        sysReminderMapper.updateById(reminder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeReminder(Long id) {
        SysReminder existing = requireOwnReminder(id);
        if (Objects.equals(existing.getStatus(), STATUS_DONE)) {
            return;
        }
        if (Objects.equals(existing.getStatus(), STATUS_CANCELLED)) {
            throw ApiException.badRequest("已取消的提醒不能完成");
        }

        SysReminder reminder = new SysReminder();
        reminder.setId(id);
        reminder.setStatus(STATUS_DONE);
        reminder.setDoneTime(LocalDateTime.now());
        reminder.setCancelTime(null);
        sysReminderMapper.updateById(reminder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReminder(Long id) {
        SysReminder existing = requireOwnReminder(id);
        if (Objects.equals(existing.getStatus(), STATUS_CANCELLED)) {
            return;
        }
        if (Objects.equals(existing.getStatus(), STATUS_DONE)) {
            throw ApiException.badRequest("已完成的提醒不能取消");
        }

        SysReminder reminder = new SysReminder();
        reminder.setId(id);
        reminder.setStatus(STATUS_CANCELLED);
        reminder.setCancelTime(LocalDateTime.now());
        reminder.setDoneTime(null);
        sysReminderMapper.updateById(reminder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReminders(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw ApiException.badRequest("请选择要删除的提醒");
        }
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw ApiException.badRequest("提醒ID不能为空");
        }

        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>(ids);
        LambdaQueryWrapper<SysReminder> wrapper = buildOwnerQuery();
        wrapper.in(SysReminder::getId, uniqueIds);
        List<SysReminder> reminders = sysReminderMapper.selectList(wrapper);
        if (reminders.size() != uniqueIds.size()) {
            throw ApiException.business("存在提醒已被删除或无权访问");
        }
        sysReminderMapper.deleteByIds(uniqueIds);
    }

    private void applySave(SysReminder reminder, ReminderSaveBo bo) {
        reminder.setTitle(normalizeRequired(bo == null ? null : bo.getTitle(), "提醒标题不能为空"));
        reminder.setContent(StringUtils.defaultString(normalizeOptional(bo == null ? null : bo.getContent())));
        if (bo == null || bo.getRemindTime() == null) {
            throw ApiException.badRequest("提醒时间不能为空");
        }
        reminder.setRemindTime(bo.getRemindTime());
        reminder.setRemark(StringUtils.defaultString(normalizeOptional(bo.getRemark())));
    }

    private SysReminder requireOwnReminder(Long id) {
        if (id == null) {
            throw ApiException.badRequest("提醒ID不能为空");
        }
        LambdaQueryWrapper<SysReminder> wrapper = buildOwnerQuery();
        wrapper.eq(SysReminder::getId, id);
        SysReminder reminder = sysReminderMapper.selectOne(wrapper);
        if (reminder == null) {
            throw ApiException.business("提醒不存在或无权访问");
        }
        return reminder;
    }

    private LambdaQueryWrapper<SysReminder> buildOwnerQuery() {
        return new LambdaQueryWrapper<SysReminder>().eq(SysReminder::getUserId, StpUtil.getLoginIdAsLong());
    }

    private String normalizeStatus(String status) {
        return StringUtils.isBlank(status) ? null : status.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeRequired(String input, String message) {
        String normalized = normalizeOptional(input);
        if (StringUtils.isBlank(normalized)) {
            throw ApiException.badRequest(message);
        }
        return normalized;
    }

    private String normalizeOptional(String input) {
        return StringUtils.isBlank(input) ? null : input.trim();
    }

    private ReminderVo toVo(SysReminder reminder) {
        ReminderVo vo = new ReminderVo();
        vo.setId(reminder.getId());
        vo.setTitle(reminder.getTitle());
        vo.setContent(reminder.getContent());
        vo.setRemindTime(reminder.getRemindTime());
        vo.setStatus(reminder.getStatus());
        vo.setDue(Objects.equals(reminder.getStatus(), STATUS_PENDING)
                && reminder.getRemindTime() != null
                && !reminder.getRemindTime().isAfter(LocalDateTime.now()));
        vo.setDoneTime(reminder.getDoneTime());
        vo.setCancelTime(reminder.getCancelTime());
        vo.setRemark(reminder.getRemark());
        vo.setCreateTime(reminder.getCreateTime());
        vo.setUpdateTime(reminder.getUpdateTime());
        return vo;
    }
}
