package cern.modesti.request;

/**
 * Interface to be implemented by the different domain formatters.
 * 
 * The purpose of the formatter is to convert the point properties to the appropriate type
 * before inserting the request in MongoDB.
 * 
 * @author Ivan Prieto Barreiro
 */
public interface RequestFormatter {

  /**
   * Format the point properties of the request to the appropriate type.
   * @param request The request to be formatted.
   */
  void format(Request request);
}
