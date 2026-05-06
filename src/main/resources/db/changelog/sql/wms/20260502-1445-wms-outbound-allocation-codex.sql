--liquibase formatted sql

--changeset codex:20260502-1445-wms-outbound-allocation labels:wms context:all
--comment: 新增出库库存分配明细，支撑锁定、释放和确认消费
create table if not exists outbound_allocation
(
    id            bigint primary key comment '主键ID',
    order_id      bigint         not null comment '出库单ID',
    order_item_id bigint         not null comment '出库单明细ID',
    line_no       int            not null default 1 comment '行号',
    inventory_id  bigint         not null comment '库存ID',
    warehouse_id  bigint         not null comment '仓库ID',
    location_id   bigint         not null comment '库位ID',
    material_id   bigint         not null comment '物料ID',
    allocated_qty decimal(18, 2) not null default 0 comment '分配数量',
    status        varchar(16)    not null default 'ACTIVE' comment '状态（ACTIVE/RELEASED/CONSUMED）',
    allocate_time datetime       not null default CURRENT_TIMESTAMP comment '分配时间',
    release_time  datetime       null comment '释放时间',
    consume_time  datetime       null comment '消费时间',
    remark        varchar(255)   not null default '' comment '备注',
    create_by     varchar(64)    not null default '' comment '创建人',
    create_time   datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by     varchar(64)    not null default '' comment '更新人',
    update_time   datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    key idx_outbound_allocation_order_id (order_id),
    key idx_outbound_allocation_order_item_id (order_item_id),
    key idx_outbound_allocation_inventory_id (inventory_id),
    key idx_outbound_allocation_status (status)
) engine = InnoDB comment '出库库存分配明细'
  collate = utf8mb4_unicode_ci;
