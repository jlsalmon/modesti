package cern.modesti.validation;

import cern.modesti.request.Request;
import cern.modesti.schema.Schema;
import cern.modesti.schema.SchemaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class ValidationService2 {

  @Autowired
  private SchemaService schemaService;

  /**
   * This service will reimplement the validations currently held within stored procedures on the database. The validations will be based on the flexible
   * JSON schema.
   *
   * @param request
   *
   * @return
   */
  public boolean validateRequest(Request request) {
    log.info(String.format("validating request %s", request.getRequestId()));

    // Get the schema
    Schema schema = schemaService.materialiseSchema(request, request.getCategories());


    return false;
  }
}