package com.berlin.aetherflow.wms.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量保存盘点复盘明细。
 */
@Data
public class StockCountReviewItemsBo {

    @NotNull(message = "复盘明细不能为空")
    @Size(min = 1, message = "复盘明细不能为空")
    private List<
            @NotNull(message = "复盘明细项不能为空")
            @Valid
            StockCountReviewItemBo
    > items;
}
