package org.informiz.ctrl.citation;

import jakarta.validation.Valid;
import org.informiz.ctrl.entity.ChaincodeEntityController;
import org.informiz.model.CitationBase;
import org.informiz.model.InformizEntity;
import org.informiz.model.Review;
import org.informiz.model.SourceRef;
import org.informiz.repo.citation.CitationRepository;
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
    public CitationController(CitationRepository repository, SourceRepository sourceRepo) {
        super(repository);
        this.sourceRepo = sourceRepo;
    }
    @GetMapping("/view/{citationId}")
    public String viewCitation(@PathVariable("citationId") @Valid Long id, Model model) {
        CitationBase citation = entityRepo.loadByLocalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid citation id"));
        model.addAttribute(CITATION_ATTR, citation);
        return String.format("%s/view-citation.html", PREFIX);
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_MEMBER')")
    public String addCitationForm(Model model) {
        model.addAttribute(CITATION_ATTR, new CitationBase());
        return String.format("%s/add-citation.html", PREFIX);
    }

    @GetMapping("/details/{citationId}")
    @PreAuthorize("hasAuthority('ROLE_MEMBER')")
    public String getCitation(@PathVariable("citationId") Long id, Model model) {
        CitationBase citation = entityRepo.loadByLocalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid citation id"));
        model.addAttribute(CITATION_ATTR, citation);
        model.addAttribute(REVIEW_ATTR, new Review());
        model.addAttribute(SOURCE_ATTR, new SourceRef());
        return EDIT_PAGE_TEMPLATE;
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_MEMBER')")
    public String addCitation(@Validated(CitationBase.NewCitationFromUI.class) @ModelAttribute(CITATION_ATTR) CitationBase citation,
                              BindingResult result) {
        if (result.hasErrors()) {
            return String.format("%s/add-citation.html", PREFIX);
        }
        entityRepo.save(citation);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping(path = {"/", "/all"})
    public String getAllCitations(Model model) {
        model.addAttribute(CITATIONS_ATTR, entityRepo.findAll());
        return String.format("%s/all-citations.html", PREFIX);
    }

    @PostMapping("/details/{citationId}")
    @PreAuthorize("hasAuthority('ROLE_MEMBER') and #citation.ownerId == authentication.principal.name")
    public String updateCitation(@PathVariable("citationId") @Valid Long id,
                                 @Validated(CitationBase.ExistingCitationFromUI.class) @ModelAttribute(CITATION_ATTR) CitationBase citation,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            return failedEdit(model, result, citation, citation);
        }
        CitationBase current = entityRepo.loadByLocalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid citation id"));
        current.edit(citation);
        entityRepo.save(current);
        return getRedirectToEditPage(current.getLocalId());
    }

    @PostMapping("/delete/{citationId}")
    @PreAuthorize("hasAuthority('ROLE_MEMBER') and #ownerId == authentication.principal.name")
    public String deleteCitation(@PathVariable("citationId") @Valid Long id ,@RequestParam String ownerId) {
        CitationBase citation = entityRepo.loadByLocalId(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid citation id"));
        // TODO: set inactive
        entityRepo.delete(citation);
        return String.format("redirect:%s/all", PREFIX);
    }

    @PostMapping("/{citationId}/review/")
    @PreAuthorize("hasAuthority('ROLE_CHECKER')")
    public String addReview(@PathVariable("citationId") @Valid Long id,
                                 @Validated(Review.NewUserReview.class) @ModelAttribute(REVIEW_ATTR) Review review,
                                 BindingResult result, Model model, Authentication authentication) {
        return reviewEntity(id, review, result, model, authentication);
    }

    @PostMapping("/{citationId}/review/edit/")
    @PreAuthorize("hasAuthority('ROLE_CHECKER') and #review.ownerId == authentication.principal.name")
    public String editReview(@PathVariable("citationId") @Valid Long id,
                             @Validated(Review.ExistingUserReview.class) @ModelAttribute(REVIEW_ATTR) Review review,
                             BindingResult result, Model model, Authentication authentication) {
        return reviewEntity(id, review, result, model, authentication);
    }

    //Todo need to restrict mediaTypes
    @PostMapping(value = "/{citationId}/review/del/"/*, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE*/)
    @PreAuthorize("hasAuthority('ROLE_CHECKER') and #review.ownerId == authentication.principal.name") //
    public String deleteReview(@PathVariable("citationId") @Valid Long id,
                               @Validated(InformizEntity.ExistingEntityFromUI.class) @ModelAttribute(REVIEW_ATTR) Review review,
                               BindingResult result, Model model, Authentication authentication) {
        return deleteReview(id, review.getId(), result, model, authentication);
    }

    @PostMapping("/source-ref/{citationId}")
    @PreAuthorize("hasAuthority('ROLE_CHECKER')")
    public String addSource(@PathVariable("citationId") @Valid Long id,
                            @Validated(SourceRef.NewUserSourceReference.class) @ModelAttribute(SOURCE_ATTR) SourceRef srcRef,
                            BindingResult result, Model model, Authentication authentication) {
        return sourceForEntity(id, srcRef, sourceRepo.findByEntityId(srcRef.getSrcEntityId()), result, model, authentication);
    }

    @PostMapping("/source-ref/{citationId}/edit/")
    @PreAuthorize("hasAuthority('ROLE_CHECKER') and #srcRef.ownerId == authentication.principal.name")
    public String editSourceRef(@PathVariable("citationId") @Valid Long id,
                             @Validated(SourceRef.ExistingUserSourceReference.class) @ModelAttribute(SOURCE_ATTR) SourceRef srcRef,
                             BindingResult result, Model model, Authentication authentication) {
        return sourceForEntity(id, srcRef, sourceRepo.findByEntityId(srcRef.getSrcEntityId()), result, model, authentication);
    }

    @PostMapping("/source-ref/{citationId}/del/")
    @PreAuthorize("hasAuthority('ROLE_CHECKER') and #srcRef.ownerId == authentication.principal.name")
    public String deleteSrcRef(@PathVariable("citationId") @Valid Long id,
                               @Validated(InformizEntity.ExistingEntityFromUI.class) @ModelAttribute(SOURCE_ATTR) SourceRef srcRef,
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