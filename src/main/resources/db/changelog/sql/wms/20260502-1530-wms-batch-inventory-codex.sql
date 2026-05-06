--liquibase formatted sql

--changeset codex:20260502-1530-wms-batch-inventory labels:wms context:all
--comment: 批次库存一期，为入库明细、出库明细、库存、库存流水和出库分配补充批次维度
set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'inventory' and column_name = 'batch_no') = 0, 'alter table inventory add column batch_no varchar(64) not null default '''' comment ''批次号'' after material_id', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'inventory' and column_name = 'production_date') = 0, 'alter table inventory add column production_date date null comment ''生产日期'' after batch_no', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'inventory' and column_name = 'expiry_date') = 0, 'alter table inventory add column expiry_date date null comment ''到期日期'' after production_date', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'inventory' and column_name = 'inbound_time') = 0, 'alter table inventory add column inbound_time datetime null comment ''首次入库时间'' after expiry_date', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.statistics where table_schema = database() and table_name = 'inventory' and index_name = 'uk_stock_dim') > 0, 'alter table inventory drop index uk_stock_dim', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.statistics where table_schema = database() and table_name = 'inventory' and index_name = 'uk_inventory_batch_dim') = 0, 'alter table inventory add unique key uk_inventory_batch_dim (warehouse_id, location_id, material_id, batch_no)', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.statistics where table_schema = database() and table_name = 'inventory' and index_name = 'idx_inventory_batch_no') = 0, 'alter table inventory add key idx_inventory_batch_no (batch_no)', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'inbound_order_item' and column_name = 'batch_no') = 0, 'alter table inbound_order_item add column batch_no varchar(64) not null default '''' comment ''批次号'' after location_id', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'inbound_order_item' and column_name = 'production_date') = 0, 'alter table inbound_order_item add column production_date date null comment ''生产日期'' after batch_no', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'inbound_order_item' and column_name = 'expiry_date') = 0, 'alter table inbound_order_item add column expiry_date date null comment ''到期日期'' after production_date', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.statistics where table_schema = database() and table_name = 'inbound_order_item' and index_name = 'idx_inbound_item_batch_no') = 0, 'alter table inbound_order_item add key idx_inbound_item_batch_no (batch_no)', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'outbound_order_item' and column_name = 'batch_no') = 0, 'alter table outbound_order_item add column batch_no varchar(64) not null default '''' comment ''批次号'' after location_id', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'outbound_order_item' and column_name = 'production_date') = 0, 'alter table outbound_order_item add column production_date date null comment ''生产日期'' after batch_no', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'outbound_order_item' and column_name = 'expiry_date') = 0, 'alter table outbound_order_item add column expiry_date date null comment ''到期日期'' after production_date', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.statistics where table_schema = database() and table_name = 'outbound_order_item' and index_name = 'idx_outbound_item_batch_no') = 0, 'alter table outbound_order_item add key idx_outbound_item_batch_no (batch_no)', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'inventory_adjustment_item' and column_name = 'batch_no') = 0, 'alter table inventory_adjustment_item add column batch_no varchar(64) not null default '''' comment ''批次号'' after location_id', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'inventory_adjustment_item' and column_name = 'production_date') = 0, 'alter table inventory_adjustment_item add column production_date date null comment ''生产日期'' after batch_no', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'inventory_adjustment_item' and column_name = 'expiry_date') = 0, 'alter table inventory_adjustment_item add column expiry_date date null comment ''到期日期'' after production_date', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.statistics where table_schema = database() and table_name = 'inventory_adjustment_item' and index_name = 'idx_adjustment_item_batch_no') = 0, 'alter table inventory_adjustment_item add key idx_adjustment_item_batch_no (batch_no)', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'stock_transaction' and column_name = 'batch_no') = 0, 'alter table stock_transaction add column batch_no varchar(64) not null default '''' comment ''批次号'' after material_id', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'stock_transaction' and column_name = 'production_date') = 0, 'alter table stock_transaction add column production_date date null comment ''生产日期'' after batch_no', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'stock_transaction' and column_name = 'expiry_date') = 0, 'alter table stock_transaction add column expiry_date date null comment ''到期日期'' after production_date', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.statistics where table_schema = database() and table_name = 'stock_transaction' and index_name = 'idx_stock_tx_batch_no') = 0, 'alter table stock_transaction add key idx_stock_tx_batch_no (batch_no)', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'outbound_allocation' and column_name = 'batch_no') = 0, 'alter table outbound_allocation add column batch_no varchar(64) not null default '''' comment ''批次号'' after material_id', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'outbound_allocation' and column_name = 'production_date') = 0, 'alter table outbound_allocation add column production_date date null comment ''生产日期'' after batch_no', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'outbound_allocation' and column_name = 'expiry_date') = 0, 'alter table outbound_allocation add column expiry_date date null comment ''到期日期'' after production_date', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.statistics where table_schema = database() and table_name = 'outbound_allocation' and index_name = 'idx_outbound_alloc_batch_no') = 0, 'alter table outbound_allocation add key idx_outbound_alloc_batch_no (batch_no)', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;
