--liquibase formatted sql

--changeset thread-a:20260505-1406-wms-wave-picking-exception-thread-a labels:wms context:all
--comment: 为波次生成拣货任务、部分拣货和拣货异常补充拣货任务字段与索引（幂等）
set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'picking_task' and column_name = 'source_type') = 0, 'alter table picking_task add column source_type varchar(16) not null default ''OUTBOUND_ORDER'' comment ''来源类型（OUTBOUND_ORDER/WAVE）'' after task_no', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'picking_task' and column_name = 'outbound_order_id' and is_nullable = 'NO') > 0, 'alter table picking_task modify column outbound_order_id bigint null comment ''出库单ID''', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'picking_task' and column_name = 'wave_id') = 0, 'alter table picking_task add column wave_id bigint null comment ''波次单ID'' after outbound_order_no', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'picking_task' and column_name = 'wave_no') = 0, 'alter table picking_task add column wave_no varchar(64) not null default '''' comment ''波次号'' after wave_id', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.statistics where table_schema = database() and table_name = 'picking_task' and index_name = 'uk_picking_task_outbound_order_id') > 0, 'alter table picking_task drop index uk_picking_task_outbound_order_id', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.statistics where table_schema = database() and table_name = 'picking_task' and index_name = 'idx_picking_task_outbound_order_id') = 0, 'alter table picking_task add key idx_picking_task_outbound_order_id (outbound_order_id)', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.statistics where table_schema = database() and table_name = 'picking_task' and index_name = 'uk_picking_task_wave_id') = 0, 'alter table picking_task add unique key uk_picking_task_wave_id (wave_id)', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'picking_task_item' and column_name = 'wave_id') = 0, 'alter table picking_task_item add column wave_id bigint null comment ''波次单ID'' after task_id', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'picking_task_item' and column_name = 'wave_item_id') = 0, 'alter table picking_task_item add column wave_item_id bigint null comment ''波次单明细ID'' after wave_id', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'picking_task_item' and column_name = 'exception_type') = 0, 'alter table picking_task_item add column exception_type varchar(32) not null default '''' comment ''异常类型'' after picked_qty', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'picking_task_item' and column_name = 'exception_qty') = 0, 'alter table picking_task_item add column exception_qty decimal(18, 2) not null default 0 comment ''异常数量'' after exception_type', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'picking_task_item' and column_name = 'exception_reason') = 0, 'alter table picking_task_item add column exception_reason varchar(255) not null default '''' comment ''异常原因'' after exception_qty', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.statistics where table_schema = database() and table_name = 'picking_task_item' and index_name = 'idx_picking_task_item_wave_id') = 0, 'alter table picking_task_item add key idx_picking_task_item_wave_id (wave_id)', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.statistics where table_schema = database() and table_name = 'picking_task_item' and index_name = 'idx_picking_task_item_wave_item_id') = 0, 'alter table picking_task_item add key idx_picking_task_item_wave_item_id (wave_item_id)', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.statistics where table_schema = database() and table_name = 'picking_task_item' and index_name = 'idx_picking_task_item_exception_type') = 0, 'alter table picking_task_item add key idx_picking_task_item_exception_type (exception_type)', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.statistics where table_schema = database() and table_name = 'picking_task_item' and index_name = 'uk_picking_task_item_allocation_id') > 0, 'alter table picking_task_item drop index uk_picking_task_item_allocation_id', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

set @ddl = if((select count(*) from information_schema.statistics where table_schema = database() and table_name = 'picking_task_item' and index_name = 'idx_picking_task_item_allocation_id') = 0, 'alter table picking_task_item add key idx_picking_task_item_allocation_id (allocation_id)', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;
