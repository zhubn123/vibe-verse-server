package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.berlin.aetherflow.wms.domain.query.BatchTraceDetailQuery;
import com.berlin.aetherflow.wms.domain.query.BatchTraceQuery;
import com.berlin.aetherflow.wms.domain.vo.BatchTraceDetailVo;
import com.berlin.aetherflow.wms.domain.vo.BatchTraceInventoryVo;
import com.berlin.aetherflow.wms.domain.vo.BatchTraceOrderVo;
import com.berlin.aetherflow.wms.domain.vo.BatchTraceTransactionVo;
import com.berlin.aetherflow.wms.domain.vo.BatchTraceVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 批次追溯只读 Mapper。
 */
@Mapper
public interface BatchTraceMapper {

    @Select("""
            <script>
            select
                dim.warehouse_id as warehouseId,
                w.warehouse_code as warehouseCode,
                w.warehouse_name as warehouseName,
                dim.material_id as materialId,
                m.material_code as materialCode,
                m.material_name as materialName,
                m.specification as specification,
                m.unit as unit,
                dim.batch_no as batchNo,
                coalesce(inv.production_date, tx.production_date) as productionDate,
                coalesce(inv.expiry_date, tx.expiry_date) as expiryDate,
                coalesce(inv.quantity, 0) as quantity,
                coalesce(inv.locked_quantity, 0) as lockedQuantity,
                coalesce(inv.frozen_quantity, 0) as frozenQuantity,
                coalesce(inv.available_quantity, 0) as availableQuantity,
                coalesce(inv.location_count, 0) as locationCount,
                coalesce(tx.transaction_count, 0) as transactionCount,
                inv.earliest_inbound_time as earliestInboundTime,
                tx.latest_transaction_time as latestTransactionTime
            from (
                select
                    warehouse_id,
                    material_id,
                    coalesce(batch_no, '') as batch_no
                from inventory
                group by warehouse_id, material_id, coalesce(batch_no, '')
                union
                select
                    warehouse_id,
                    material_id,
                    coalesce(batch_no, '') as batch_no
                from stock_transaction
                group by warehouse_id, material_id, coalesce(batch_no, '')
            ) dim
            left join (
                select
                    warehouse_id,
                    material_id,
                    coalesce(batch_no, '') as batch_no,
                    min(production_date) as production_date,
                    min(expiry_date) as expiry_date,
                    coalesce(sum(coalesce(quantity, 0)), 0) as quantity,
                    coalesce(sum(coalesce(locked_quantity, 0)), 0) as locked_quantity,
                    coalesce(sum(coalesce(frozen_quantity, 0)), 0) as frozen_quantity,
                    coalesce(sum(coalesce(quantity, 0) - coalesce(locked_quantity, 0) - coalesce(frozen_quantity, 0)), 0) as available_quantity,
                    cast(count(distinct location_id) as signed) as location_count,
                    min(inbound_time) as earliest_inbound_time
                from inventory
                group by warehouse_id, material_id, coalesce(batch_no, '')
            ) inv on inv.warehouse_id = dim.warehouse_id
                and inv.material_id = dim.material_id
                and inv.batch_no = dim.batch_no
            left join (
                select
                    warehouse_id,
                    material_id,
                    coalesce(batch_no, '') as batch_no,
                    min(production_date) as production_date,
                    min(expiry_date) as expiry_date,
                    cast(count(*) as signed) as transaction_count,
                    max(operate_time) as latest_transaction_time
                from stock_transaction
                group by warehouse_id, material_id, coalesce(batch_no, '')
            ) tx on tx.warehouse_id = dim.warehouse_id
                and tx.material_id = dim.material_id
                and tx.batch_no = dim.batch_no
            left join warehouse w on w.id = dim.warehouse_id
            left join material m on m.id = dim.material_id
            <where>
                <if test="query.warehouseId != null">
                    and dim.warehouse_id = #{query.warehouseId}
                </if>
                <if test="query.materialId != null">
                    and dim.material_id = #{query.materialId}
                </if>
                <if test="query.batchNo != null">
                    and dim.batch_no = #{query.batchNo}
                </if>
                <if test="query.productionDateFrom != null">
                    and coalesce(inv.production_date, tx.production_date) &gt;= #{query.productionDateFrom}
                </if>
                <if test="query.productionDateTo != null">
                    and coalesce(inv.production_date, tx.production_date) &lt;= #{query.productionDateTo}
                </if>
                <if test="query.expiryDateFrom != null">
                    and coalesce(inv.expiry_date, tx.expiry_date) &gt;= #{query.expiryDateFrom}
                </if>
                <if test="query.expiryDateTo != null">
                    and coalesce(inv.expiry_date, tx.expiry_date) &lt;= #{query.expiryDateTo}
                </if>
                <if test="query.inboundTimeFrom != null">
                    and inv.earliest_inbound_time &gt;= #{query.inboundTimeFrom}
                </if>
                <if test="query.inboundTimeTo != null">
                    and inv.earliest_inbound_time &lt;= #{query.inboundTimeTo}
                </if>
            </where>
            order by
                case when tx.latest_transaction_time is null then 1 else 0 end asc,
                tx.latest_transaction_time desc,
                inv.earliest_inbound_time asc,
                dim.warehouse_id asc,
                dim.material_id asc,
                dim.batch_no asc
            </script>
            """)
    IPage<BatchTraceVo> selectBatchTracePage(Page<BatchTraceVo> page,
                                             @Param("query") BatchTraceQuery query);

