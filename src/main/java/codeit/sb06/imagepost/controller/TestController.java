package codeit.sb06.imagepost.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test/session")
    public String testInvalidSession() {
        return "Valid session";
    }
}
