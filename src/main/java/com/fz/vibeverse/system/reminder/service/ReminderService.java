package com.fz.vibeverse.system.reminder.service;

import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.system.reminder.domain.bo.ReminderSaveBo;
import com.fz.vibeverse.system.reminder.domain.query.ReminderQuery;
import com.fz.vibeverse.system.reminder.domain.vo.ReminderVo;

import java.util.List;

/**
 * 站内提醒服务。
 */
public interface ReminderService {

    PageResult<ReminderVo> queryReminderPage(ReminderQuery query);

    ReminderVo getReminderDetail(Long id);

    long countDueReminders();

    void createReminder(ReminderSaveBo bo);

    void updateReminder(Long id, ReminderSaveBo bo);

    void completeReminder(Long id);

    void cancelReminder(Long id);

    void deleteReminders(List<Long> ids);
}
