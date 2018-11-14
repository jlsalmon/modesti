package cern.modesti.workflow.task.scheduling;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

/**
 * Tests for the TaskScheduler
 * 
 * @author Ivan Prieto Barreiro
 */
public class TaskSchedulerTest {
  
  private TaskScheduler scheduler;
  private TaskSchedulerConfig config;
  
  @Before
  public void init() throws Exception {
    scheduler = Whitebox.invokeConstructor(TaskScheduler.class);
    config = new TaskSchedulerConfig();
  }

  @Test
  public void testScheduleDailyTaskForToday() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 9, 18, 34, 0);
    config.setHour(19);
    config.setMinute(33);
    scheduleDailyTaskHelper(now, config, "2018-05-09T19:33:00");
  }
  
  @Test
  public void testScheduleDailyTaskForTomorrow() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 9, 18, 34, 0);
    config.setHour(13);
    config.setMinute(33);
    scheduleDailyTaskHelper(now, config, "2018-05-10T13:33:00");
  }
  
  @Test
  public void testScheduleDailyTaskSkippingWeekend() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 11, 18, 34, 0);
    config.setHour(13);
    config.setMinute(33);
    scheduleDailyTaskHelper(now, config, "2018-05-14T13:33:00");
  }
  
  private void scheduleDailyTaskHelper(GregorianCalendar now, TaskSchedulerConfig config, String expectedResult) throws Exception {
    String scheduledTime = Whitebox.invokeMethod(scheduler, "scheduleDailyTask", now, config);
    assertEquals("The scheduled time is not correct", expectedResult, scheduledTime);
  }
  
  @Test
  public void testScheduleWeeklyTaskForThisWeek() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 7, 18, 34, 0);
    config.setWeekDay(Calendar.WEDNESDAY);
    config.setHour(12);
    scheduleWeeklyTaskHelper(now, config, "2018-05-09T12:00:00");
  }
  
  @Test
  public void testScheduleWeeklyTaskForToday() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 9, 11, 34, 0);
    config.setWeekDay(Calendar.WEDNESDAY);
    config.setHour(12);
    scheduleWeeklyTaskHelper(now, config, "2018-05-09T12:00:00");
  }
  
  @Test
  public void testScheduleWeeklyTaskForNextWeek() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 9, 12, 34, 0);
    config.setWeekDay(Calendar.WEDNESDAY);
    config.setHour(12);
    scheduleWeeklyTaskHelper(now, config, "2018-05-16T12:00:00");
  }
  
  @Test
  public void testScheduleWeeklyTaskForNextWeek2() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 10, 11, 34, 0);
    config.setWeekDay(Calendar.WEDNESDAY);
    config.setHour(12);
    scheduleWeeklyTaskHelper(now, config, "2018-05-16T12:00:00");
  }
  
  private void scheduleWeeklyTaskHelper(GregorianCalendar now, TaskSchedulerConfig config, String expectedResult) throws Exception {
    now.setTimeZone(TimeZone.getDefault());
    String scheduledTime = Whitebox.invokeMethod(scheduler, "scheduleWeeklyTask", now, config);
    assertEquals("The scheduled time is not correct", expectedResult, scheduledTime);
  }
  
  @Test
  public void testScheduleWithLimitForToday() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 9, 9, 59, 0);
    config.setWeekDay(Calendar.WEDNESDAY);
    config.setHour(12);
    config.setLimitWeekDay(Calendar.WEDNESDAY);
    config.setLimitHour(10);
    config.setLimitMinute(0);
    scheduleWeeklyTaskWithLimitHelper(now, config, "2018-05-09T12:00:00");
  }
  
  @Test
  public void testScheduleWithLimitForNextWeek() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 9, 10, 0, 0);
    config.setWeekDay(Calendar.WEDNESDAY);
    config.setHour(12);
    config.setLimitWeekDay(Calendar.WEDNESDAY);
    config.setLimitHour(10);
    config.setLimitMinute(0);
    scheduleWeeklyTaskWithLimitHelper(now, config, "2018-05-16T12:00:00");
  }
  
  @Test
  public void testScheduleWithLimitForThisWeek() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 7, 10, 20, 0);
    config.setWeekDay(Calendar.WEDNESDAY);
    config.setHour(12);
    config.setLimitWeekDay(Calendar.WEDNESDAY);
    config.setLimitHour(10);
    config.setLimitMinute(0);
    scheduleWeeklyTaskWithLimitHelper(now, config, "2018-05-09T12:00:00");
  }
  
  @Test
  public void testScheduleWithLimitForNextWeek2() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 11, 9, 0, 0);
    config.setWeekDay(Calendar.WEDNESDAY);
    config.setHour(12);
    config.setLimitWeekDay(Calendar.WEDNESDAY);
    config.setLimitHour(10);
    config.setLimitMinute(0);
    scheduleWeeklyTaskWithLimitHelper(now, config, "2018-05-16T12:00:00");
  }
  
  private void scheduleWeeklyTaskWithLimitHelper(GregorianCalendar now, TaskSchedulerConfig config, String expectedResult) throws Exception {
    now.setTimeZone(TimeZone.getDefault());
    String scheduledTime = Whitebox.invokeMethod(scheduler, "scheduleWeeklyTaskWithTimeLimit", now, config);
    assertEquals("The scheduled time is not correct", expectedResult, scheduledTime);
  }
}
