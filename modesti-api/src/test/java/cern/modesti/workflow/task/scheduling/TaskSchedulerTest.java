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
  
  @Before
  public void init() throws Exception {
    scheduler = Whitebox.invokeConstructor(TaskScheduler.class);
  }

  @Test
  public void testScheduleDailyTaskForToday() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 9, 18, 34, 0);
    scheduleDailyTaskHelper(now, 19, 33, "2018-05-09T19:33:00Z");
  }
  
  @Test
  public void testScheduleDailyTaskForTomorrow() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 9, 18, 34, 0);
    scheduleDailyTaskHelper(now, 13, 33, "2018-05-10T13:33:00Z");
  }
  
  @Test
  public void testScheduleDailyTaskSkippingWeekend() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 11, 18, 34, 0);
    scheduleDailyTaskHelper(now, 13, 33, "2018-05-14T13:33:00Z");
  }
  
  private void scheduleDailyTaskHelper(GregorianCalendar now, int scheduleHour, int scheduleMinute, String expectedResult) throws Exception {
    now.setTimeZone(TimeZone.getDefault());
    String scheduledTime = Whitebox.invokeMethod(scheduler, "scheduleDailyTask", now, scheduleHour, scheduleMinute);
    assertEquals("The scheduled time is not correct", expectedResult, scheduledTime);
  }
  
  @Test
  public void testScheduleWeeklyTaskForThisWeek() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 7, 18, 34, 0);
    scheduleWeeklyTaskHelper(now, Calendar.WEDNESDAY, 12, "2018-05-09T12:00:00Z");
  }
  
  @Test
  public void testScheduleWeeklyTaskForToday() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 9, 11, 34, 0);
    scheduleWeeklyTaskHelper(now, Calendar.WEDNESDAY, 12, "2018-05-09T12:00:00Z");
  }
  
  @Test
  public void testScheduleWeeklyTaskForNextWeek() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 9, 12, 34, 0);
    scheduleWeeklyTaskHelper(now, Calendar.WEDNESDAY, 12, "2018-05-16T12:00:00Z");
  }
  
  @Test
  public void testScheduleWeeklyTaskForNextWeek2() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 10, 11, 34, 0);
    scheduleWeeklyTaskHelper(now, Calendar.WEDNESDAY, 12, "2018-05-16T12:00:00Z");
  }
  
  private void scheduleWeeklyTaskHelper(GregorianCalendar now, int weekDay, int scheduleHour, String expectedResult) throws Exception {
    now.setTimeZone(TimeZone.getDefault());
    String scheduledTime = Whitebox.invokeMethod(scheduler, "scheduleWeeklyTask", now, weekDay, scheduleHour);
    assertEquals("The scheduled time is not correct", expectedResult, scheduledTime);
  }
  
  @Test
  public void testScheduleWithLimitForToday() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 9, 9, 59, 0);
    scheduleWeeklyTaskWithLimitHelper(now, Calendar.WEDNESDAY, 12, Calendar.WEDNESDAY, 10, 0, "2018-05-09T12:00:00Z");
  }
  
  @Test
  public void testScheduleWithLimitForNextWeek() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 9, 10, 0, 0);
    scheduleWeeklyTaskWithLimitHelper(now, Calendar.WEDNESDAY, 12, Calendar.WEDNESDAY, 10, 0, "2018-05-16T12:00:00Z");
  }
  
  @Test
  public void testScheduleWithLimitForThisWeek() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 7, 10, 20, 0);
    scheduleWeeklyTaskWithLimitHelper(now, Calendar.WEDNESDAY, 12, Calendar.WEDNESDAY, 10, 0, "2018-05-09T12:00:00Z");
  }
  
  @Test
  public void testScheduleWithLimitForNextWeek2() throws Exception {
    GregorianCalendar now = new GregorianCalendar(2018, 4, 11, 9, 0, 0);
    scheduleWeeklyTaskWithLimitHelper(now, Calendar.WEDNESDAY, 12, Calendar.WEDNESDAY, 10, 0, "2018-05-16T12:00:00Z");
  }
  
  private void scheduleWeeklyTaskWithLimitHelper(GregorianCalendar now, int weekDay, int scheduleHour, 
      int limitWeekDay, int limitHour, int limitMinute, String expectedResult) throws Exception {
    now.setTimeZone(TimeZone.getDefault());
    String scheduledTime = Whitebox.invokeMethod(scheduler, "scheduleWeeklyTask", now, weekDay, scheduleHour, limitWeekDay, limitHour, limitMinute);
    assertEquals("The scheduled time is not correct", expectedResult, scheduledTime);
  }
}
