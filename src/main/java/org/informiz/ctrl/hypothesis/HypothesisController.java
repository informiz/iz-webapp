package org.informiz.ctrl.hypothesis;

import org.informiz.ctrl.entity.ChaincodeEntityController;
import org.informiz.model.*;
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
                .orElse(null); //Throw(() ->new IllegalArgumentException("Invalid Hypothesis id"));
        if (hypothesis == null) return String.format("redirect:%s/all", PREFIX);

        model.addAttribute(HYPOTHESIS_ATTR, hypothesis);
        return String.format("%s/view-hypothesis.html", PREFIX);
    }

    // TODO: Remove references to deleted hypothesis
    @GetMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    public String getHypothesis(@PathVariable("id") @Valid Long id, Model model) {
        HypothesisBase hypothesis = entityRepo.findById(id)
                .orElse(null); //Throw(() ->new IllegalArgumentException("Invalid Hypothesis id"));
        if (hypothesis == null) return String.format("redirect:%s/all", PREFIX);

        prepareEditModel(model, hypothesis, new Review(), new Reference());
        return String.format("%s/update-hypothesis.html", PREFIX);
    }

    @PostMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    public String updateHypothesis(@PathVariable("id") @Valid Long id,
                                    @Valid @ModelAttribute(HYPOTHESIS_ATTR) HypothesisBase hypothesis,
                                    BindingResult result, Model model) {
        if (result.hasErrors()) {
            prepareEditModel(model, hypothesis, new Review(), new Reference());
            return String.format("%s/update-hypothesis.html", PREFIX);
        }

        HypothesisBase current = entityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid hypothesis id"));
        current.edit(hypothesis);
        entityRepo.save(current);
        return String.format("redirect:%s/details/%s", PREFIX, id);


    }

    @PostMapping("/review/{id}")
    @Secured("ROLE_CHECKER")
    @Transactional
    public String reviewHypothesis(@PathVariable("id") @Valid Long id,
                                   @Valid @ModelAttribute(REVIEW_ATTR) Review review,
                                   BindingResult result, Authentication authentication) {

        reviewEntity(id, review, authentication, result);
        return String.format("redirect:%s/details/%s", PREFIX, id);
    }


    @GetMapping("/review/{id}/del/{revId}")
    @Secured("ROLE_CHECKER")
    @Transactional
    public String unReviewHypothesis (@PathVariable("id") @Valid Long id,
                                  @PathVariable("revId") @Valid Long revId,
                                  Authentication authentication) {

        deleteReview(id, revId, authentication);
        return String.format("redirect:%s/details/%s", PREFIX, id);


    }


    @PostMapping("/reference/{id}")
    @Secured("ROLE_CHECKER")
    @Transactional
    public String addReference(@PathVariable("id") @Valid Long id,
                               @Valid @ModelAttribute(REFERENCE_ATTR) Reference reference,
                               BindingResult result,Authentication authentication, Model model) {

        return handleReference(id, reference, result, authentication, model);
    }

    @PostMapping("/reference/{id}/{refId}")
    @Secured("ROLE_CHECKER")
    @Transactional
    public String editReference(@PathVariable("id") @Valid Long id, @PathVariable("refId") @Valid Long refId,
                                @Valid @ModelAttribute(REFERENCE_ATTR) Reference reference,
                                BindingResult result, Authentication authentication, Model model) {

        return handleReference(id, reference, result, authentication, model);
    }

    @GetMapping("/reference/{id}/del/{refId}")
    @Secured("ROLE_CHECKER")
    @Transactional
    public String unReferenceHypothesis(@PathVariable("id") @Valid Long id,
                                     @PathVariable("refId") @Valid Long refId,
                                     Authentication authentication) {

        HypothesisBase current = entityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Claim id"));

        current.removeReference(refId);

        return String.format("redirect:%s/details/%s", PREFIX, id);
    }



    @PostMapping("/source/{id}")
    @Secured("ROLE_CHECKER")
    @Transactional
    public String addSource(@PathVariable("id") @Valid Long id, @ModelAttribute(SOURCE_ATTR) SourceRef source,
                            BindingResult result, Model model) {

        HypothesisBase current = entityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid claim id"));

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

    private String handleReference(Long id, Reference reference, BindingResult result,
                                   Authentication authentication, Model model) {

        HypothesisBase current = entityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid claim id"));

        if ( ! result.hasFieldErrors() ) {
            referenceEntity(current, reference, authentication);
        }
        // TODO: error handling in modal? Where will the error be visible?
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