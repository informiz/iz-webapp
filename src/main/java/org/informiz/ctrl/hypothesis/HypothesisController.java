package org.informiz.ctrl.hypothesis;

import org.informiz.ctrl.entity.ChaincodeEntityController;
import org.informiz.model.HypothesisBase;
import org.informiz.model.Reference;
import org.informiz.model.Review;
import org.informiz.model.SourceRef;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping(path= HypothesisController.PREFIX)
public class HypothesisController extends ChaincodeEntityController<HypothesisBase> {

    public static final String PREFIX = "/hypothesis";
    public static final String HYPOTHESIS_ATTR = "hypothesis"; // singular
    public static final String HYPOTHESES_ATTR = "hypotheses"; // plural
    public static final String REFERENCE_ATTR = "reference";
    public static final String SOURCE_ATTR = "source";


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
        prepareEditModel(model, hypothesis, new Review(), new Reference());
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
//            model.addAttribute(HYPOTHESIS_ATTR, current);
        }
        prepareEditModel(model, hypothesis, new Review(), new Reference());
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
//            model.addAttribute(HYPOTHESIS_ATTR, current);
        }
        prepareEditModel(model, current, review, new Reference());
        return String.format("%s/update-hypothesis.html", PREFIX);
    }

    @PostMapping("/reference/{id}")
    @Secured("ROLE_USER")
    @Transactional
    public String addReference(@PathVariable("id") @Valid Long id,
                               @Valid @ModelAttribute(REFERENCE_ATTR) Reference reference,
                               BindingResult result, Model model) {

        HypothesisBase current = entityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid source id"));

        if ( ! (result.hasFieldErrors("citationId") || result.hasFieldErrors("entailment"))) {
            current = addReference(current, reference);
//            model.addAttribute(HYPOTHESIS_ATTR, current);
        }
        prepareEditModel(model, current, new Review(), reference);
        return String.format("%s/update-hypothesis.html", PREFIX);
    }

    protected HypothesisBase addReference(HypothesisBase hypothesis, Reference reference) {
        reference.setReviewed(hypothesis);
        Reference current = hypothesis.getReference(reference);
        if (current != null) {
            current.setEntailment(reference.getEntailment());
            current.setComment(reference.getComment());
        } else {
            hypothesis.addReference(new Reference(hypothesis, reference.getCitationId(),
                    reference.getEntailment(), reference.getComment()));
        }
        return hypothesis;
    }

    @PostMapping("/source/{id}")
    @Secured("ROLE_USER")
    @Transactional
    public String addSource(@PathVariable("id") @Valid Long id, @ModelAttribute(SOURCE_ATTR) SourceRef source,
                            BindingResult result, Model model) {

        HypothesisBase current = entityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Citation id"));

        try {
            SourceRef toAdd = new SourceRef(source.getSrcEntityId(), current, source.getLink(), source.getDescription());
            current.addSource(toAdd);
            model.addAttribute(HYPOTHESIS_ATTR, current);
            model.addAttribute(SOURCE_ATTR, new SourceRef());
        } catch (IllegalArgumentException e) {
            result.addError(new ObjectError("link",
                    "Please provide either a link, a source or both"));
        }
        prepareEditModel(model, current, new Review(), new Reference());
        return String.format("%s/update-hypothesis.html", PREFIX);
    }

    private void prepareEditModel(Model model, HypothesisBase hypothesis, Review review, Reference ref) {
        model.addAttribute(HYPOTHESIS_ATTR, hypothesis);
        model.addAttribute(REVIEW_ATTR, review);
        model.addAttribute(REFERENCE_ATTR, ref);
        model.addAttribute(SOURCE_ATTR, new SourceRef());
    }
}