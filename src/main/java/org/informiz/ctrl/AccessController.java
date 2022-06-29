package org.informiz.ctrl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AccessController {

    @RequestMapping(value = {"/", "/home", "/home.html"})
    public String index() {
        return "home.html";
    }

    @GetMapping(value = {"/policy"})
    public String policy() {
        return "policy.html";
    }

}
