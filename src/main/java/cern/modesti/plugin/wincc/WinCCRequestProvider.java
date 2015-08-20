package cern.modesti.plugin.wincc;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.request.Request;
import cern.modesti.request.RequestType;
import cern.modesti.validation.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Component
@Slf4j
public class WinCCRequestProvider implements RequestProvider {

  private static final String WINCC = "WINCC";

  /**
   * TODO create separate workflow for WINCC
   */
  private static final String WINCC_CREATE_KEY = "create-tim-points-0.2";

  @Autowired
  private ValidationService validationService;

  @Override
  public boolean validate(Request request) {
    log.info(format("validating WINCC request: %s", request));

    // TODO pull out WINCC validation from the core

    return validationService.validateRequest(request);
  }

  @Override
  public boolean configure(Request request) {
    log.info(format("configuring WINCC request: %s", request));

    // TODO implement WINCC sync here

    return false;
  }

  @Override
  public String getProcessKey(RequestType type) {
    return WINCC_CREATE_KEY;
  }

  @Override
  public boolean supports(Request request) {
    return WINCC.equals(request.getDomain());
  }
}
