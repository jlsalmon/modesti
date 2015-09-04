//package cern.modesti.refpoint;
//
//import cern.modesti.repository.refpoint.RefPoint;
//import cern.modesti.repository.refpoint.RefPointRepository;
//import cern.modesti.request.point.Point;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Service;
//
//import java.beans.BeanInfo;
//import java.beans.IntrospectionException;
//import java.beans.Introspector;
//import java.beans.PropertyDescriptor;
//import java.lang.reflect.InvocationTargetException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static java.lang.String.format;
//
///**
// * This class synchronises all points in timrefdb to modestidb.
// *
// * @author Justin Lewis Salmon
// */
//@Service
//@Slf4j
//@Profile({"dev", "prod"})
//public class RefPointSynchroniser {
//
//  @Autowired
//  PointRepository pointRepository;
//
//  @Autowired
//  RefPointRepository refPointRepository;
//
//  //@PostConstruct
//  public void synchronise() throws IntrospectionException, InvocationTargetException, IllegalAccessException {
//    log.info("synchronising points from timrefdb...");
//    List<Point> points = new ArrayList<>();
//
//    long start = System.currentTimeMillis();
//    for (RefPoint refPoint : refPointRepository.findAll()) {
//
//      Point point = new Point(refPoint.getPointId());
//      Map<String, Object> properties = new HashMap<>();
//
//      BeanInfo beanInfo = Introspector.getBeanInfo(RefPoint.class);
//      for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
//        String propertyName = descriptor.getName();
//        Object value = descriptor.getReadMethod().invoke(refPoint);
//
//        // Ignore the class property, as mongodb can't serialise it (and we don't need it anyway)
//        if (!propertyName.equals("class")) {
//          properties.put(propertyName, value);
//        }
//      }
//
//      point.setProperties(properties);
//
//      // Don't duplicate any more than we already are!
//      if (pointRepository.findOne(refPoint.getPointId()) == null) {
//        points.add(point);
//      }
//    }
//
//    pointRepository.save(points);
//
//    long end = System.currentTimeMillis();
//    long total = end - start;
//    log.info(format("synchronised %d points in %dms", points.size(), total));
//  }
//}
