package org.informiz.ctrl.reference;

import org.informiz.ctrl.entity.ChaincodeEntityController;
import org.informiz.model.ReferenceTextBase;
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
@RequestMapping(path= ReferenceController.PREFIX)
public class ReferenceController extends ChaincodeEntityController<ReferenceTextBase> {

    public static final String PREFIX = "/reference";
    public static final String REFERENCE_ATTR = "reference";
    public static final String REFERENCES_ATTR = "references";


    @GetMapping(path = {"/", "/all"})
    public String getAllReferences(Model model) {
        model.addAttribute(REFERENCES_ATTR, entityRepo.findAll());
        return String.format("%s/all-references.html", PREFIX);
    }

    @GetMapping("/add")
    @Secured("ROLE_MEMBER")
    public String addReferenceForm(Model model) {
        model.addAttribute(REFERENCE_ATTR, new ReferenceTextBase());
        return String.format("%s/add-reference.html", PREFIX);
    }

    @PostMapping("/add")
    @Secured("ROLE_MEMBER")
    public String addReference(@Valid @ModelAttribute(REFERENCE_ATTR) ReferenceTextBase reference,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return String.format("%s/add-reference.html", PREFIX);
        }
        // TODO: Add to ledger
        entityRepo.save(reference);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/delete/{id}")
    @Secured("ROLE_MEMBER")
    public String deleteReference(@PathVariable("id") @Valid Long id) {
        ReferenceTextBase reference = entityRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid reference id"));
        // TODO: set inactive
        entityRepo.delete(reference);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/view/{id}")
    public String viewReference(@PathVariable("id") @Valid Long id, Model model) {
        ReferenceTextBase reference = entityRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid reference id"));
        model.addAttribute(REFERENCE_ATTR, reference);
        return String.format("%s/view-reference.html", PREFIX);
    }

    @GetMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    public String getReference(@PathVariable("id")  Long id, Model model) {
        ReferenceTextBase reference = entityRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid reference id"));
        model.addAttribute(REFERENCE_ATTR, reference);
        model.addAttribute(REVIEW_ATTR, new Review());
        return String.format("%s/update-reference.html", PREFIX);
    }

    @PostMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    public String updateReference(@PathVariable("id") @Valid Long id,
                                    @Valid @ModelAttribute(REFERENCE_ATTR) ReferenceTextBase reference,
                                    BindingResult result, Model model) {
        if (! result.hasErrors()) {
            ReferenceTextBase current = entityRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid reference id"));
            current.edit(reference);
            entityRepo.save(current);
            model.addAttribute(REFERENCE_ATTR, current);
            return String.format("redirect:%s/all", PREFIX);
        }
        return String.format("%s/update-reference.html", PREFIX);
    }

    @PostMapping("/review/{id}")
    @Secured("ROLE_USER")
    @Transactional
    public String reviewReference(@PathVariable("id") @Valid Long id,
                                   @Valid @ModelAttribute(REVIEW_ATTR) Review review,
                                   BindingResult result, Authentication authentication, Model model) {

        ReferenceTextBase current = entityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid source id"));

        if ( ! result.hasFieldErrors("rating")) {
            current = reviewEntity(current, review, authentication);
            model.addAttribute(REFERENCE_ATTR, current);
            model.addAttribute(REVIEW_ATTR, new Review());
        }

        return String.format("redirect:%s/details/%s", PREFIX, current.getId());
    }
}