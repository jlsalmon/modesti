package cern.modesti.workflow.task.scheduling;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Value;

import lombok.Data;

/**
 * Data model used to schedule tasks automatically (FOR_CONFIGURATION, FOR_CSAM_SYNC, etc)
 *  
 * @author Ivan Prieto Barreiro
 */
@Data
public class TaskSchedulerConfig {
  /** Week day when the configuration must be trigger. -1 for daily configuration**/
  @Value("${weekDay:4}")
  @Max(value=7, message = "Week day for scheduler configuration cannot be > 7")
  private int weekDay;
  
  @Value("${hour:14}")
  @Min(value=0, message = "Hour for scheduler configuration cannot be < 0")
  @Max(value=23, message = "Hour for scheduler configuration cannot be > 23")
  private int hour;
  
  @Value("${minute:0}")
  @Min(value=0, message = "Minute for scheduler configuration cannot be < 0")
  @Max(value=59, message = "Minute for scheduler configuration cannot be > 59")
  private int minute;
  
  @Value("${limitWeekDay:-1}")
  @Max(value=7, message = "Lmit week day for scheduler configuration cannot be > 7")
  private int limitWeekDay;
  
  @Value("${limitHour:-1}")
  @Max(value=23, message = "Limit hour for scheduler configuration cannot be > 23")
  private int limitHour;
  
  @Value("${limitMinute:-1}")
  @Max(value=59, message = "Limit minute for scheduler configuration cannot be > 59")
  private int limitMinute;
  
  /**
   * Checks whether the task must be scheduled daily
   * @return TRUE if and only if the task must be scheduled daily
   */
  public boolean isDailyTask() {
    return weekDay < 0;
  }

  /**
   * Checks whether the task must be scheduled weekly
   * @return TRUE if and only if the task must be scheduled weekly
   */
  public boolean isWeeklyTask() {
    return !isDailyTask();
  }

  /**
   * Checks whether the task has any time restriction
   * @return TRUE if and only if the task has a time restriction
   */
  public boolean hasTimeRestriction() {
    return limitWeekDay > 0 && limitHour >= 0 && limitMinute >= 0;
  }
}
