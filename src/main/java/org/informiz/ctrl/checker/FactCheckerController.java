package org.informiz.ctrl.checker;

import org.informiz.model.FactCheckerBase;
import org.informiz.repo.checker.FactCheckerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
        return String.format("%s/all-fc.html", PREFIX);
    }

    @GetMapping("/add")
    @Secured("ROLE_ADMIN")
    public String addFactCheckerForm(Model model) {
        model.addAttribute(CHECKER_ATTR, new FactCheckerBase());
        return String.format("%s/add-fc.html", PREFIX);
    }

    @PostMapping("/add")
    @Secured("ROLE_ADMIN")
    public String addFactChecker(@Valid @ModelAttribute(CHECKER_ATTR) FactCheckerBase checker,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return String.format("%s/add-fc.html", PREFIX);
        }
        // TODO: TESTING, REVERT THIS!!!
        // checker = CheckerCCDao.addFactChecker(getSession(), checker);
        factCheckerRepo.save(checker);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/delete/{id}")
    @Secured("ROLE_ADMIN")
    public String deleteUser(@PathVariable("id") long id) {
        FactCheckerBase checker = factCheckerRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid fact-checker id"));
        // TODO: set inactive
        factCheckerRepo.delete(checker);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/view/{id}")
    public String viewFactchecker(@PathVariable("id")  Long id, Model model) {
        FactCheckerBase checker = factCheckerRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid fact-checker id"));
        model.addAttribute(CHECKER_ATTR, checker);
        return String.format("%s/view-fc.html", PREFIX);
    }

    @GetMapping("/details/{id}")
    @Secured("ROLE_ADMIN")
    public String getFactchecker(@PathVariable("id")  Long id, Model model) {
        FactCheckerBase checker = factCheckerRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid fact-checker id"));
        model.addAttribute(CHECKER_ATTR, checker);
        return String.format("%s/update-fc.html", PREFIX);
    }

    @PostMapping("/details/{id}")
    @Secured("ROLE_ADMIN")
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
        return String.format("%s/update-fc.html", PREFIX);
    }
}