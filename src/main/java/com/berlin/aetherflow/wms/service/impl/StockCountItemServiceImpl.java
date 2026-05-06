package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.wms.domain.entity.StockCountItem;
import com.berlin.aetherflow.wms.mapper.StockCountItemMapper;
import com.berlin.aetherflow.wms.service.StockCountItemService;
import org.springframework.stereotype.Service;

/**
 * 盘点单明细 Service 实现。
 */
@Service
public class StockCountItemServiceImpl extends ServiceImpl<StockCountItemMapper, StockCountItem>
        implements StockCountItemService {
}
