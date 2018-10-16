package cern.modesti.predicate;

/**
 * Exception thrown by the {@link PredicateBuilder} when the predicate cannot be built.
 * 
 * @author Ivan Prieto Barreiro
 */
public class InvalidPredicateException extends RuntimeException {
  private static final long serialVersionUID = -102640136016604733L;

  /**
   * Constructor
   * @param msg Error message
   */
  public InvalidPredicateException(String msg) {
    super(msg); 
  }
  
  /**
   * Constructor
   * @param msg Error message
   * @param t Exception thrown while building the predicate
   */
  public InvalidPredicateException(String msg, Throwable t) {
    super(msg,t); 
  }
}
