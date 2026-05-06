package com.berlin.aetherflow.wms.service;

import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.query.BatchTraceDetailQuery;
import com.berlin.aetherflow.wms.domain.query.BatchTraceQuery;
import com.berlin.aetherflow.wms.domain.vo.BatchTraceDetailVo;
import com.berlin.aetherflow.wms.domain.vo.BatchTraceVo;

/**
 * 批次追溯 Service。
 */
public interface BatchTraceService {

    PageResult<BatchTraceVo> queryList(BatchTraceQuery query);

    BatchTraceDetailVo queryDetail(BatchTraceDetailQuery query);
}
