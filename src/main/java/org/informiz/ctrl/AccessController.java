package org.informiz.ctrl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AccessController {

    @Value("${spring.application.name}")
    String appName;


    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("appName", appName);
        return "home.html";
    }

    @RequestMapping("/home.html")
    public String homePage(Model model) {
        model.addAttribute("appName", appName);
        return "home.html";
    }
}
