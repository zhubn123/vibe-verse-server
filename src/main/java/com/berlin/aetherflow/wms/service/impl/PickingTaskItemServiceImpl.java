package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.wms.domain.entity.PickingTaskItem;
import com.berlin.aetherflow.wms.mapper.PickingTaskItemMapper;
import com.berlin.aetherflow.wms.service.PickingTaskItemService;
import org.springframework.stereotype.Service;

/**
 * 拣货任务明细 Service 实现。
 */
@Service
public class PickingTaskItemServiceImpl extends ServiceImpl<PickingTaskItemMapper, PickingTaskItem>
        implements PickingTaskItemService {
}
