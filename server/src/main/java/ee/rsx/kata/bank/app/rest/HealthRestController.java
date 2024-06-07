package ee.rsx.kata.bank.app.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthRestController {

  @GetMapping(value = "/health")
  public HealthResponse health(){
    return new HealthResponse("OK");
  }
}

