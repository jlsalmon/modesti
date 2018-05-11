package cern.modesti.workflow.task.scheduler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

/**
 * Class used to determine the time of the next automatic execution of a task.
 *  
 * @author Ivan Prieto Barreiro
 */
@Slf4j
public class TaskScheduler {
  
  private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  
  
  private TaskScheduler() {
    formatter.setTimeZone(TimeZone.getDefault());
  }
  
  /**
   * Schedule a daily task for a specific hour/minute.
   * @param hour The required hour for the schedule.
   * @param minute The required minute for the schedule.
   * @return The date for the task execution in the format "yyyy-MM-dd'T'HH:mm:ss'Z'"
   */
  public static String scheduleDailyTask(int hour, int minute) {
    return new TaskScheduler().scheduleDailyTask(new GregorianCalendar(), hour, minute);
  }
  
  private String scheduleDailyTask(GregorianCalendar now, int hour, int minute) {
    GregorianCalendar schedule = new GregorianCalendar(
        now.get(Calendar.YEAR), 
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH),
        hour,
        minute,
        now.get(Calendar.SECOND));
    schedule.setTimeZone(TimeZone.getDefault());
    
    if (schedule.before(now)) {
      schedule.add(Calendar.DAY_OF_MONTH, 1);
    }
    
    skipWeekends(schedule);
    
    return formatter.format(schedule.getTime());
  }
  
  /**
   * Schedule a weekly task for a specific day/hour.
   * @param weekDay The required weekday for the schedule ({@link Calendar#DAY_OF_WEEK}).
   * @param hour The required hour for the schedule.
   * @return The date for the task execution in the format "yyyy-MM-dd'T'HH:mm:ss'Z'"
   */
  public static String scheduleWeeklyTask(int weekDay, int hour) {
    return new TaskScheduler().scheduleWeeklyTask(new GregorianCalendar(), weekDay, hour);
  }
  
  private String scheduleWeeklyTask(GregorianCalendar now, int weekDay, int hour) {
    GregorianCalendar schedule = new GregorianCalendar(
        now.get(Calendar.YEAR), 
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH),
        hour,
        0,
        now.get(Calendar.SECOND));
    
    if (schedule.get(Calendar.DAY_OF_WEEK) != weekDay) {
      setForNextWeekDay(schedule, weekDay);
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
  
  /**
   * Schedule a weekly task for a specific day/hour with time restriction. If the task arrives after the limit date, it will
   * be scheduled for the next week.
   * @param weekDay The required weekday for the schedule ({@link Calendar#DAY_OF_WEEK}).
   * @param hour The required hour for the schedule.
   * @param limitWeekDay The limit weekday ({@link Calendar#DAY_OF_WEEK}).
   * @param limitHour The limit hour.
   * @param limitMinute The limit minute.
   * @return
   */
  public static String scheduleWeeklyTask(int weekDay, int hour, int limitWeekDay, int limitHour, int limitMinute) {
    return new TaskScheduler().scheduleWeeklyTask(new GregorianCalendar(), weekDay, hour, limitWeekDay, limitHour, limitMinute);
  }
  
  private String scheduleWeeklyTask(GregorianCalendar now, int weekDay, int hour, int limitWeekDay, int limitHour, int limitMinute) {
    
    GregorianCalendar schedule = new GregorianCalendar(
        now.get(Calendar.YEAR), 
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH),
        hour,
        0,
        now.get(Calendar.SECOND));
    
    if (schedule.get(Calendar.DAY_OF_WEEK) != weekDay) {
      setForNextWeekDay(schedule, weekDay);
    }
    
    GregorianCalendar limit = new GregorianCalendar(
        now.get(Calendar.YEAR), 
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH),
        limitHour,
        limitMinute,
        0);
    limit.add(Calendar.SECOND, -1);
    
    if (limit.get(Calendar.DAY_OF_WEEK) != limitWeekDay) {
      setForNextWeekDay(limit, limitWeekDay);
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
