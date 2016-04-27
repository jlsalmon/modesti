package cern.modesti.user;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Justin Lewis Salmon
 */
@Service
@Profile("dev")
public class MockUserService implements UserService {

  private List<User> users = new ArrayList<>();

  ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());

  @PostConstruct
  public void init() throws IOException {
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
        for (String group : groups) {
          if (user.getAuthorities().contains(new Role(group))) {
            return true;
          }
        }
      }
      return false;
    }).collect(Collectors.toList());
  }

  @Override
  public User getCurrentUser() {
    return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
        List<Role> roles = Arrays.asList(props[4].split(",")).stream().map(Role::new).collect(Collectors.toList());

        User user = new User(users.size() + 1, props[0], props[1], props[2], props[3], roles);
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