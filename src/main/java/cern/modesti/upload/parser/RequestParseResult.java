package cern.modesti.upload.parser;

import cern.modesti.request.Request;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Represents the result of a successful request parse operation.
 *
 * @author Justin Lewis Salmon
 */
@Data
@AllArgsConstructor
public class RequestParseResult {
  private Request request;
  private List<String> warnings;
}
