package org.informiz.ctrl.citation;

import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.informiz.ctrl.entity.ChaincodeEntityController;
import org.informiz.model.CitationBase;
import org.informiz.model.Review;
import org.informiz.model.SourceBase;
import org.informiz.model.SourceRef;
import org.informiz.repo.citation.CitationRepository;
import org.informiz.repo.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(path = CitationController.PREFIX)
@Validated
public class CitationController extends ChaincodeEntityController<CitationBase> {

    public static final String PREFIX = "/citation";
    public static final String CITATION_ATTR = "citation";
    public static final String SOURCE_ATTR = "source";
    public static final String CITATIONS_ATTR = "citations";

    // TODO: duplicated code, move to superclass for sourceable entities
    private final SourceRepository sourceRepo;

    @Autowired
    public CitationController(CitationRepository repository, SourceRepository sourceRepo) {
        super(repository);
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
    public String addCitation(@Valid @ModelAttribute(CITATION_ATTR) CitationBase citation,
                              BindingResult result) {
        if (result.hasErrors()) {
            return String.format("%s/add-citation.html", PREFIX);
        }
        entityRepo.save(citation);

        return String.format("redirect:%s/all", PREFIX);

    }

    @PostMapping("/delete/{id}")
    @Secured("ROLE_MEMBER")
    @PreAuthorize("#ownerId == authentication.principal.name")
    public String deleteCitation(@PathVariable("id") @Valid Long id ,@RequestParam String ownerId) {
        CitationBase citation = entityRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid citation id"));
        // TODO: set inactive
        entityRepo.delete(citation);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/view/{id}")
    public String viewCitation(@PathVariable("id") @Valid Long id, Model model) {
        CitationBase citation = entityRepo.loadByLocalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid citation id"));
        model.addAttribute(CITATION_ATTR, citation);
        return String.format("%s/view-citation.html", PREFIX);
    }

    @GetMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    public String getCitation(@PathVariable("id") Long id, Model model) {
        CitationBase citation = entityRepo.loadByLocalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid citation id"));
        model.addAttribute(CITATION_ATTR, citation);
        model.addAttribute(REVIEW_ATTR, new Review());
        model.addAttribute(SOURCE_ATTR, new SourceRef());
        return String.format("%s/update-citation.html", PREFIX);
    }

    @PostMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    @PreAuthorize("#citation.ownerId == authentication.principal.name")
    public String updateCitation(@PathVariable("id") @Valid Long id,
                                 @Valid @ModelAttribute(CITATION_ATTR) CitationBase citation,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute(REVIEW_ATTR, new Review());
            model.addAttribute(SOURCE_ATTR, new SourceRef());
            return String.format("%s/update-citation.html", PREFIX);
        }

        CitationBase current = entityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid citation id"));
        current.edit(citation);
        entityRepo.save(current);
        return String.format("redirect:%s/details/%s", PREFIX, current.getLocalId());
    }

    @PostMapping("/{id}/review/")
    @Secured("ROLE_CHECKER")
    public String addReview(@PathVariable("id") @Valid Long id,
                                 @Validated(Review.UserReview.class) @ModelAttribute(REVIEW_ATTR) Review review,
                                 BindingResult result, Model model, Authentication authentication) {
        return reviewCitation(id, review, result, model, authentication);
    }

    @PostMapping("/{id}/review/edit/")
    @Secured("ROLE_CHECKER")
    @PreAuthorize("#review.ownerId == authentication.principal.name")
    public String editReview(@PathVariable("id") @Valid Long id,
                             @Validated(Review.UserReview.class) @ModelAttribute(REVIEW_ATTR) Review review,
                                 BindingResult result, Model model, Authentication authentication) {
        return reviewCitation(id, review, result, model, authentication);
    }

    private String reviewCitation(Long id, Review review, BindingResult result, Model model, Authentication authentication) {
        CitationBase current = reviewEntity(id, review, authentication, result);

        if(current == null) {
            // Review successful, redirect back to edit page
            return String.format("redirect:%s/details/%s", PREFIX, id);
        }
        // Failed to review, reloaded page presents errors in UI, just add necessary entities to the model
        model.addAttribute(CITATION_ATTR, current);
        model.addAttribute(SOURCE_ATTR, new SourceRef());
        return String.format("%s/update-citation.html", PREFIX);
    }

    @PostMapping("/{id}/review/del/")
    @Secured("ROLE_CHECKER")
    @PreAuthorize("#review.ownerId == authentication.principal.name")
    public String deleteReview(@PathVariable("id") @Valid Long id,
                               @Valid @Review.ReviewDeletion @ModelAttribute(REVIEW_ATTR) Review review,
                                  Authentication authentication) {

        CitationBase current = deleteReview(id, review.getId(), authentication);
        entityRepo.save(current);
        return String.format("redirect:%s/details/%s", PREFIX, id);
    }


    @PostMapping("/source/{id}")
    @Secured("ROLE_CHECKER")
    @Transactional
    public String addSource(@PathVariable("id") @Valid Long id, @ModelAttribute(SOURCE_ATTR) SourceRef srcRef,
                            BindingResult result) {

        CitationBase current = entityRepo.loadByLocalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Citation id"));

        SourceBase source = null;
        if (StringUtils.isNotBlank(srcRef.getSrcEntityId())) {
            source = sourceRepo.findByEntityId(srcRef.getSrcEntityId());
        }

        try {
            SourceRef toAdd = new SourceRef(source, current, srcRef.getLink(), srcRef.getDescription());
            current.addSource(toAdd);

        } catch (IllegalArgumentException e) {
            result.addError(new ObjectError("link",
                    "Please provide either a link, a source or both"));
        }
        return String.format("redirect:%s/details/%s", PREFIX, current.getLocalId());
    }


    @PostMapping("/source/{id}/{srcId}")
    @Secured("ROLE_CHECKER")
    @Transactional
    @PreAuthorize("#source.ownerId == authentication.principal.name")
    // TODO: resolve codes duplicate issue
    public String editSrcRef(@PathVariable("id") @Valid Long id,
                             @PathVariable("srcId") @Valid Long srcId,
                             Authentication authentication) {

        return String.format("redirect:%s/details/%s", PREFIX, id);
    }


    @PostMapping("/source/{id}/del/{refId}")
    @Secured("ROLE_CHECKER")
    @Transactional
    @PreAuthorize("#source.ownerId == authentication.principal.name")
    // TODO: resolve codes duplicate issue
    public String deleteSrcRef(@PathVariable("id") @Valid Long id,
                               @PathVariable("refId") @Valid Long refId,
                               Authentication authentication) {

        CitationBase current = entityRepo.loadByLocalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Claim id"));

        current.removeSource(refId);
        return String.format("redirect:%s/details/%s", PREFIX, id);
    }

    // TODO: which methods need to be declared transactional?

}