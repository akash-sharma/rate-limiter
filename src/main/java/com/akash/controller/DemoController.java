package com.akash.controller;

import com.akash.constant.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

  private static final Logger LOGGER = LogManager.getLogger(DemoController.class);

  @GetMapping(path = Route.V1_PRODUCT, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> v1Product() {

    return new ResponseEntity("v1 product response", HttpStatus.OK);
  }

  @GetMapping(path = Route.V2_PRODUCT, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> v2Product() {

    return new ResponseEntity("v2 product response", HttpStatus.OK);
  }

  @GetMapping(path = Route.V3_PRODUCT, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> v3Product() {

    return new ResponseEntity("v3 product response", HttpStatus.OK);
  }
}
