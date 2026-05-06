--liquibase formatted sql

--changeset codex:20260502-2345-system-wms-stock-count-permission labels:system,wms context:all
--comment: WMS 盘点管理权限码、默认角色映射、盘点状态字典和库存流水业务类型（幂等）
insert into sys_permission (id, perm_key, perm_name, module, action, status, remark, create_by, create_time, update_by, update_time)
values (1930000000000001034, 'wms:stock-count:view', '库存盘点查看', 'wms', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001035, 'wms:stock-count:manage', '库存盘点管理', 'wms', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    perm_name = values(perm_name),
    module = values(module),
    action = values(action),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_role_permission (id, role_id, permission_id, create_by, create_time, update_by, update_time)
values (1930000000000002034, 1930000000000000001, 1930000000000001034, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002035, 1930000000000000001, 1930000000000001035, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002134, 1930000000000000002, 1930000000000001034, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002135, 1930000000000000002, 1930000000000001035, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002234, 1930000000000000003, 1930000000000001034, 'liquibase', now(), 'liquibase', now())
on duplicate key update
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_dict_type (dict_code, dict_name, module, status, remark, create_by, create_time, update_by, update_time)
values ('wms_stock_count_status', 'WMS 盘点单状态', 'wms', 0, '库存盘点单状态', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    dict_name = values(dict_name),
    module = values(module),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_dict_item (dict_code, item_value, item_label, sort_order, status, remark, create_by, create_time, update_by, update_time)
values ('wms_stock_count_status', 'PENDING', '待盘点', 1, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_stock_count_status', 'ADJUSTED', '已调账', 2, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_stock_count_status', 'CANCELLED', '已取消', 3, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_stock_biz_type', 'STOCK_COUNT', '盘点调账', 6, 0, '', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    item_label = values(item_label),
    sort_order = values(sort_order),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);