    @Select("""
            <script>
            select
                #{query.warehouseId} as warehouseId,
                w.warehouse_code as warehouseCode,
                w.warehouse_name as warehouseName,
                base.material_id as materialId,
                m.material_code as materialCode,
                m.material_name as materialName,
                m.specification as specification,
                m.unit as unit,
                base.batch_no as batchNo,
                coalesce(inv.production_date, tx.production_date) as productionDate,
                coalesce(inv.expiry_date, tx.expiry_date) as expiryDate,
                coalesce(inv.quantity, 0) as quantity,
                coalesce(inv.locked_quantity, 0) as lockedQuantity,
                coalesce(inv.frozen_quantity, 0) as frozenQuantity,
                coalesce(inv.available_quantity, 0) as availableQuantity,
                coalesce(inv.location_count, 0) as locationCount,
                coalesce(tx.transaction_count, 0) as transactionCount,
                inv.earliest_inbound_time as earliestInboundTime,
                tx.latest_transaction_time as latestTransactionTime
            from (
                select
                    #{query.materialId} as material_id,
                    #{query.batchNo} as batch_no
            ) base
            left join material m on m.id = base.material_id
            left join warehouse w on w.id = #{query.warehouseId}
            left join (
                select
                    material_id,
                    coalesce(batch_no, '') as batch_no,
                    min(production_date) as production_date,
                    min(expiry_date) as expiry_date,
                    coalesce(sum(coalesce(quantity, 0)), 0) as quantity,
                    coalesce(sum(coalesce(locked_quantity, 0)), 0) as locked_quantity,
                    coalesce(sum(coalesce(frozen_quantity, 0)), 0) as frozen_quantity,
                    coalesce(sum(coalesce(quantity, 0) - coalesce(locked_quantity, 0) - coalesce(frozen_quantity, 0)), 0) as available_quantity,
                    cast(count(distinct location_id) as signed) as location_count,
                    min(inbound_time) as earliest_inbound_time
                from inventory
                where material_id = #{query.materialId}
                    and coalesce(batch_no, '') = #{query.batchNo}
                <if test="query.warehouseId != null">
                    and warehouse_id = #{query.warehouseId}
                </if>
                group by material_id, coalesce(batch_no, '')
            ) inv on inv.material_id = base.material_id
                and inv.batch_no = base.batch_no
            left join (
                select
                    material_id,
                    coalesce(batch_no, '') as batch_no,
                    min(production_date) as production_date,
                    min(expiry_date) as expiry_date,
                    cast(count(*) as signed) as transaction_count,
                    max(operate_time) as latest_transaction_time
                from stock_transaction
                where material_id = #{query.materialId}
                    and coalesce(batch_no, '') = #{query.batchNo}
                <if test="query.warehouseId != null">
                    and warehouse_id = #{query.warehouseId}
                </if>
                group by material_id, coalesce(batch_no, '')
            ) tx on tx.material_id = base.material_id
                and tx.batch_no = base.batch_no
            </script>
            """)
    BatchTraceDetailVo selectBatchTraceDetail(@Param("query") BatchTraceDetailQuery query);

