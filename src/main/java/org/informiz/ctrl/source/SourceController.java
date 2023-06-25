package org.informiz.ctrl.source;

import jakarta.validation.Valid;
import org.informiz.ctrl.entity.ChaincodeEntityController;
import org.informiz.model.*;
import org.informiz.repo.source.SourceRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping(path = SourceController.PREFIX)
@Validated
public class SourceController extends ChaincodeEntityController<SourceBase> {

    public static final String PREFIX = "/source";
    public static final String SOURCE_ATTR = "source";
    public static final String SOURCES_ATTR = "sources";

    @Autowired
    public SourceController(SourceRepository repository) {
        super(repository);
    }

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
    public String addSource(@Validated(SourceBase.SourceFromUI.class) @ModelAttribute(SOURCE_ATTR) SourceBase source,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return String.format("%s/add-src.html", PREFIX);
        }
        // TODO: Add to ledger
        entityRepo.save(source);
        return String.format("redirect:%s/all", PREFIX);
    }

    @PostMapping("/delete/{sourceId}")
    @Secured("ROLE_MEMBER")
    @PreAuthorize("#ownerId == authentication.principal.name")
    public String deleteSource(@PathVariable("sourceId") @Valid Long id, @RequestParam String ownerId) {
        SourceBase source = entityRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid source id"));
        // TODO: set inactive
        entityRepo.delete(source);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/view/{sourceId}")
    @Secured("ROLE_MEMBER")
    public String viewSource(@PathVariable("sourceId") @Valid Long id, Model model) {
        SourceBase source = entityRepo.loadByLocalId(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid Source id"));
        model.addAttribute(SOURCE_ATTR, source);
        return String.format("%s/view-src.html", PREFIX);
    }

    @GetMapping("/details/{sourceId}")
    @Secured("ROLE_MEMBER")
    public String getSource(@PathVariable("sourceId") @Valid Long id, Model model) {
        SourceBase source = entityRepo.loadByLocalId(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid Source id"));
        model.addAttribute(SOURCE_ATTR, source);
        model.addAttribute(REVIEW_ATTR, new Review());
        return getEditPageTemplate();
    }

    @PostMapping("/details/{sourceId}")
    @Secured("ROLE_MEMBER")
    @PreAuthorize("#source.ownerId == authentication.principal.name")
    public String updateSource(@PathVariable("sourceId") @Valid Long id,
                               @Validated(SourceBase.SourceFromUI.class) @ModelAttribute(SOURCE_ATTR) SourceBase source,
                               BindingResult result, Model model) {
        if (! result.hasErrors()) {
            SourceBase current = entityRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid source id"));
            current.edit(source);
            entityRepo.save(current);
            model.addAttribute(SOURCE_ATTR, current);
            return getRedirectToEditPage(current.getLocalId());
        }
        return getEditPageTemplate();
    }

    @PostMapping("/{sourceId}/review/")
    @Secured("ROLE_CHECKER")
    public String reviewSource(@PathVariable("sourceId") @Valid Long id,
                               @Validated(Review.UserReview.class) @ModelAttribute(REVIEW_ATTR) Review review,
                               BindingResult result, Model model, Authentication authentication) {
        return reviewEntity(id, review, result, model, authentication);
    }

    @PostMapping("/{sourceId}/review/edit/")
    @Secured("ROLE_CHECKER")
    @PreAuthorize("#review.ownerId == authentication.principal.name")
    public String editReview(@PathVariable("sourceId") @Valid Long id,
                             @Validated(Review.UserReview.class) @ModelAttribute(REVIEW_ATTR) Review review,
                             BindingResult result, Model model, Authentication authentication) {
        return reviewEntity(id, review, result, model, authentication);
    }

    @PostMapping("/{sourceId}/review/del/")
    @Secured("ROLE_CHECKER")
    @PreAuthorize("#review.ownerId == authentication.principal.name")
    public String deleteReview (@PathVariable("sourceId") @Valid Long id,
                                @Validated(InformizEntity.DeleteEntity.class) @ModelAttribute(REVIEW_ATTR) Review review,
                                BindingResult result, Model model, Authentication authentication) {
        return deleteReview(id, review.getId(), result, model, authentication);
    }

    protected void modelForReviewError(@NotNull Model model, SourceBase current) {
        model.addAttribute(SOURCE_ATTR, current);
        model.addAttribute(REVIEW_ATTR, new Review());
    }

    protected String getEditPageTemplate() { return String.format("%s/update-src.html", PREFIX); }
    protected String getRedirectToEditPage(Long id) { return String.format("redirect:%s/details/%s", PREFIX, id); }
}
