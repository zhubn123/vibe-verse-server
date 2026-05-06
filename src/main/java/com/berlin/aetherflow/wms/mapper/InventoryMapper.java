package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.wms.domain.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
* @author berlin
* @description 针对表【stock(库存表)】的数据库操作Mapper
* @createDate 2026-04-15 16:17:27
* @Entity com.berlin.aetherflow.wms.domain.entity.Inventory
*/
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    @Update("""
            update inventory
            set quantity = quantity - #{deductQty},
                update_by = #{updateBy},
                update_time = now()
            where id = #{id}
              and quantity - locked_quantity - frozen_quantity >= #{deductQty}
            """)
    int deductAvailableQuantity(@Param("id") Long id,
                                @Param("deductQty") BigDecimal deductQty,
                                @Param("updateBy") String updateBy);

    @Update("""
            update inventory
            set locked_quantity = locked_quantity + #{lockQty},
                update_by = #{updateBy},
                update_time = now()
            where id = #{id}
              and quantity - locked_quantity - frozen_quantity >= #{lockQty}
            """)
    int lockAvailableQuantity(@Param("id") Long id,
                              @Param("lockQty") BigDecimal lockQty,
                              @Param("updateBy") String updateBy);

    @Update("""
            update inventory
            set locked_quantity = locked_quantity - #{releaseQty},
                update_by = #{updateBy},
                update_time = now()
            where id = #{id}
              and locked_quantity >= #{releaseQty}
            """)
    int releaseLockedQuantity(@Param("id") Long id,
                              @Param("releaseQty") BigDecimal releaseQty,
                              @Param("updateBy") String updateBy);

    @Update("""
            update inventory
            set quantity = quantity - #{consumeQty},
                locked_quantity = locked_quantity - #{consumeQty},
                update_by = #{updateBy},
                update_time = now()
            where id = #{id}
              and quantity >= #{consumeQty}
              and locked_quantity >= #{consumeQty}
            """)
    int consumeLockedQuantity(@Param("id") Long id,
                              @Param("consumeQty") BigDecimal consumeQty,
                              @Param("updateBy") String updateBy);

    @Update("""
            update inventory
            set frozen_quantity = frozen_quantity + #{freezeQty},
                update_by = #{updateBy},
                update_time = now()
            where id = #{id}
              and quantity - locked_quantity - frozen_quantity >= #{freezeQty}
            """)
    int freezeAvailableQuantity(@Param("id") Long id,
                                @Param("freezeQty") BigDecimal freezeQty,
                                @Param("updateBy") String updateBy);

    @Update("""
            update inventory
            set frozen_quantity = frozen_quantity - #{unfreezeQty},
                update_by = #{updateBy},
                update_time = now()
            where id = #{id}
              and frozen_quantity >= #{unfreezeQty}
            """)
    int unfreezeQuantity(@Param("id") Long id,
                         @Param("unfreezeQty") BigDecimal unfreezeQty,
                         @Param("updateBy") String updateBy);
}




