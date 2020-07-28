package org.informiz.ctrl.hypothesis;

import org.informiz.ctrl.entity.ChaincodeEntityController;
import org.informiz.model.HypothesisBase;
import org.informiz.model.Review;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping(path= HypothesisController.PREFIX)
public class HypothesisController extends ChaincodeEntityController<HypothesisBase> {

    public static final String PREFIX = "/hypothesis";
    public static final String HYPOTHESIS_ATTR = "hypothesis"; // singular
    public static final String HYPOTHESES_ATTR = "hypotheses"; // plural


    @GetMapping(path = {"/", "/all"})
    public String getAllHypotheses(Model model) {
        model.addAttribute(HYPOTHESES_ATTR, entityRepo.findAll());
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
        entityRepo.save(hypothesis);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/delete/{id}")
    @Secured("ROLE_MEMBER")
    public String deleteHypothesis(@PathVariable("id") @Valid Long id) {
        HypothesisBase hypothesis = entityRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid hypothesis id"));
        // TODO: set inactive
        entityRepo.delete(hypothesis);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/view/{id}")
    public String viewHypothesis(@PathVariable("id") @Valid Long id, Model model) {
        HypothesisBase hypothesis = entityRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid Hypothesis id"));
        model.addAttribute(HYPOTHESIS_ATTR, hypothesis);
        return String.format("%s/view-hypothesis.html", PREFIX);
    }

    @GetMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    public String getHypothesis(@PathVariable("id") @Valid Long id, Model model) {
        HypothesisBase hypothesis = entityRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid Hypothesis id"));
        model.addAttribute(HYPOTHESIS_ATTR, hypothesis);
        model.addAttribute(REVIEW_ATTR, new Review());
        return String.format("%s/update-hypothesis.html", PREFIX);
    }

    @PostMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    public String updateHypothesis(@PathVariable("id") @Valid Long id,
                                    @Valid @ModelAttribute(HYPOTHESIS_ATTR) HypothesisBase hypothesis,
                                    BindingResult result, Model model) {
        if (! result.hasErrors()) {
            HypothesisBase current = entityRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid hypothesis id"));
            current.edit(hypothesis);
            entityRepo.save(current);
            model.addAttribute(HYPOTHESIS_ATTR, current);
            model.addAttribute(REVIEW_ATTR, new Review());
            return String.format("redirect:%s/all", PREFIX);
        }
        return String.format("%s/update-hypothesis.html", PREFIX);
    }

    @PostMapping("/review/{id}")
    @Secured("ROLE_USER")
    @Transactional
    public String reviewHypothesis(@PathVariable("id") @Valid Long id,
                                   @Valid @ModelAttribute(REVIEW_ATTR) Review review,
                                   BindingResult result, Authentication authentication, Model model) {

        HypothesisBase current = entityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid source id"));

        if ( ! result.hasFieldErrors("rating")) {
            current = reviewEntity(current, review, authentication);
            model.addAttribute(HYPOTHESIS_ATTR, current);
            model.addAttribute(REVIEW_ATTR, new Review());
        }

        return String.format("redirect:%s/details/%s", PREFIX, current.getId());
    }
}