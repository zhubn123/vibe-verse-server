package com.berlin.aetherflow.wms.domain.vo;

import com.berlin.aetherflow.wms.domain.entity.InboundOrder;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Inbound order detail response.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InboundOrder.class, convertGenerate = false)
public class InboundOrderDetailVo extends InboundOrderVo {

    private List<InboundOrderItemVo> orderItems;
}
