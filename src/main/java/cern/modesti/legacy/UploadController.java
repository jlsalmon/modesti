/**
 *
 */
package cern.modesti.legacy;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
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
public class UploadController {

  private static final Logger LOG = LoggerFactory.getLogger(UploadController.class);

  @Autowired
  private EntityLinks entityLinks;

  @Autowired
  private UploadService service;

  @RequestMapping(value = "/requests/upload", method = POST)
  public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file, UriComponentsBuilder b) {
    Request request = null;

    if (!file.isEmpty()) {
      try {
        request = service.parseRequestFromExcelSheet(file.getOriginalFilename(), file.getInputStream());

        LOG.info("successfully uploaded " + file.getOriginalFilename());
      } catch (Exception e) {
        LOG.info("failed to upload " + file.getOriginalFilename(), e);
      }
    } else {
      LOG.info("failed to upload " + file.getOriginalFilename() + " because the file was empty.");
    }

    // Add link to newly created request in Location header
    UriComponents uriComponents = b.path("/requests/{id}").buildAndExpand(request.getRequestId());

    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(uriComponents.toUri());
    return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
  }

}
