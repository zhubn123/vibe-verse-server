--liquibase formatted sql

--changeset thread-a:20260504-1456-wms-wave-stock-count-review-thread-a labels:wms context:all
--comment: 新增波次规划表，并为库存盘点补充复盘审批字段
create table if not exists wave_order
(
    id           bigint primary key comment '主键ID',
    wave_no      varchar(64)    not null comment '波次号',
    warehouse_id bigint         not null comment '仓库ID',
    status       varchar(16)    not null default 'DRAFT' comment '状态（DRAFT/RELEASED/CANCELLED）',
    group_rule   varchar(32)    not null default 'MANUAL' comment '组波规则（MANUAL/BY_ORDER/BY_SKU/BY_AREA）',
    total_orders int            not null default 0 comment '出库单数',
    total_items  int            not null default 0 comment '明细行数',
    total_qty    decimal(18, 2) not null default 0 comment '计划数量',
    release_time datetime       null comment '释放时间',
    cancel_time  datetime       null comment '取消时间',
    remark       varchar(255)   not null default '' comment '备注',
    create_by    varchar(64)    not null default '' comment '创建人',
    create_time  datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by    varchar(64)    not null default '' comment '更新人',
    update_time  datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_wave_order_no (wave_no),
    key idx_wave_order_warehouse_status (warehouse_id, status),
    key idx_wave_order_status (status),
    key idx_wave_order_group_rule (group_rule)
) engine = InnoDB comment '波次单'
  collate = utf8mb4_unicode_ci;

create table if not exists wave_order_item
(
    id                     bigint primary key comment '主键ID',
    wave_id                bigint         not null comment '波次单ID',
    line_no                int            not null default 1 comment '行号',
    outbound_order_id      bigint         not null comment '出库单ID',
    outbound_order_no      varchar(64)    not null default '' comment '出库单号',
    outbound_order_item_id bigint         null comment '出库单明细ID',
    warehouse_id           bigint         not null comment '仓库ID',
    area_id                bigint         null comment '区域ID',
    location_id            bigint         null comment '库位ID',
    material_id            bigint         null comment '物料ID',
    batch_no               varchar(128)   not null default '' comment '批次号',
    production_date        date           null comment '生产日期',
    expiry_date            date           null comment '到期日期',
    planned_qty            decimal(18, 2) not null default 0 comment '计划数量',
    status                 varchar(16)    not null default 'DRAFT' comment '状态（DRAFT/RELEASED/CANCELLED）',
    remark                 varchar(255)   not null default '' comment '备注',
    create_by              varchar(64)    not null default '' comment '创建人',
    create_time            datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by              varchar(64)    not null default '' comment '更新人',
    update_time            datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_wave_order_item_line (wave_id, line_no),
    key idx_wave_order_item_wave_id (wave_id),
    key idx_wave_order_item_outbound_order_id (outbound_order_id),
    key idx_wave_order_item_outbound_item_id (outbound_order_item_id),
    key idx_wave_order_item_warehouse_location (warehouse_id, location_id),
    key idx_wave_order_item_material_batch (material_id, batch_no),
    key idx_wave_order_item_status (status)
) engine = InnoDB comment '波次单明细'
  collate = utf8mb4_unicode_ci;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'stock_count_order' and column_name = 'review_submit_time') = 0, 'alter table stock_count_order add column review_submit_time datetime null comment ''提交复盘时间'' after count_time', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'stock_count_order' and column_name = 'review_time') = 0, 'alter table stock_count_order add column review_time datetime null comment ''复盘审批时间'' after review_submit_time', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'stock_count_order' and column_name = 'review_by') = 0, 'alter table stock_count_order add column review_by varchar(64) not null default '''' comment ''复盘审批人'' after review_time', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'stock_count_order' and column_name = 'review_remark') = 0, 'alter table stock_count_order add column review_remark varchar(255) not null default '''' comment ''复盘审批备注'' after review_by', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'stock_count_item' and column_name = 'review_counted_qty') = 0, 'alter table stock_count_item add column review_counted_qty decimal(18, 2) null comment ''复盘实盘数量'' after counted_qty', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'stock_count_item' and column_name = 'difference_reason') = 0, 'alter table stock_count_item add column difference_reason varchar(255) not null default '''' comment ''差异原因'' after difference_qty', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'stock_count_item' and column_name = 'review_remark') = 0, 'alter table stock_count_item add column review_remark varchar(255) not null default '''' comment ''复盘备注'' after difference_reason', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;
