package com.berlin.aetherflow.wms.service;

import com.berlin.aetherflow.wms.domain.vo.WmsWorkbenchVo;

/**
 * WMS workbench service.
 */
public interface WmsWorkbenchService {

    WmsWorkbenchVo getOverview(Long warehouseId, Integer days);
}
