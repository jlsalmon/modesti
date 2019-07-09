package cern.modesti.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TnAddressController {
  private static final String TN_ADDRESS = "172.18.0.0/16";
  private static final IpAddressMatcher TN_ADDRESS_MATCHER = new IpAddressMatcher(TN_ADDRESS);
  
  @GetMapping(value="/api/is_tn_address")
  public boolean isTnAddress(HttpServletRequest request) {
    return TN_ADDRESS_MATCHER.matches(request);
  }
}
