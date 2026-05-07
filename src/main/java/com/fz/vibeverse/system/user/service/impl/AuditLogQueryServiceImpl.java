package com.fz.vibeverse.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.common.utils.OrderUtil;
import com.fz.vibeverse.system.user.domain.entity.SysAuditLog;
import com.fz.vibeverse.system.user.domain.query.AuditLogQuery;
import com.fz.vibeverse.system.user.domain.vo.AuditLogVo;
import com.fz.vibeverse.system.user.mapper.SysAuditLogMapper;
import com.fz.vibeverse.system.user.service.AuditLogQueryService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审计日志查询服务实现。
 */
@Service
@RequiredArgsConstructor
public class AuditLogQueryServiceImpl implements AuditLogQueryService {

    private final SysAuditLogMapper sysAuditLogMapper;

    @Override
    public PageResult<AuditLogVo> queryAuditLogPage(AuditLogQuery query) {
        AuditLogQuery normalized = query == null ? new AuditLogQuery() : query;
        IPage<SysAuditLog> page = new Page<>(normalized.getPageNo(), normalized.getPageSize());
        OrderUtil.addOrder(page, normalized.getSortBy(), normalized.getIsAsc());

        LambdaQueryWrapper<SysAuditLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isBlank(normalized.getSortBy())) {
            wrapper.orderByDesc(SysAuditLog::getOccurTime);
        }

        String username = normalizeOptional(normalized.getUsername());
        if (StringUtils.isNotBlank(username)) {
            wrapper.eq(SysAuditLog::getUsername, username);
        }

        String eventType = normalizeOptional(normalized.getEventType());
        if (StringUtils.isNotBlank(eventType)) {
            wrapper.eq(SysAuditLog::getEventType, eventType);
        }

        String eventName = normalizeOptional(normalized.getEventName());
        if (StringUtils.isNotBlank(eventName)) {
            wrapper.like(SysAuditLog::getEventName, eventName);
        }

        if (normalized.getResult() != null) {
            wrapper.eq(SysAuditLog::getResult, normalized.getResult());
        }
        if (normalized.getStartTime() != null) {
            wrapper.ge(SysAuditLog::getOccurTime, normalized.getStartTime());
        }
        if (normalized.getEndTime() != null) {
            wrapper.le(SysAuditLog::getOccurTime, normalized.getEndTime());
        }

        IPage<SysAuditLog> result = sysAuditLogMapper.selectPage(page, wrapper);
        List<AuditLogVo> records = result.getRecords().stream()
                .map(this::toVo)
                .toList();
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    private AuditLogVo toVo(SysAuditLog log) {
        AuditLogVo vo = new AuditLogVo();
        vo.setId(log.getId());
        vo.setUserId(log.getUserId());
        vo.setUsername(log.getUsername());
        vo.setEventType(log.getEventType());
        vo.setEventName(log.getEventName());
        vo.setRequestUri(log.getRequestUri());
        vo.setClientIp(log.getClientIp());
        vo.setResult(log.getResult());
        vo.setMessage(log.getMessage());
        vo.setOccurTime(log.getOccurTime());
        return vo;
    }

    private String normalizeOptional(String input) {
        return StringUtils.isBlank(input) ? null : input.trim();
    }
}
