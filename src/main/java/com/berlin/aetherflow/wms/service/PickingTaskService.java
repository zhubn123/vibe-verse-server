package com.berlin.aetherflow.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.bo.PickingTaskActionBo;
import com.berlin.aetherflow.wms.domain.entity.PickingTask;
import com.berlin.aetherflow.wms.domain.query.PickingTaskQuery;
import com.berlin.aetherflow.wms.domain.vo.PickingTaskDetailVo;
import com.berlin.aetherflow.wms.domain.vo.PickingTaskVo;

/**
 * 拣货任务 Service。
 */
public interface PickingTaskService extends IService<PickingTask> {

    PageResult<PickingTaskVo> queryList(PickingTaskQuery query);

    PickingTaskDetailVo getDetailById(Long id);

    Long createFromOutboundOrder(Long outboundOrderId);

    Long createFromWave(Long waveId);

    Boolean applyAction(Long id, PickingTaskActionBo bo);
}
