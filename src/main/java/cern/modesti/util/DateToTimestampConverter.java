package cern.modesti.util;

import org.springframework.core.convert.converter.Converter;

import java.sql.Timestamp;
import java.util.Date;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class DateToTimestampConverter implements Converter<Date, Timestamp> {
  @Override
  public Timestamp convert(Date source) {
    return new Timestamp(source.getTime());
  }
}