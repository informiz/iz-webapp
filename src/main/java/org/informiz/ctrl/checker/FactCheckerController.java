package org.informiz.ctrl.checker;

import org.informiz.ctrl.entity.ChaincodeEntityController;
import org.informiz.model.FactCheckerBase;
import org.informiz.repo.checker.FactCheckerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(path= FactCheckerController.PREFIX)
@Validated
public class FactCheckerController  extends ChaincodeEntityController<FactCheckerBase> {

    public static final String PREFIX = "/factchecker";
    public static final String CHECKER_ATTR = "checker";
    public static final String CHECKERS_ATTR = "checkers";

    // TODO: chaincode DAO

    @Autowired
    public FactCheckerController(FactCheckerRepository repository) {
        super(repository);
    }

    @GetMapping(path = {"/", "/all"})
    public String getAllFactCheckers(Model model) {
        Iterable<FactCheckerBase> checkers = entityRepo.findAll();
        model.addAttribute(CHECKERS_ATTR, checkers);
        return String.format("%s/all-fc.html", PREFIX);
    }

    @GetMapping("/add")
    @Secured("ROLE_ADMIN")
    public String addFactCheckerForm(Model model) {
        model.addAttribute(CHECKER_ATTR, new FactCheckerBase());
        return String.format("%s/add-fc.html", PREFIX);
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String addFactChecker(@Validated(FactCheckerBase.FactCheckerFromUI.class) @ModelAttribute(CHECKER_ATTR) FactCheckerBase checker,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return String.format("%s/add-fc.html", PREFIX);
        }
        // checker = CheckerCCDao.addFactChecker(getSession(), checker);
        entityRepo.save(checker);
        return getRedirectToEditPage(null);
    }

    @GetMapping("/delete/{id}")
    @Secured("ROLE_ADMIN")
    public String deleteUser(@PathVariable("id") long id) {
        FactCheckerBase checker = entityRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid fact-checker id"));
        // TODO: set inactive
        entityRepo.delete(checker);
        return getRedirectToEditPage(null);
    }

    @GetMapping("/view/{id}")
    public String viewFactchecker(@PathVariable("id")  Long id, Model model) {
        FactCheckerBase checker = entityRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid fact-checker id"));
        model.addAttribute(CHECKER_ATTR, checker);
        return String.format("%s/view-fc.html", PREFIX);
    }

    @GetMapping("/details/{id}")
    @Secured("ROLE_ADMIN")
    public String getFactchecker(@PathVariable("id")  Long id, Model model) {
        FactCheckerBase checker = entityRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid fact-checker id"));
        model.addAttribute(CHECKER_ATTR, checker);
        return getEditPageTemplate();
    }

    @PostMapping("/details/{id}")
    @Secured("ROLE_ADMIN")
    public String updateFactChecker(@PathVariable("id")  Long id,
                                    @Validated(FactCheckerBase.FactCheckerFromUI.class) @ModelAttribute(CHECKER_ATTR) FactCheckerBase checker,
                                    BindingResult result, Model model) {
        if (! result.hasErrors()) {
            FactCheckerBase current = entityRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid fact-checker id"));
            current.edit(checker);
            entityRepo.save(current);
            model.addAttribute(CHECKER_ATTR, current);
            return getRedirectToEditPage(null);
        }
        return failedEdit(model, result, checker, checker);
    }

    @Override
    protected String getEditPageTemplate() { return String.format("%s/update-fc.html", PREFIX); }

    @Override
    protected String getRedirectToEditPage(Long id) { return String.format("redirect:%s/all", PREFIX); }

    @Override
    protected void modelForError(Model model, FactCheckerBase current) {
        super.modelForError(model, current);
        model.addAttribute(CHECKER_ATTR, current);
    }
}