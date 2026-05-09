package com.fz.vibeverse.system.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.common.utils.OrderUtil;
import com.fz.vibeverse.exception.ApiException;
import com.fz.vibeverse.system.exchange.domain.entity.DataExchangeTask;
import com.fz.vibeverse.system.exchange.domain.query.DataExchangeTaskQuery;
import com.fz.vibeverse.system.exchange.domain.vo.DataExchangeTaskVo;
import com.fz.vibeverse.system.exchange.mapper.DataExchangeTaskMapper;
import com.fz.vibeverse.system.exchange.service.DataExchangeTaskService;
import com.fz.vibeverse.system.exchange.support.DataExchangeTaskRecorder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 导入导出任务服务实现。
 */
@Service
@RequiredArgsConstructor
public class DataExchangeTaskServiceImpl implements DataExchangeTaskService {

    private final DataExchangeTaskMapper dataExchangeTaskMapper;
    private final DataExchangeTaskRecorder dataExchangeTaskRecorder;

    @Override
    public PageResult<DataExchangeTaskVo> queryTaskPage(DataExchangeTaskQuery query) {
        DataExchangeTaskQuery normalized = query == null ? new DataExchangeTaskQuery() : query;
        IPage<DataExchangeTask> page = new Page<>(normalized.getPageNo(), normalized.getPageSize());
        OrderUtil.addOrder(page, normalized.getSortBy(), normalized.getIsAsc());

        LambdaQueryWrapper<DataExchangeTask> wrapper = new LambdaQueryWrapper<>();
        String direction = normalizeOptional(normalized.getDirection());
        if (StringUtils.isNotBlank(direction)) {
            wrapper.eq(DataExchangeTask::getDirection, direction.toUpperCase(Locale.ROOT));
        }

        String scene = normalizeOptional(normalized.getScene());
        if (StringUtils.isNotBlank(scene)) {
            wrapper.like(DataExchangeTask::getScene, scene);
        }

        String status = normalizeOptional(normalized.getStatus());
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(DataExchangeTask::getStatus, status.toUpperCase(Locale.ROOT));
        }

        if (StringUtils.isBlank(normalized.getSortBy())) {
            wrapper.orderByDesc(DataExchangeTask::getCreateTime);
        }

        IPage<DataExchangeTask> result = dataExchangeTaskMapper.selectPage(page, wrapper);
        List<DataExchangeTaskVo> records = result.getRecords()
                .stream()
                .map(dataExchangeTaskRecorder::toVo)
                .toList();
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public DataExchangeTaskVo getTaskDetail(Long id) {
        return dataExchangeTaskRecorder.toVo(requireTaskById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTasks(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw ApiException.badRequest("请选择要删除的任务");
        }
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw ApiException.badRequest("任务ID不能为空");
        }

        LinkedHashSet<Long> uniqueIds = ids.stream().collect(Collectors.toCollection(LinkedHashSet::new));
        List<DataExchangeTask> tasks = dataExchangeTaskMapper.selectByIds(uniqueIds).stream()
                .filter(Objects::nonNull)
                .toList();
        if (tasks.size() != uniqueIds.size()) {
            throw ApiException.business("存在任务已被删除或不存在");
        }
        dataExchangeTaskMapper.deleteByIds(uniqueIds);
    }

    private DataExchangeTask requireTaskById(Long id) {
        if (id == null) {
            throw ApiException.badRequest("任务ID不能为空");
        }
        DataExchangeTask task = dataExchangeTaskMapper.selectById(id);
        if (task == null) {
            throw ApiException.business("任务不存在");
        }
        return task;
    }

    private String normalizeOptional(String input) {
        return StringUtils.isBlank(input) ? null : input.trim();
    }
}
