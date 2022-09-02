package org.informiz.ctrl.source;

import org.informiz.ctrl.entity.ChaincodeEntityController;
import org.informiz.model.Review;
import org.informiz.model.SourceBase;
import org.informiz.model.SourceRef;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;

@Controller
@RequestMapping(path = SourceController.PREFIX)
public class SourceController extends ChaincodeEntityController<SourceBase> {

    public static final String PREFIX = "/source";
    public static final String SOURCE_ATTR = "source";
    public static final String SOURCES_ATTR = "sources";


    @GetMapping(path = {"/", "/all"})
    public String getAllSources(Model model) {
        model.addAttribute(SOURCES_ATTR, entityRepo.findAll());
        return String.format("%s/all-src.html", PREFIX);
    }

    @GetMapping("/add")
    @Secured("ROLE_MEMBER")
    public String addSourceForm(Model model) {
        model.addAttribute(SOURCE_ATTR, new SourceBase());
        return String.format("%s/add-src.html", PREFIX);
    }

    @PostMapping("/add")
    @Secured("ROLE_MEMBER")
    public String addSource(@Valid @ModelAttribute(SOURCE_ATTR) SourceBase source,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return String.format("%s/add-src.html", PREFIX);
        }
        // TODO: Add to ledger
        entityRepo.save(source);
        return String.format("redirect:%s/all", PREFIX);
    }

    @PostMapping("/delete/{id}")
    @Secured("ROLE_MEMBER")
    @PreAuthorize("#ownerId == authentication.principal.name")
    public String deleteSource(@PathVariable("id") @Valid Long id, @RequestParam String ownerId) {
        SourceBase source = entityRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid source id"));
        // TODO: set inactive
        entityRepo.delete(source);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/view/{id}")
    @Secured("ROLE_MEMBER")
    public String viewSource(@PathVariable("id") @Valid Long id, Model model) {
        SourceBase source = entityRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid Source id"));
        model.addAttribute(SOURCE_ATTR, source);
        return String.format("%s/view-src.html", PREFIX);
    }

    @GetMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    public String getSource(@PathVariable("id") @Valid Long id, Model model) {
        SourceBase source = entityRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid Source id"));
        model.addAttribute(SOURCE_ATTR, source);
        model.addAttribute(REVIEW_ATTR, new Review());
        return String.format("%s/update-src.html", PREFIX);
    }

    @PostMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    @PreAuthorize("#source.ownerId == authentication.principal.name")
    public String updateSource(@PathVariable("id") @Valid Long id,
                                    @Valid @ModelAttribute(SOURCE_ATTR) SourceBase source,
                                    BindingResult result, Model model) {
        if (! result.hasErrors()) {
            SourceBase current = entityRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid source id"));
            current.edit(source);
            entityRepo.save(current);
            model.addAttribute(SOURCE_ATTR, current);
            return String.format("redirect:%s/details/%s", PREFIX, current.getId());
        }
        return String.format("%s/update-src.html", PREFIX);
    }

    @PostMapping("/{id}/review/")
    @Secured("ROLE_CHECKER")
    @Transactional
    public String reviewSource(@PathVariable("id") @Valid Long id,
                                  @Valid @ModelAttribute(REVIEW_ATTR) Review review,
                                  BindingResult result, Authentication authentication) {
        reviewEntity(id, review, authentication, result);
        return String.format("redirect:%s/details/%s", PREFIX, id);
    }

    @PostMapping("/{id}/review/edit/")
    @Secured("ROLE_CHECKER")
    @org.springframework.transaction.annotation.Transactional
    @PreAuthorize("#review.ownerId == authentication.principal.name")
    public String editReview(@PathVariable("id") @Valid Long id,
                             @Valid @ModelAttribute(REVIEW_ATTR) Review review,
                             BindingResult result, Authentication authentication) {
        reviewEntity(id, review, authentication, result);
        return String.format("redirect:%s/details/%s", PREFIX, id);
    }

    @PostMapping("/{id}/review/del/")
    @Secured("ROLE_CHECKER")
    @org.springframework.transaction.annotation.Transactional
    @PreAuthorize("#review.ownerId == authentication.principal.name")
    public String deleteReview (@PathVariable("id") @Valid Long id,
                               @ModelAttribute(REVIEW_ATTR) Review review,
                               Authentication authentication) {

        deleteReview(id, review.getId(), authentication);
        return String.format("redirect:%s/details/%s", PREFIX, id);
    }

}
