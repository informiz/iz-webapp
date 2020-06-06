package org.informiz.ctrl.checker;

import org.informiz.model.FactCheckerBase;
import org.informiz.repo.FactCheckerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

//@RestController
@Controller
@RequestMapping(path= FactCheckerController.PREFIX)
public class FactCheckerController {

    public static final String PREFIX = "/factchecker";
    public static final String CHECKER_ATTR = "checker";
    public static final String CHECKERS_ATTR = "checkers";
    @Autowired
    private FactCheckerRepository factCheckerRepo;
    // TODO: chaincode DAO


    @GetMapping(path = {"/", "/all"})
    public String getAllFactCheckers(Model model) {
        model.addAttribute(CHECKERS_ATTR, factCheckerRepo.findAll());
        return "all-fc.html";
    }

    @GetMapping("/add")
    public String addFactCheckerForm(Model model) {
        model.addAttribute(CHECKER_ATTR, new FactCheckerBase());
        return "add-fc.html";
    }

    @PostMapping("/add")
    public String addFactChecker(@Valid @ModelAttribute(CHECKER_ATTR) FactCheckerBase checker,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return "add-fc.html";
        }
        // TODO: TESTING, REVERT THIS!!!
        // checker = CheckerCCDao.addFactChecker(getSession(), checker);
        factCheckerRepo.save(checker);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") long id) {
        FactCheckerBase checker = factCheckerRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid fact-checker id"));
        // TODO: set inactive
        factCheckerRepo.delete(checker);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/details/{id}")
    public String getFactchecker(@PathVariable("id")  Long id, Model model) {
        FactCheckerBase checker = factCheckerRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid fact-checker id"));
        model.addAttribute(CHECKER_ATTR, checker);
        return "update-fc.html";
    }

    @PostMapping("/details/{id}")
    public String updateFactChecker(@PathVariable("id")  Long id,
                                    @Valid @ModelAttribute(CHECKER_ATTR) FactCheckerBase checker,
                                    BindingResult result, Model model) {
        if (! result.hasErrors()) {
            FactCheckerBase current = factCheckerRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid fact-checker id"));
            current.edit(checker);
            factCheckerRepo.save(current);
            model.addAttribute(CHECKER_ATTR, current);
        }
        return "update-fc.html";
    }

    private HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true); // true == allow create
    }
}