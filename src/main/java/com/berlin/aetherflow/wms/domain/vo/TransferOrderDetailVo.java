package com.berlin.aetherflow.wms.domain.vo;

import com.berlin.aetherflow.wms.domain.entity.TransferOrder;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 移库单详情返回对象。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = TransferOrder.class, convertGenerate = false)
public class TransferOrderDetailVo extends TransferOrderVo {

    private List<TransferOrderItemVo> orderItems;
}
