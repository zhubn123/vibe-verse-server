package com.berlin.aetherflow.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.bo.PutawayTaskActionBo;
import com.berlin.aetherflow.wms.domain.entity.InboundOrder;
import com.berlin.aetherflow.wms.domain.entity.InboundOrderItem;
import com.berlin.aetherflow.wms.domain.entity.PutawayTask;
import com.berlin.aetherflow.wms.domain.query.PutawayTaskQuery;
import com.berlin.aetherflow.wms.domain.vo.PutawayTaskDetailVo;
import com.berlin.aetherflow.wms.domain.vo.PutawayTaskVo;

import java.util.List;

/**
 * 上架任务 Service。
 */
public interface PutawayTaskService extends IService<PutawayTask> {

    PageResult<PutawayTaskVo> queryList(PutawayTaskQuery query);

    PutawayTaskDetailVo getDetailById(Long id);

    void createFromInboundOrder(InboundOrder order, List<InboundOrderItem> orderItems);

    Boolean applyAction(Long id, PutawayTaskActionBo bo);
}
