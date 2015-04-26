package cern.modesti.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Component;

@Component
public class SearchTextConverter implements Converter<String, TextCriteria>{

  Logger logger = LoggerFactory.getLogger(SearchTextConverter.class);

  @Override
  public TextCriteria convert(String q) {
    logger.info("converting search text");
    return new TextCriteria().matching(q);
  }

}
