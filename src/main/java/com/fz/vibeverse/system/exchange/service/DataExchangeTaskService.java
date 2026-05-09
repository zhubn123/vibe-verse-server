package com.fz.vibeverse.system.exchange.service;

import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.system.exchange.domain.query.DataExchangeTaskQuery;
import com.fz.vibeverse.system.exchange.domain.vo.DataExchangeTaskVo;

import java.util.List;

/**
 * 导入导出任务服务。
 */
public interface DataExchangeTaskService {

    PageResult<DataExchangeTaskVo> queryTaskPage(DataExchangeTaskQuery query);

    DataExchangeTaskVo getTaskDetail(Long id);

    void deleteTasks(List<Long> ids);
}