    @Select("""
            <script>
            select
                i.id as inventoryId,
                i.warehouse_id as warehouseId,
                w.warehouse_code as warehouseCode,
                w.warehouse_name as warehouseName,
                l.area_id as areaId,
                a.area_code as areaCode,
                a.area_name as areaName,
                i.location_id as locationId,
                l.location_code as locationCode,
                l.location_name as locationName,
                i.material_id as materialId,
                m.material_code as materialCode,
                m.material_name as materialName,
                m.specification as specification,
                m.unit as unit,
                coalesce(i.batch_no, '') as batchNo,
                i.production_date as productionDate,
                i.expiry_date as expiryDate,
                i.inbound_time as inboundTime,
                coalesce(i.quantity, 0) as quantity,
                coalesce(i.locked_quantity, 0) as lockedQuantity,
                coalesce(i.frozen_quantity, 0) as frozenQuantity,
                coalesce(i.quantity, 0) - coalesce(i.locked_quantity, 0) - coalesce(i.frozen_quantity, 0) as availableQuantity
            from inventory i
            left join warehouse w on w.id = i.warehouse_id
            left join location l on l.id = i.location_id
            left join area a on a.id = l.area_id
            left join material m on m.id = i.material_id
            where i.material_id = #{query.materialId}
                and coalesce(i.batch_no, '') = #{query.batchNo}
            <if test="query.warehouseId != null">
                and i.warehouse_id = #{query.warehouseId}
            </if>
            order by
                i.warehouse_id asc,
                l.area_id asc,
                i.location_id asc,
                i.inbound_time asc,
                i.id asc
            </script>
            """)
    List<BatchTraceInventoryVo> selectBatchTraceInventories(@Param("query") BatchTraceDetailQuery query);

    @Select("""
            <script>
            select
                st.id as id,
                st.biz_type as bizType,
                st.biz_id as bizId,
                st.warehouse_id as warehouseId,
                w.warehouse_code as warehouseCode,
                w.warehouse_name as warehouseName,
                st.area_id as areaId,
                a.area_code as areaCode,
                a.area_name as areaName,
                st.location_id as locationId,
                l.location_code as locationCode,
                l.location_name as locationName,
                st.material_id as materialId,
                m.material_code as materialCode,
                m.material_name as materialName,
                m.specification as specification,
                m.unit as unit,
                coalesce(st.batch_no, '') as batchNo,
                st.production_date as productionDate,
                st.expiry_date as expiryDate,
                coalesce(st.change_qty, 0) as changeQty,
                coalesce(st.before_qty, 0) as beforeQty,
                coalesce(st.after_qty, 0) as afterQty,
                st.operator_id as operatorId,
                st.create_by as operatorName,
                st.operate_time as operateTime,
                st.remark as remark
            from stock_transaction st
            left join warehouse w on w.id = st.warehouse_id
            left join location l on l.id = st.location_id
            left join area a on a.id = coalesce(st.area_id, l.area_id)
            left join material m on m.id = st.material_id
            where st.material_id = #{query.materialId}
                and coalesce(st.batch_no, '') = #{query.batchNo}
            <if test="query.warehouseId != null">
                and st.warehouse_id = #{query.warehouseId}
            </if>
            order by
                st.operate_time desc,
                st.id desc
            </script>
            """)
    List<BatchTraceTransactionVo> selectBatchTraceTransactions(@Param("query") BatchTraceDetailQuery query);

