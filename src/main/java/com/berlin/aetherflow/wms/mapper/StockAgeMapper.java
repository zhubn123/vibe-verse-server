package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.berlin.aetherflow.wms.domain.query.StockAgeQuery;
import com.berlin.aetherflow.wms.domain.vo.StockAgeSummaryVo;
import com.berlin.aetherflow.wms.domain.vo.StockAgeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * 库龄分析只读 Mapper。
 */
@Mapper
public interface StockAgeMapper {

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
                i.batch_no as batchNo,
                i.production_date as productionDate,
                i.expiry_date as expiryDate,
                i.inbound_time as inboundTime,
                #{asOfDate} as asOfDate,
                case
                    when i.inbound_time is null then null
                    else datediff(#{asOfDate}, date(i.inbound_time))
                end as ageDays,
                case
                    when i.inbound_time is null then 'UNKNOWN'
                    when datediff(#{asOfDate}, date(i.inbound_time)) between 0 and 30 then 'AGE_0_30'
                    when datediff(#{asOfDate}, date(i.inbound_time)) between 31 and 60 then 'AGE_31_60'
                    when datediff(#{asOfDate}, date(i.inbound_time)) between 61 and 90 then 'AGE_61_90'
                    else 'AGE_OVER_90'
                end as ageBucket,
                case
                    when i.inbound_time is null then '未知'
                    when datediff(#{asOfDate}, date(i.inbound_time)) between 0 and 30 then '0-30天'
                    when datediff(#{asOfDate}, date(i.inbound_time)) between 31 and 60 then '31-60天'
                    when datediff(#{asOfDate}, date(i.inbound_time)) between 61 and 90 then '61-90天'
                    else '90天以上'
                end as ageBucketLabel,
                coalesce(i.quantity, 0) as quantity,
                coalesce(i.locked_quantity, 0) as lockedQuantity,
                coalesce(i.frozen_quantity, 0) as frozenQuantity,
                coalesce(i.quantity, 0) - coalesce(i.locked_quantity, 0) - coalesce(i.frozen_quantity, 0) as availableQuantity
            from inventory i
            left join warehouse w on w.id = i.warehouse_id
            left join location l on l.id = i.location_id
            left join area a on a.id = l.area_id
            left join material m on m.id = i.material_id
            <where>
                <if test="query.warehouseId != null">
                    and i.warehouse_id = #{query.warehouseId}
                </if>
                <if test="query.areaId != null">
                    and l.area_id = #{query.areaId}
                </if>
                <if test="query.locationId != null">
                    and i.location_id = #{query.locationId}
                </if>
                <if test="query.materialId != null">
                    and i.material_id = #{query.materialId}
                </if>
                <if test="query.batchNo != null and query.batchNo != ''">
                    and i.batch_no like concat('%', #{query.batchNo}, '%')
                </if>
                <if test="query.minAgeDays != null">
                    and i.inbound_time is not null
                    and datediff(#{asOfDate}, date(i.inbound_time)) >= #{query.minAgeDays}
                </if>
                <if test="query.maxAgeDays != null">
                    and i.inbound_time is not null
                    and datediff(#{asOfDate}, date(i.inbound_time)) &lt;= #{query.maxAgeDays}
                </if>
            </where>
            order by
                case when i.inbound_time is null then 1 else 0 end asc,
                i.inbound_time asc,
                i.id desc
            </script>
            """)
    IPage<StockAgeVo> selectStockAgePage(Page<StockAgeVo> page,
                                         @Param("query") StockAgeQuery query,
                                         @Param("asOfDate") LocalDate asOfDate);

    @Select("""
            <script>
            select
                #{asOfDate} as asOfDate,
                count(*) as totalCount,
                coalesce(sum(coalesce(i.quantity, 0)), 0) as totalQuantity,
                coalesce(sum(coalesce(i.quantity, 0) - coalesce(i.locked_quantity, 0) - coalesce(i.frozen_quantity, 0)), 0) as totalAvailableQuantity,
                cast(coalesce(sum(case when i.inbound_time is not null then 1 else 0 end), 0) as signed) as knownAgeCount,
                coalesce(sum(case when i.inbound_time is not null then coalesce(i.quantity, 0) else 0 end), 0) as knownAgeQuantity,
                cast(coalesce(sum(case when i.inbound_time is null then 1 else 0 end), 0) as signed) as unknownAgeCount,
                coalesce(sum(case when i.inbound_time is null then coalesce(i.quantity, 0) else 0 end), 0) as unknownAgeQuantity,
                cast(coalesce(sum(case when i.inbound_time is not null
                    and datediff(#{asOfDate}, date(i.inbound_time)) between 0 and 30 then 1 else 0 end), 0) as signed) as age0To30Count,
                coalesce(sum(case when i.inbound_time is not null
                    and datediff(#{asOfDate}, date(i.inbound_time)) between 0 and 30 then coalesce(i.quantity, 0) else 0 end), 0) as age0To30Quantity,
                cast(coalesce(sum(case when i.inbound_time is not null
                    and datediff(#{asOfDate}, date(i.inbound_time)) between 31 and 60 then 1 else 0 end), 0) as signed) as age31To60Count,
                coalesce(sum(case when i.inbound_time is not null
                    and datediff(#{asOfDate}, date(i.inbound_time)) between 31 and 60 then coalesce(i.quantity, 0) else 0 end), 0) as age31To60Quantity,
                cast(coalesce(sum(case when i.inbound_time is not null
                    and datediff(#{asOfDate}, date(i.inbound_time)) between 61 and 90 then 1 else 0 end), 0) as signed) as age61To90Count,
                coalesce(sum(case when i.inbound_time is not null
                    and datediff(#{asOfDate}, date(i.inbound_time)) between 61 and 90 then coalesce(i.quantity, 0) else 0 end), 0) as age61To90Quantity,
                cast(coalesce(sum(case when i.inbound_time is not null
                    and datediff(#{asOfDate}, date(i.inbound_time)) > 90 then 1 else 0 end), 0) as signed) as ageOver90Count,
                coalesce(sum(case when i.inbound_time is not null
                    and datediff(#{asOfDate}, date(i.inbound_time)) > 90 then coalesce(i.quantity, 0) else 0 end), 0) as ageOver90Quantity
            from inventory i
            left join location l on l.id = i.location_id
            <where>
                <if test="query.warehouseId != null">
                    and i.warehouse_id = #{query.warehouseId}
                </if>
                <if test="query.areaId != null">
                    and l.area_id = #{query.areaId}
                </if>
                <if test="query.locationId != null">
                    and i.location_id = #{query.locationId}
                </if>
                <if test="query.materialId != null">
                    and i.material_id = #{query.materialId}
                </if>
                <if test="query.batchNo != null and query.batchNo != ''">
                    and i.batch_no like concat('%', #{query.batchNo}, '%')
                </if>
                <if test="query.minAgeDays != null">
                    and i.inbound_time is not null
                    and datediff(#{asOfDate}, date(i.inbound_time)) >= #{query.minAgeDays}
                </if>
                <if test="query.maxAgeDays != null">
                    and i.inbound_time is not null
                    and datediff(#{asOfDate}, date(i.inbound_time)) &lt;= #{query.maxAgeDays}
                </if>
            </where>
            </script>
            """)
    StockAgeSummaryVo selectStockAgeSummary(@Param("query") StockAgeQuery query,
                                            @Param("asOfDate") LocalDate asOfDate);
}
