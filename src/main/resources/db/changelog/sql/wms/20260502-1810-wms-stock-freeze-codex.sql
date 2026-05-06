--liquibase formatted sql

--changeset codex:20260502-1810-wms-stock-freeze labels:wms context:all
--comment: 库存冻结一期，为库存表补充冻结库存字段
set @ddl = if((select count(*) from information_schema.columns where table_schema = database() and table_name = 'inventory' and column_name = 'frozen_quantity') = 0, 'alter table inventory add column frozen_quantity decimal(18, 2) not null default 0 comment ''冻结库存'' after locked_quantity', 'select 1');
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;
