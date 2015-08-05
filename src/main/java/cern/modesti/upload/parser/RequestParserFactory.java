/**
 *
 */
package cern.modesti.upload.parser;

import cern.modesti.upload.exception.RequestParseException;
import cern.modesti.repository.jpa.equipment.MonitoringEquipmentRepository;
import cern.modesti.repository.jpa.location.functionality.FunctionalityRepository;
import cern.modesti.repository.jpa.person.PersonRepository;
import cern.modesti.repository.jpa.subsystem.SubSystemRepository;
import cern.modesti.request.RequestType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * @author Justin Lewis Salmon
 *
 */
@Component
public class RequestParserFactory {

  private static final Logger LOG = LoggerFactory.getLogger(RequestParserFactory.class);

  @Autowired
  private PersonRepository personRepository;

  @Autowired
  private SubSystemRepository subSystemRepository;

  @Autowired
  private FunctionalityRepository functionalityRepository;

  @Autowired
  private MonitoringEquipmentRepository monitoringEquipmentRepository;

  /**
   * @param stream
   *
   * @return
   */
  public RequestParser createRequestParser(InputStream stream) {
    RequestParser requestParser;
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
      requestParser = new TIMRequestParser(sheet);
    } else if (domain.equals(RequestType.Domain.CSAM.toString())) {
      requestParser = new CSAMRequestParser(sheet);
    } else if (domain.equals(RequestType.Domain.PVSS.toString())) {
      requestParser = new PVSSRequestParser(sheet);
    } else {
      throw new RequestParseException("Domain " + domain + " is not valid and/or supported");
    }

    requestParser.setPersonRepository(personRepository);
    requestParser.setSubSystemRepository(subSystemRepository);
    requestParser.setMonitoringEquipmentRepository(monitoringEquipmentRepository);
    requestParser.setFunctionalityRepository(functionalityRepository);
    return requestParser;
  }
}
