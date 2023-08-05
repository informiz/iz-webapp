package org.informiz.ctrl.citation;

import jakarta.validation.Valid;
import org.informiz.ctrl.entity.ChaincodeEntityController;
import org.informiz.model.CitationBase;
import org.informiz.model.InformizEntity;
import org.informiz.model.Review;
import org.informiz.model.SourceRef;
import org.informiz.repo.review.ReviewRepository;
import org.informiz.repo.citation.CitationRepository;
import org.informiz.repo.source.SourceRepository;
import org.informiz.repo.src_ref.SourceRefRepository;
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
@RequestMapping(path = CitationController.PREFIX)
@Validated
public class CitationController extends ChaincodeEntityController<CitationBase> {

    public static final String PREFIX = "/citation";
    public static final String EDIT_PAGE_TEMPLATE = String.format("%s/update-citation.html", PREFIX);
    public static final String CITATION_ATTR = "citation";
    public static final String CITATIONS_ATTR = "citations";

    // TODO: duplicated code, move to superclass for sourceable entities
    // TODO: sources are not necessarily in local channel
    private final SourceRepository sourceRepo;

    @Autowired
    public CitationController(CitationRepository repository, SourceRepository sourceRepo,
                              ReviewRepository reviewRepo, SourceRefRepository srcRefRepo) {
        super(repository, reviewRepo, null, srcRefRepo);
        this.sourceRepo = sourceRepo;
    }

    @GetMapping(path = {"/", "/all"})
    public String getAllCitations(Model model) {
        model.addAttribute(CITATIONS_ATTR, entityRepo.findAll());
        return String.format("%s/all-citations.html", PREFIX);
    }

    @GetMapping("/add")
    @Secured("ROLE_MEMBER")
    public String addCitationForm(Model model) {
        model.addAttribute(CITATION_ATTR, new CitationBase());
        return String.format("%s/add-citation.html", PREFIX);
    }

    @PostMapping("/add")
    @Secured("ROLE_MEMBER")
    public String addCitation(@Validated(CitationBase.CitationFromUI.class) @ModelAttribute(CITATION_ATTR) CitationBase citation,
                              BindingResult result) {
        if (result.hasErrors()) {
            return String.format("%s/add-citation.html", PREFIX);
        }
        entityRepo.save(citation);

        return String.format("redirect:%s/all", PREFIX);

    }

    @PostMapping("/delete/{citationId}")
    @Secured("ROLE_MEMBER")
    @PreAuthorize("#ownerId == authentication.principal.name")
    public String deleteCitation(@PathVariable("citationId") @Valid Long id ,@RequestParam String ownerId) {
        CitationBase citation = entityRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid citation id"));
        // TODO: set inactive
        entityRepo.delete(citation);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/view/{citationId}")
    public String viewCitation(@PathVariable("citationId") @Valid Long id, Model model) {
        CitationBase citation = entityRepo.loadByLocalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid citation id"));
        model.addAttribute(CITATION_ATTR, citation);
        return String.format("%s/view-citation.html", PREFIX);
    }

    @GetMapping("/details/{citationId}")
    @Secured("ROLE_MEMBER")
    public String getCitation(@PathVariable("citationId") Long id, Model model) {
        CitationBase citation = entityRepo.loadByLocalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid citation id"));
        model.addAttribute(CITATION_ATTR, citation);
        model.addAttribute(REVIEW_ATTR, new Review());
        model.addAttribute(SOURCE_ATTR, new SourceRef());
        return EDIT_PAGE_TEMPLATE;
    }

    @PostMapping("/details/{citationId}")
    @Secured("ROLE_MEMBER")
    @PreAuthorize("#citation.ownerId == authentication.principal.name")
    public String updateCitation(@PathVariable("citationId") @Valid Long id,
                                 @Validated(CitationBase.CitationFromUI.class) @ModelAttribute(CITATION_ATTR) CitationBase citation,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            return failedEdit(model, result, citation, citation);
        }

        CitationBase current = entityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid citation id"));
        current.edit(citation);
        entityRepo.save(current);
        return getRedirectToEditPage(current.getLocalId());
    }

    @PostMapping("/{citationId}/review/")
    @Secured("ROLE_CHECKER")
    public String addReview(@PathVariable("citationId") @Valid Long id,
                                 @Validated(Review.UserReview.class) @ModelAttribute(REVIEW_ATTR) Review review,
                                 BindingResult result, Model model, Authentication authentication) {
        return reviewEntity(id, review, result, model, authentication);
    }

    @PostMapping("/{citationId}/review/edit/")
    @Secured("ROLE_CHECKER")
    @PreAuthorize("#review.ownerId == authentication.principal.name")
    public String editReview(@PathVariable("citationId") @Valid Long id,
                             @Validated(Review.UserReview.class) @ModelAttribute(REVIEW_ATTR) Review review,
                             BindingResult result, Model model, Authentication authentication) {
        return reviewEntity(id, review, result, model, authentication);
    }


    @PostMapping("/{citationId}/review/del/")
    @Secured("ROLE_CHECKER")
    @PreAuthorize("#review.ownerId == authentication.principal.name")
    public String deleteReview(@PathVariable("citationId") @Valid Long id,
                               @Validated(InformizEntity.DeleteEntity.class) @ModelAttribute(REVIEW_ATTR) Review review,
                               BindingResult result, Model model, Authentication authentication) {
        return deleteReview(id, review.getId(), result, model, authentication);
    }


    @PostMapping("/source-ref/{citationId}")
    @Secured("ROLE_CHECKER")
    public String addSource(@PathVariable("citationId") @Valid Long id,
                            @Validated(SourceRef.UserSourceReference.class) @ModelAttribute(SOURCE_ATTR) SourceRef srcRef,
                            BindingResult result, Model model, Authentication authentication) {
        return sourceForEntity(id, srcRef, sourceRepo.findByEntityId(srcRef.getSrcEntityId()), result, model, authentication);
    }


    @PostMapping("/source-ref/{citationId}/edit/")
    @Secured("ROLE_CHECKER")
    @PreAuthorize("#srcRef.ownerId == authentication.principal.name")
    public String editSourceRef(@PathVariable("citationId") @Valid Long id,
                             @Validated(SourceRef.UserSourceReference.class) @ModelAttribute(SOURCE_ATTR) SourceRef srcRef,
                             BindingResult result, Model model, Authentication authentication) {
        return sourceForEntity(id, srcRef, sourceRepo.findByEntityId(srcRef.getSrcEntityId()), result, model, authentication);
    }


    @PostMapping("/source-ref/{citationId}/del/")
    @Secured("ROLE_CHECKER")
    @PreAuthorize("#srcRef.ownerId == authentication.principal.name")
    public String deleteSrcRef(@PathVariable("citationId") @Valid Long id,
                               @Validated(InformizEntity.DeleteEntity.class) @ModelAttribute(SOURCE_ATTR) SourceRef srcRef,
                               BindingResult result, Model model, Authentication authentication) {
        return deleteSrcReference(id, srcRef.getId(), result, model, authentication);
    }

    @Override
    protected void modelForError(@NotNull Model model, CitationBase current) {
        super.modelForError(model, current);
        model.addAttribute(CITATION_ATTR, current);
        if (! model.containsAttribute(SOURCE_ATTR)) model.addAttribute(SOURCE_ATTR, new SourceRef());
    }

    protected String getEditPageTemplate() { return String.format("%s/update-citation.html", PREFIX); }
    protected String getRedirectToEditPage(Long id) { return String.format("redirect:%s/details/%s", PREFIX, id); }

}