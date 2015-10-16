package cern.modesti.upload;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.security.Principal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import cern.modesti.request.Request;

/**
 * @author Justin Lewis Salmon
 *
 */
@Controller
@Slf4j
public class UploadController {

  @Autowired
  private UploadService service;

  @RequestMapping(value = "/requests/upload", method = POST)
  public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("description") String description, UriComponentsBuilder b, Principal user) {
    Request request;

    if (!file.isEmpty()) {
      try {
        request = service.parseRequestFromExcelSheet(description, file.getInputStream(), user);

        log.info("successfully uploaded " + file.getOriginalFilename());
      } catch (Exception e) {
        log.info("failed to upload " + file.getOriginalFilename(), e);
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
      }
    } else {
      log.info("failed to upload " + file.getOriginalFilename() + " because the file was empty.");
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // Add link to newly created request in Location header
    UriComponents uriComponents = b.path("/requests/{id}").buildAndExpand(request.getRequestId());
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(uriComponents.toUri());

    return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
  }
}
