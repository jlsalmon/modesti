package cern.modesti.security.mock;

import cern.modesti.user.User;
import cern.modesti.security.UserService;
import cern.modesti.user.UserImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link UserService} implementation for development purposes that loads a
 * list of mock users and groups from {@literal mock-users.txt} files on the
 * classpath.
 *
 * @author Justin Lewis Salmon
 */
@Slf4j
public class MockUserServiceImpl implements MockUserService, UserService {

  private List<User> users = new ArrayList<>();

  ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());

  public MockUserServiceImpl() throws IOException {
    loadMockUsers();
  }

  @Override
  public User findOneByUsername(String username) {
    return users.stream().filter(user -> user.getUsername().equals(username)).findFirst().orElse(null);
  }

  @Override
  public List<User> findByUsernameStartsWith(String query) {
    return users.stream().filter(user -> user.getUsername().startsWith(query)).collect(Collectors.toList());
  }

  @Override
  public List<User> findByNameAndGroup(String query, List<String> groups) {
    if (query == null) {
      return new ArrayList<>();
    }

    return users.stream().filter(user -> {
      if (user.getUsername().startsWith(query)) {
        if (groups.isEmpty()) {
          return true;
        }
        for (String group : groups) {
          if (user.getAuthorities().contains(new SimpleGrantedAuthority(group))) {
            return true;
          }
        }
      }
      return false;
    }).collect(Collectors.toList());
  }

  @Override
  public List<String> findGroupsByName(String query) {
    List<String> groups = new ArrayList<>();

    for (User user : users) {
      user.getAuthorities().forEach(authority -> groups.add(authority.getAuthority()));
    }

    return groups;
  }

  @Override
  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      log.warn("Current user is null!");
      return null;
    }
    return (User) authentication.getPrincipal();
  }

  public void addMockUser(User user) {
    users.add(user);
  }

  public void login(String username) {
    User user = findOneByUsername(username);
    Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private void loadMockUsers() throws IOException {
    for (Resource resource : resolver.getResources("classpath*:mock-users.txt")) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
      String line;

      while ((line = reader.readLine()) != null) {
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }

        String[] props = line.split(" ");
        List<SimpleGrantedAuthority> roles = Arrays.stream(props[4].split(",")).map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        User user = new UserImpl(users.size() + 1, props[0], props[1], props[2], props[3], roles);
        boolean store = true;

        for (User existingUser : users) {
          if (existingUser.getUsername().equals(user.getUsername())) {
            store = false;
          }
        }

        if (store) {
          addMockUser(user);
        }
      }
    }
  }
}
