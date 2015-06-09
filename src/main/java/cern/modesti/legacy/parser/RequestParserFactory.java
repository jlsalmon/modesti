/**
 *
 */
package cern.modesti.legacy.parser;

import java.io.InputStream;

import cern.modesti.request.RequestType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.modesti.legacy.exception.RequestParseException;
import org.springframework.context.ApplicationContext;

/**
 * @author Justin Lewis Salmon
 *
 */
public class RequestParserFactory {

  private static final Logger LOG = LoggerFactory.getLogger(RequestParserFactory.class);

  /**
   * @param stream
   * @param context
   * @return
   */
  public static RequestParser createRequestParser(InputStream stream, ApplicationContext context) {
    Workbook workbook;
    try {
      workbook = WorkbookFactory.create(stream);
    } catch (Exception e) {
      LOG.error("Exception caught while creating request parser", e);
      throw new RequestParseException(e);
    }

    Sheet sheet = workbook.getSheetAt(0);
    Row header = sheet.getRow(0);

    String domain = header.getCell(0).getStringCellValue().trim();
    if (domain.equals(RequestType.Domain.TIM.toString())) {
      return new TIMRequestParser(sheet);
    } else if (domain.equals(RequestType.Domain.CSAM.toString())) {
      return new CSAMRequestParser(sheet, context);
    } else if (domain.equals(RequestType.Domain.PVSS.toString())) {
      return new PVSSRequestParser(sheet);
    } else {
      throw new RequestParseException("Domain " + domain + " is not valid and/or supported");
    }
  }
}
