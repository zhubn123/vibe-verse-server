package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.wms.domain.entity.TransferOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 移库单 Mapper。
 */
@Mapper
public interface TransferOrderMapper extends BaseMapper<TransferOrder> {

    @Update("""
            update transfer_order
            set status = #{confirmedStatus},
                transfer_time = case when transfer_time is null then #{transferTime} else transfer_time end,
                update_by = #{updateBy},
                update_time = now()
            where id = #{id}
              and status = #{draftStatus}
            """)
    int confirmDraftOrder(@Param("id") Long id,
                          @Param("draftStatus") Integer draftStatus,
                          @Param("confirmedStatus") Integer confirmedStatus,
                          @Param("transferTime") LocalDateTime transferTime,
                          @Param("updateBy") String updateBy);
}
