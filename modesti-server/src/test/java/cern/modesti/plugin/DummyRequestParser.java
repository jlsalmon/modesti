package cern.modesti.plugin;

import cern.modesti.request.RequestImpl;
import cern.modesti.request.upload.parser.RequestParseResult;
import cern.modesti.request.upload.parser.RequestParser;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Collections;

/**
 * @author Justin Lewis Salmon
 */
public class DummyRequestParser implements RequestParser {

  @Override
  public RequestParseResult parseRequest(Sheet sheet) {
    return new RequestParseResult(new RequestImpl(), Collections.emptyList());
  }

  @Override
  public String getPluginId() {
    return DummyRequestProvider.DUMMY;
  }
}
