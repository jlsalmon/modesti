///**
// *
// */
//package cern.modesti.request;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.rest.webmvc.spi.BackendIdConverter;
//import org.springframework.stereotype.Component;
//
//import java.io.Serializable;
//
///**
// * @author Justin Lewis Salmon
// *
// */
//@Component
//public class RequestIdConverter implements BackendIdConverter {
//
//  @Autowired
//  private RequestRepository requestRepository;
//
//  /**
//   * TODO
//   *
//   * @param id
//   * @param entityType
//   * @return
//   */
//  @Override
//  public Serializable fromRequestId(String id, Class<?> entityType) {
//
//    if (entityType.equals(Request.class)) {
//      Request request = requestRepository.findOneByRequestId(id);
//
//      if (request != null) {
//        return request.getId();
//      }
//    }
//
//    return id;
//  }
//
//  /**
//   * TODO
//   *
//   * @param id
//   * @param entityType
//   * @return
//   */
//  @Override
//  public String toRequestId(Serializable id, Class<?> entityType) {
//
//    if (entityType.equals(Request.class)) {
//      Request request = requestRepository.findOne(id.toString());
//
//      if (request != null) {
//        return request.getRequestId();
//      }
//    }
//
//    return id.toString();
//  }
//
//  /**
//   * TODO
//   *
//   * @param delimiter
//   * @return
//   */
//  @Override
//  public boolean supports(Class<?> delimiter) {
//    return true;
//  }
//}
