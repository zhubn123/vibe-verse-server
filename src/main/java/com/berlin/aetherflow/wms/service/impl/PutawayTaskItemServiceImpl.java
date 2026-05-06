package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.wms.domain.entity.PutawayTaskItem;
import com.berlin.aetherflow.wms.mapper.PutawayTaskItemMapper;
import com.berlin.aetherflow.wms.service.PutawayTaskItemService;
import org.springframework.stereotype.Service;

/**
 * 上架任务明细 Service 实现。
 */
@Service
public class PutawayTaskItemServiceImpl extends ServiceImpl<PutawayTaskItemMapper, PutawayTaskItem>
        implements PutawayTaskItemService {
}
