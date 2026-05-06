--liquibase formatted sql

--changeset thread-a:20260504-1456-system-wms-wave-stock-age-seed-thread-a labels:system,wms context:all
--comment: WMS 波次和库龄权限码、默认角色映射、波次字典及盘点复盘状态字典（幂等）
insert into sys_permission (id, perm_key, perm_name, module, action, status, remark, create_by, create_time, update_by, update_time)
values (1930000000000001040, 'wms:wave:view', '波次规划查看', 'wms', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001041, 'wms:wave:manage', '波次规划管理', 'wms', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001042, 'wms:stock-age:view', '库龄查看', 'wms', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    perm_name = values(perm_name),
    module = values(module),
    action = values(action),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_role_permission (id, role_id, permission_id, create_by, create_time, update_by, update_time)
values (1930000000000002040, 1930000000000000001, 1930000000000001040, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002041, 1930000000000000001, 1930000000000001041, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002042, 1930000000000000001, 1930000000000001042, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002140, 1930000000000000002, 1930000000000001040, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002141, 1930000000000000002, 1930000000000001041, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002142, 1930000000000000002, 1930000000000001042, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002240, 1930000000000000003, 1930000000000001040, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002242, 1930000000000000003, 1930000000000001042, 'liquibase', now(), 'liquibase', now())
on duplicate key update
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_dict_type (dict_code, dict_name, module, status, remark, create_by, create_time, update_by, update_time)
values ('wms_wave_status', 'WMS 波次状态', 'wms', 0, '波次规划状态', 'liquibase', now(), 'liquibase', now()),
       ('wms_wave_group_rule', 'WMS 波次组波规则', 'wms', 0, '波次规划组波规则', 'liquibase', now(), 'liquibase', now()),
       ('wms_stock_count_status', 'WMS 盘点单状态', 'wms', 0, '库存盘点单状态', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    dict_name = values(dict_name),
    module = values(module),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_dict_item (dict_code, item_value, item_label, sort_order, status, remark, create_by, create_time, update_by, update_time)
values ('wms_wave_status', 'DRAFT', '草稿', 1, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_wave_status', 'RELEASED', '已释放', 2, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_wave_status', 'CANCELLED', '已取消', 3, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_wave_group_rule', 'MANUAL', '手工组波', 1, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_wave_group_rule', 'BY_ORDER', '按订单组波', 2, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_wave_group_rule', 'BY_SKU', '按 SKU 组波', 3, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_wave_group_rule', 'BY_AREA', '按区域组波', 4, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_stock_count_status', 'REVIEWING', '复盘中', 4, 0, '', 'liquibase', now(), 'liquibase', now()),
       ('wms_stock_count_status', 'APPROVED', '审批通过', 5, 0, '', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    item_label = values(item_label),
    sort_order = values(sort_order),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);
