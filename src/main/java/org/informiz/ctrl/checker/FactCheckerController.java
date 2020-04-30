package org.informiz.ctrl.checker;

import org.informiz.model.FactCheckerBase;
import org.informiz.repo.CryptoUtils;
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

import static org.informiz.repo.CryptoUtils.ChaincodeProxy.PROXY_ATTR;

//@RestController
@Controller
@RequestMapping(path= FactCheckerController.prefix)
public class FactCheckerController {

    public static final String prefix = "/factchecker";
    @Autowired
    private FactCheckerRepository factCheckerRepo;
    // TODO: chaincode DAO


    @GetMapping("/add")
    public String addFactCheckerForm(Model model) {
        model.addAttribute("checker", new FactCheckerBase());
        return "add-fc.html";
    }

    @PostMapping("/add")
    public String addFactChecker(@Valid FactCheckerBase checker, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "add-fc.html";
        }

        factCheckerRepo.save(checker);
        return String.format("redirect:%s/all", prefix);
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") long id, Model model) {
        FactCheckerBase checker = factCheckerRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid fact-checker id"));
        factCheckerRepo.delete(checker);
        return String.format("redirect:%s/all", prefix);
    }

    @GetMapping("/details/{id}")
    public String getFactchecker(@PathVariable("id")  long id, Model model) {
        FactCheckerBase checker = factCheckerRepo.findById(id);
        if (checker == null) throw new IllegalArgumentException("Invalid fact-checker id");
        model.addAttribute("checker", checker);
        return "update-fc.html";
    }

    @PostMapping("/details/{id}")
    public String updateFactChecker(@Valid FactCheckerBase checker, BindingResult result, Model model) {
        CryptoUtils.ChaincodeProxy proxy = getCCProxy();
        if (! result.hasErrors()) {
            FactCheckerBase prev = factCheckerRepo.findById(checker.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid fact-checker id"));
            // Make sure immutable fields are not changed
            checker.setEntityId(prev.getEntityId());

/*
            proxy.submitTransaction("FactCheckerContract", "updateFactCheckerInfo",
                    new String[]{checker.getEntityId(), checker.toString()});
*/

            factCheckerRepo.save(checker);
        }
        return "update-fc.html";
    }

    private CryptoUtils.ChaincodeProxy getCCProxy() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession(true); // true == allow create
        // TODO: re-create from encrypted blob?
        return (CryptoUtils.ChaincodeProxy) session.getAttribute(PROXY_ATTR);
    }

    @GetMapping(path = {"/", "/all"})
    public String getAllFactCheckers(Model model) {
        model.addAttribute("checkers", factCheckerRepo.findAll());
        return "all-fc.html";
    }
}