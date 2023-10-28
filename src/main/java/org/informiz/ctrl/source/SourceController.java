package org.informiz.ctrl.source;

import jakarta.validation.Valid;
import org.informiz.ctrl.entity.ChaincodeEntityController;
import org.informiz.model.InformizEntity;
import org.informiz.model.Review;
import org.informiz.model.SourceBase;
import org.informiz.repo.source.SourceRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
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
    @PreAuthorize("hasAuthority('ROLE_MEMBER')")
    public String addSourceForm(Model model) {
        model.addAttribute(SOURCE_ATTR, new SourceBase());
        return String.format("%s/add-src.html", PREFIX);
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_MEMBER')")
    public String addSource(@Validated(SourceBase.ExistingSourceFromUI.class) @ModelAttribute(SOURCE_ATTR) SourceBase source,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return String.format("%s/add-src.html", PREFIX);
        }
        // TODO: Add to ledger
        entityRepo.save(source);
        return String.format("redirect:%s/all", PREFIX);
    }

    @PostMapping("/delete/{sourceId}")
    @PreAuthorize("hasAuthority('ROLE_MEMBER') and #ownerId == authentication.principal.name")
    public String deleteSource(@PathVariable("sourceId") @Valid Long id, @RequestParam String ownerId) {
        SourceBase source = entityRepo.loadByLocalId(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid source id"));
        // TODO: set inactive
        entityRepo.delete(source);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/view/{sourceId}")
    @PreAuthorize("hasAuthority('ROLE_VIEWER')")
    public String viewSource(@PathVariable("sourceId") @Valid Long id, Model model) {
        SourceBase source = entityRepo.loadByLocalId(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid Source id"));
        model.addAttribute(SOURCE_ATTR, source);
        return String.format("%s/view-src.html", PREFIX);
    }

    @GetMapping("/details/{sourceId}")
    @PreAuthorize("hasAuthority('ROLE_CHECKER')")
    public String getSource(@PathVariable("sourceId") @Valid Long id, Model model) {
        SourceBase source = entityRepo.loadByLocalId(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid Source id"));
        model.addAttribute(SOURCE_ATTR, source);
        model.addAttribute(REVIEW_ATTR, new Review());
        return getEditPageTemplate();
    }

    @PostMapping("/details/{sourceId}")
    @PreAuthorize("hasAuthority('ROLE_MEMBER') and #source.ownerId == authentication.principal.name")
    public String updateSource(@PathVariable("sourceId") @Valid Long id,
                               @Validated(SourceBase.ExistingSourceFromUI.class) @ModelAttribute(SOURCE_ATTR) SourceBase source,
                               BindingResult result, Model model) {
        if (! result.hasErrors()) {
            SourceBase current = entityRepo.loadByLocalId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid source id"));
            current.edit(source);
            entityRepo.save(current);
            model.addAttribute(SOURCE_ATTR, current);
            return getRedirectToEditPage(current.getLocalId());
        }
        return failedEdit(model, result, source, source);
    }

    @PostMapping("/{sourceId}/review/")
    @PreAuthorize("hasAuthority('ROLE_CHECKER')")
    public String reviewSource(@PathVariable("sourceId") @Valid Long id,
                               @Validated(Review.ExistingUserReview.class) @ModelAttribute(REVIEW_ATTR) Review review,
                               BindingResult result, Model model, Authentication authentication) {
        return reviewEntity(id, review, result, model, authentication);
    }

    @PostMapping("/{sourceId}/review/edit/")
    @PreAuthorize("hasAuthority('ROLE_CHECKER') and #review.ownerId == authentication.principal.name")
    public String editReview(@PathVariable("sourceId") @Valid Long id,
                             @Validated(Review.ExistingUserReview.class) @ModelAttribute(REVIEW_ATTR) Review review,
                             BindingResult result, Model model, Authentication authentication) {
        return reviewEntity(id, review, result, model, authentication);
    }

    @PostMapping("/{sourceId}/review/del/")
    @PreAuthorize("hasAuthority('ROLE_CHECKER') and #review.ownerId == authentication.principal.name")
    public String deleteReview (@PathVariable("sourceId") @Valid Long id,
                                @Validated(InformizEntity.DeleteEntity.class) @ModelAttribute(REVIEW_ATTR) Review review,
                                BindingResult result, Model model, Authentication authentication) {
        return deleteReview(id, review.getId(), result, model, authentication);
    }

    protected void modelForError(@NotNull Model model, SourceBase current) {
        super.modelForError(model, current);
        model.addAttribute(SOURCE_ATTR, current);
    }

    protected String getEditPageTemplate() { return String.format("%s/update-src.html", PREFIX); }
    protected String getRedirectToEditPage(Long id) { return String.format("redirect:%s/details/%s", PREFIX, id); }


    //TODO: @ModelAttribute method e.g. 'void uiModel(@PathVariable("sourceId") long sourceId, Model model) { ... }'
}
