package com.berlin.aetherflow.wms.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量保存盘点明细实盘数量。
 */
@Data
public class StockCountItemsBo {

    @NotNull(message = "盘点明细不能为空")
    @Size(min = 1, message = "盘点明细不能为空")
    private List<@Valid StockCountItemBo> items;
}
