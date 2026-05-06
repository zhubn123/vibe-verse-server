package com.berlin.aetherflow.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.bo.WaveActionBo;
import com.berlin.aetherflow.wms.domain.bo.WaveBo;
import com.berlin.aetherflow.wms.domain.entity.WaveOrder;
import com.berlin.aetherflow.wms.domain.query.WaveQuery;
import com.berlin.aetherflow.wms.domain.vo.WaveDetailVo;
import com.berlin.aetherflow.wms.domain.vo.WaveVo;

/**
 * 波次规划 Service。
 */
public interface WaveService extends IService<WaveOrder> {

    PageResult<WaveVo> queryList(WaveQuery query);

    WaveDetailVo getDetailById(Long id);

    Long createWave(WaveBo bo);

    Boolean updateWave(Long id, WaveBo bo);

    Boolean applyAction(Long id, WaveActionBo bo);
}