    @Select("""
            <script>
            select *
            from (
                select
                    'INBOUND_ORDER' as orderType,
                    '入库单' as orderTypeLabel,
                    'IN' as direction,
                    '入库' as directionLabel,
                    io.id as orderId,
                    io.order_no as orderNo,
                    ioi.id as itemId,
                    ioi.line_no as lineNo,
                    cast(io.status as char) as status,
                    io.warehouse_id as warehouseId,
                    w.warehouse_code as warehouseCode,
                    w.warehouse_name as warehouseName,
                    l.area_id as areaId,
                    a.area_code as areaCode,
                    a.area_name as areaName,
                    ioi.location_id as locationId,
                    l.location_code as locationCode,
                    l.location_name as locationName,
                    null as sourceLocationId,
                    null as sourceLocationCode,
                    null as sourceLocationName,
                    ioi.location_id as targetLocationId,
                    l.location_code as targetLocationCode,
                    l.location_name as targetLocationName,
                    ioi.material_id as materialId,
                    m.material_code as materialCode,
                    m.material_name as materialName,
                    m.specification as specification,
                    m.unit as unit,
                    coalesce(ioi.batch_no, '') as batchNo,
                    ioi.production_date as productionDate,
                    ioi.expiry_date as expiryDate,
                    coalesce(ioi.planned_qty, 0) as plannedQuantity,
                    coalesce(ioi.received_qty, 0) as actualQuantity,
                    cast(0 as decimal(18, 6)) as differenceQuantity,
                    io.inbound_time as businessTime,
                    coalesce(ioi.remark, io.remark) as remark
                from inbound_order_item ioi
                inner join inbound_order io on io.id = ioi.order_id
                left join warehouse w on w.id = io.warehouse_id
                left join location l on l.id = ioi.location_id
                left join area a on a.id = l.area_id
                left join material m on m.id = ioi.material_id
                where ioi.material_id = #{query.materialId}
                    and coalesce(ioi.batch_no, '') = #{query.batchNo}
                <if test="query.warehouseId != null">
                    and io.warehouse_id = #{query.warehouseId}
                </if>
                union all
                select
                    'OUTBOUND_ORDER' as orderType,
                    '出库单' as orderTypeLabel,
                    'OUT' as direction,
                    '出库' as directionLabel,
                    oo.id as orderId,
                    oo.order_no as orderNo,
                    ooi.id as itemId,
                    ooi.line_no as lineNo,
                    cast(oo.status as char) as status,
                    oo.warehouse_id as warehouseId,
                    w.warehouse_code as warehouseCode,
                    w.warehouse_name as warehouseName,
                    l.area_id as areaId,
                    a.area_code as areaCode,
                    a.area_name as areaName,
                    ooi.location_id as locationId,
                    l.location_code as locationCode,
                    l.location_name as locationName,
                    ooi.location_id as sourceLocationId,
                    l.location_code as sourceLocationCode,
                    l.location_name as sourceLocationName,
                    null as targetLocationId,
                    null as targetLocationCode,
                    null as targetLocationName,
                    ooi.material_id as materialId,
                    m.material_code as materialCode,
                    m.material_name as materialName,
                    m.specification as specification,
                    m.unit as unit,
                    coalesce(ooi.batch_no, '') as batchNo,
                    ooi.production_date as productionDate,
                    ooi.expiry_date as expiryDate,
                    coalesce(ooi.planned_qty, 0) as plannedQuantity,
                    coalesce(ooi.shipped_qty, 0) as actualQuantity,
                    cast(0 as decimal(18, 6)) as differenceQuantity,
                    oo.outbound_time as businessTime,
                    coalesce(ooi.remark, oo.remark) as remark
                from outbound_order_item ooi
                inner join outbound_order oo on oo.id = ooi.order_id
                left join warehouse w on w.id = oo.warehouse_id
                left join location l on l.id = ooi.location_id
                left join area a on a.id = l.area_id
                left join material m on m.id = ooi.material_id
                where ooi.material_id = #{query.materialId}
                    and coalesce(ooi.batch_no, '') = #{query.batchNo}
                <if test="query.warehouseId != null">
                    and oo.warehouse_id = #{query.warehouseId}
                </if>
                union all
                select
                    'TRANSFER_ORDER' as orderType,
                    '移库单' as orderTypeLabel,
                    'TRANSFER' as direction,
                    '移库' as directionLabel,
                    tro.id as orderId,
                    tro.order_no as orderNo,
                    toi.id as itemId,
                    toi.line_no as lineNo,
                    cast(tro.status as char) as status,
                    tro.warehouse_id as warehouseId,
                    w.warehouse_code as warehouseCode,
                    w.warehouse_name as warehouseName,
                    sl.area_id as areaId,
                    sa.area_code as areaCode,
                    sa.area_name as areaName,
                    toi.source_location_id as locationId,
                    sl.location_code as locationCode,
                    sl.location_name as locationName,
                    toi.source_location_id as sourceLocationId,
                    sl.location_code as sourceLocationCode,
                    sl.location_name as sourceLocationName,
                    toi.target_location_id as targetLocationId,
                    tl.location_code as targetLocationCode,
                    tl.location_name as targetLocationName,
                    toi.material_id as materialId,
                    m.material_code as materialCode,
                    m.material_name as materialName,
                    m.specification as specification,
                    m.unit as unit,
                    coalesce(toi.batch_no, '') as batchNo,
                    toi.production_date as productionDate,
                    toi.expiry_date as expiryDate,
                    cast(0 as decimal(18, 6)) as plannedQuantity,
                    coalesce(toi.transfer_qty, 0) as actualQuantity,
                    cast(0 as decimal(18, 6)) as differenceQuantity,
                    tro.transfer_time as businessTime,
                    coalesce(toi.remark, tro.remark) as remark
                from transfer_order_item toi
                inner join transfer_order tro on tro.id = toi.order_id
                left join warehouse w on w.id = tro.warehouse_id
                left join location sl on sl.id = toi.source_location_id
                left join area sa on sa.id = sl.area_id
                left join location tl on tl.id = toi.target_location_id
                left join material m on m.id = toi.material_id
                where toi.material_id = #{query.materialId}
                    and coalesce(toi.batch_no, '') = #{query.batchNo}
                <if test="query.warehouseId != null">
                    and tro.warehouse_id = #{query.warehouseId}
                </if>
                union all
                select
                    'INVENTORY_ADJUSTMENT' as orderType,
                    '库存调整单' as orderTypeLabel,
                    ia.adjust_type as direction,
                    case
                        when ia.adjust_type = 'INCREASE' then '调增'
                        when ia.adjust_type = 'DECREASE' then '调减'
                        else '调整'
                    end as directionLabel,
                    ia.id as orderId,
                    ia.order_no as orderNo,
                    iai.id as itemId,
                    iai.line_no as lineNo,
                    cast(ia.status as char) as status,
                    ia.warehouse_id as warehouseId,
                    w.warehouse_code as warehouseCode,
                    w.warehouse_name as warehouseName,
                    coalesce(ia.area_id, l.area_id) as areaId,
                    a.area_code as areaCode,
                    a.area_name as areaName,
                    iai.location_id as locationId,
                    l.location_code as locationCode,
                    l.location_name as locationName,
                    case when ia.adjust_type = 'DECREASE' then iai.location_id else null end as sourceLocationId,
                    case when ia.adjust_type = 'DECREASE' then l.location_code else null end as sourceLocationCode,
                    case when ia.adjust_type = 'DECREASE' then l.location_name else null end as sourceLocationName,
                    case when ia.adjust_type = 'INCREASE' then iai.location_id else null end as targetLocationId,
                    case when ia.adjust_type = 'INCREASE' then l.location_code else null end as targetLocationCode,
                    case when ia.adjust_type = 'INCREASE' then l.location_name else null end as targetLocationName,
                    iai.material_id as materialId,
                    m.material_code as materialCode,
                    m.material_name as materialName,
                    m.specification as specification,
                    m.unit as unit,
                    coalesce(iai.batch_no, '') as batchNo,
                    iai.production_date as productionDate,
                    iai.expiry_date as expiryDate,
                    cast(0 as decimal(18, 6)) as plannedQuantity,
                    coalesce(iai.adjust_qty, 0) as actualQuantity,
                    cast(0 as decimal(18, 6)) as differenceQuantity,
                    ia.adjust_time as businessTime,
                    coalesce(iai.remark, ia.remark) as remark
                from inventory_adjustment_item iai
                inner join inventory_adjustment ia on ia.id = iai.order_id
                left join warehouse w on w.id = ia.warehouse_id
                left join location l on l.id = iai.location_id
                left join area a on a.id = coalesce(ia.area_id, l.area_id)
                left join material m on m.id = iai.material_id
                where iai.material_id = #{query.materialId}
                    and coalesce(iai.batch_no, '') = #{query.batchNo}
                <if test="query.warehouseId != null">
                    and ia.warehouse_id = #{query.warehouseId}
                </if>
                union all
                select
                    'STOCK_COUNT' as orderType,
                    '盘点单' as orderTypeLabel,
                    'COUNT' as direction,
                    '盘点' as directionLabel,
                    sco.id as orderId,
                    sco.count_no as orderNo,
                    sci.id as itemId,
                    sci.line_no as lineNo,
                    sco.status as status,
                    sci.warehouse_id as warehouseId,
                    w.warehouse_code as warehouseCode,
                    w.warehouse_name as warehouseName,
                    sci.area_id as areaId,
                    a.area_code as areaCode,
                    a.area_name as areaName,
                    sci.location_id as locationId,
                    l.location_code as locationCode,
                    l.location_name as locationName,
                    case when coalesce(sci.difference_qty, 0) &lt; 0 then sci.location_id else null end as sourceLocationId,
                    case when coalesce(sci.difference_qty, 0) &lt; 0 then l.location_code else null end as sourceLocationCode,
                    case when coalesce(sci.difference_qty, 0) &lt; 0 then l.location_name else null end as sourceLocationName,
                    case when coalesce(sci.difference_qty, 0) &gt; 0 then sci.location_id else null end as targetLocationId,
                    case when coalesce(sci.difference_qty, 0) &gt; 0 then l.location_code else null end as targetLocationCode,
                    case when coalesce(sci.difference_qty, 0) &gt; 0 then l.location_name else null end as targetLocationName,
                    sci.material_id as materialId,
                    m.material_code as materialCode,
                    m.material_name as materialName,
                    m.specification as specification,
                    m.unit as unit,
                    coalesce(sci.batch_no, '') as batchNo,
                    sci.production_date as productionDate,
                    sci.expiry_date as expiryDate,
                    coalesce(sci.expected_qty, 0) as plannedQuantity,
                    coalesce(sci.review_counted_qty, sci.counted_qty, 0) as actualQuantity,
                    coalesce(sci.difference_qty, 0) as differenceQuantity,
                    coalesce(sco.adjust_time, sco.count_time) as businessTime,
                    coalesce(sci.remark, sco.remark) as remark
                from stock_count_item sci
                inner join stock_count_order sco on sco.id = sci.count_id
                left join warehouse w on w.id = sci.warehouse_id
                left join area a on a.id = sci.area_id
                left join location l on l.id = sci.location_id
                left join material m on m.id = sci.material_id
                where sci.material_id = #{query.materialId}
                    and coalesce(sci.batch_no, '') = #{query.batchNo}
                <if test="query.warehouseId != null">
                    and sci.warehouse_id = #{query.warehouseId}
                </if>
            ) clue
            order by
                case when clue.businessTime is null then 1 else 0 end asc,
                clue.businessTime desc,
                clue.orderId desc,
                clue.lineNo asc
            </script>
            """)
    List<BatchTraceOrderVo> selectBatchTraceOrders(@Param("query") BatchTraceDetailQuery query);
}
