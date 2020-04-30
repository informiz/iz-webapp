package org.informiz.ctrl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AccessController {

    @Value("${spring.application.name}")
    String appName;


    // Login form
    @RequestMapping(value = "/login.html", method = RequestMethod.GET)
    public String login() {
        return "login.html";
    }


    // Login form with error
    @RequestMapping("/login-error.html")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "login.html";
    }

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
