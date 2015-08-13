package cern.modesti.util;

import cern.modesti.repository.jpa.subsystem.SubSystem;
import cern.modesti.request.Request;
import cern.modesti.request.RequestType;
import cern.modesti.security.ldap.Role;
import cern.modesti.security.ldap.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by jsalmon on 13/08/15.
 */
public class TestUtil {

  public static Request getTestRequest() {
    Request request = new Request();
    request.setRequestId("1");
    request.setType(RequestType.CREATE);
    request.setCreator(new User(1, "bert", "Bert", "Is Evil", "bert@modesti.ch", new HashSet<>(Collections.singleton(new Role("modesti-administrators")))));
    request.setDescription("description");
    request.setDomain("TIM");
    request.setSubsystem(new SubSystem(1L, "EAU DEMI", "EAU", "A", "DEMI", "B"));
    request.setCategories(new ArrayList<>(Arrays.asList("PLC")));
    return request;
  }
}
