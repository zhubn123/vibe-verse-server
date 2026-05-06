--liquibase formatted sql

--changeset codex:20260502-2440-system-wms-transfer-permission labels:system,wms context:all
--comment: WMS 移库单权限码、默认角色映射和库存流水业务类型（幂等）
insert into sys_permission (id, perm_key, perm_name, module, action, status, remark, create_by, create_time, update_by, update_time)
values (1930000000000001038, 'wms:transfer-order:view', '移库单查看', 'wms', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001039, 'wms:transfer-order:manage', '移库单管理', 'wms', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    perm_name = values(perm_name),
    module = values(module),
    action = values(action),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_role_permission (id, role_id, permission_id, create_by, create_time, update_by, update_time)
values (1930000000000002038, 1930000000000000001, 1930000000000001038, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002039, 1930000000000000001, 1930000000000001039, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002138, 1930000000000000002, 1930000000000001038, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002139, 1930000000000000002, 1930000000000001039, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002238, 1930000000000000003, 1930000000000001038, 'liquibase', now(), 'liquibase', now())
on duplicate key update
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_dict_item (dict_code, item_value, item_label, sort_order, status, remark, create_by, create_time, update_by, update_time)
values ('wms_stock_biz_type', 'TRANSFER_ORDER', '移库确认', 7, 0, '', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    item_label = values(item_label),
    sort_order = values(sort_order),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);
