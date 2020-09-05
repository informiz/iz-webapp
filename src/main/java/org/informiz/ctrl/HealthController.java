package org.informiz.ctrl;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = HealthController.PREFIX)
public class HealthController {

    public static final String PREFIX = "/health";

    @GetMapping(path = "/")
    public String checkHealth() {
        return "Healthy";
    }
}

