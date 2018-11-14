package cern.modesti.workflow.task.scheduling;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import lombok.extern.slf4j.Slf4j;

/**
 * Class used to determine the time of the next automatic execution of a task.
 *  
 * @author Ivan Prieto Barreiro
 */
@Slf4j
public class TaskScheduler {
  
  private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  
  /**
   * Schedule a task using the config values
   * @param config
   * @return
   */
  public static String scheduleTask(TaskSchedulerConfig config) {
    TaskScheduler scheduler = new TaskScheduler();
    GregorianCalendar now = new GregorianCalendar();
    
    if (config.isDailyTask()) {
      return scheduler.scheduleDailyTask(now, config);
    } else {
      // Weekly task
      if (config.hasTimeRestriction()) {
        return scheduler.scheduleWeeklyTaskWithTimeLimit(now, config);
      } else {
        return scheduler.scheduleWeeklyTask(now, config);
      }
    }
  }
  
  private String scheduleDailyTask(GregorianCalendar now, TaskSchedulerConfig config) {
    GregorianCalendar schedule = new GregorianCalendar(
        now.get(Calendar.YEAR), 
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH),
        config.getHour(),
        config.getMinute(),
        now.get(Calendar.SECOND));
    
    if (schedule.before(now)) {
      schedule.add(Calendar.DAY_OF_MONTH, 1);
    }
    
    skipWeekends(schedule);
    
    return formatter.format(schedule.getTime());
  }
  
  private String scheduleWeeklyTask(GregorianCalendar now, TaskSchedulerConfig config) {
    GregorianCalendar schedule = new GregorianCalendar(
        now.get(Calendar.YEAR), 
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH),
        config.getHour(),
        config.getMinute(),
        now.get(Calendar.SECOND));
    
    if (schedule.get(Calendar.DAY_OF_WEEK) != config.getWeekDay()) {
      setForNextWeekDay(schedule, config.getWeekDay());
    }
    
    if (schedule.before(now)) {
      schedule.add(Calendar.DAY_OF_MONTH, 7);
    }
    
    return formatter.format(schedule.getTime());
  }
  
  private void setForNextWeekDay(GregorianCalendar date, int weekDay) {
    int offset = weekDay - date.get(Calendar.DAY_OF_WEEK);
    if (offset < 0) {
      offset += 7;
    }
    date.add(Calendar.DAY_OF_WEEK, offset );
  }
  
  private String scheduleWeeklyTaskWithTimeLimit(GregorianCalendar now, TaskSchedulerConfig config) {
    GregorianCalendar schedule = new GregorianCalendar(
        now.get(Calendar.YEAR), 
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH),
        config.getHour(),
        config.getMinute(),
        now.get(Calendar.SECOND));
    
    if (schedule.get(Calendar.DAY_OF_WEEK) != config.getWeekDay()) {
      setForNextWeekDay(schedule, config.getWeekDay());
    }
    
    GregorianCalendar limit = new GregorianCalendar(
        now.get(Calendar.YEAR), 
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH),
        config.getLimitHour(),
        config.getLimitMinute(),
        0);
    limit.add(Calendar.SECOND, -1);
    
    if (limit.get(Calendar.DAY_OF_WEEK) != config.getLimitWeekDay()) {
      setForNextWeekDay(limit, config.getLimitWeekDay());
    }
    
    if (schedule.before(now) || now.after(limit)) {
      schedule.add(Calendar.DAY_OF_MONTH, 7);
    }
    
    return formatter.format(schedule.getTime());
  }
  
  private void skipWeekends(GregorianCalendar schedule) {
    if (schedule.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
      log.info("Next schedule is on Saturday... moving to Monday");
      schedule.add(Calendar.DAY_OF_MONTH, 2);
    }
    
    if (schedule.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
      log.info("Next schedule is on Sunday... moving to Monday");
      schedule.add(Calendar.DAY_OF_MONTH, 1);
    }
  }
}
