package org.informiz.ctrl.hypothesis;

import org.informiz.model.HypothesisBase;
import org.informiz.repo.hypothesis.HypothesisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping(path= HypothesisController.PREFIX)
public class HypothesisController {

    public static final String PREFIX = "/hypothesis";
    public static final String HYPOTHESIS_ATTR = "hypothesis"; // singular
    public static final String HYPOTHESES_ATTR = "hypotheses"; // plural
    @Autowired
    private HypothesisRepository hypothesisRepo;
    // TODO: chaincode DAO


    @GetMapping(path = {"/", "/all"})
    public String getAllHypotheses(Model model) {
        model.addAttribute(HYPOTHESES_ATTR, hypothesisRepo.findAll());
        return String.format("%s/all-hypotheses.html", PREFIX);
    }

    @GetMapping("/add")
    @Secured("ROLE_MEMBER")
    public String addHypothesisForm(Model model) {
        model.addAttribute(HYPOTHESIS_ATTR, new HypothesisBase());
        return String.format("%s/add-hypothesis.html", PREFIX);
    }

    @PostMapping("/add")
    @Secured("ROLE_MEMBER")
    public String addHypothesis(@Valid @ModelAttribute(HYPOTHESIS_ATTR) HypothesisBase hypothesis,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return "hypothesis/add-hypothesis.html";
        }
        // TODO: Add to ledger
        hypothesisRepo.save(hypothesis);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/delete/{id}")
    public String deleteHypothesis(@PathVariable("id") long id) {
        HypothesisBase hypothesis = hypothesisRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid hypothesis id"));
        // TODO: set inactive
        hypothesisRepo.delete(hypothesis);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/view/{id}")
    public String viewHypothesis(@PathVariable("id")  Long id, Model model) {
        HypothesisBase hypothesis = hypothesisRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid Hypothesis id"));
        model.addAttribute(HYPOTHESIS_ATTR, hypothesis);
        return String.format("%s/view-hypothesis.html", PREFIX);
    }

    @GetMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    public String getHypothesis(@PathVariable("id")  Long id, Model model) {
        HypothesisBase hypothesis = hypothesisRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid Hypothesis id"));
        model.addAttribute(HYPOTHESIS_ATTR, hypothesis);
        return String.format("%s/update-hypothesis.html", PREFIX);
    }

    @PostMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    public String updateHypothesis(@PathVariable("id")  Long id,
                                    @Valid @ModelAttribute(HYPOTHESIS_ATTR) HypothesisBase hypothesis,
                                    BindingResult result, Model model) {
        if (! result.hasErrors()) {
            HypothesisBase current = hypothesisRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid hypothesis id"));
            current.edit(hypothesis);
            hypothesisRepo.save(current);
            model.addAttribute(HYPOTHESIS_ATTR, current);
            return String.format("redirect:%s/all", PREFIX);
        }
        return String.format("%s/update-hypothesis.html", PREFIX);
    }
}