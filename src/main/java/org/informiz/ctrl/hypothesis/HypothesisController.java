package org.informiz.ctrl.hypothesis;

import jakarta.validation.Valid;
import org.informiz.ctrl.entity.ChaincodeEntityController;
import org.informiz.model.*;
import org.informiz.repo.hypothesis.HypothesisRepository;
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
@RequestMapping(path= HypothesisController.PREFIX)
@Validated
public class HypothesisController extends ChaincodeEntityController<HypothesisBase> {

    public static final String PREFIX = "/hypothesis";
    public static final String HYPOTHESIS_ATTR = "hypothesis"; // singular
    public static final String HYPOTHESES_ATTR = "hypotheses"; // plural

    private final SourceRepository sourceRepo;

    @Autowired
    public HypothesisController(HypothesisRepository repository, SourceRepository sourceRepo) {
        super(repository);
        this.sourceRepo = sourceRepo;
    }

    @GetMapping(path = {"/", "/all"})
    public String getAllHypotheses(Model model) {
        model.addAttribute(HYPOTHESES_ATTR, entityRepo.findAll());
        return String.format("%s/all-hypotheses.html", PREFIX);
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_MEMBER')")
    public String addHypothesisForm(Model model) {
        model.addAttribute(HYPOTHESIS_ATTR, new HypothesisBase());
        return String.format("%s/add-hypothesis.html", PREFIX);
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_MEMBER')")
    public String addHypothesis(@Validated(HypothesisBase.NewHypothesisFromUI.class) @ModelAttribute(HYPOTHESIS_ATTR) HypothesisBase hypothesis,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return "hypothesis/add-hypothesis.html";
        }
        // TODO: Add to ledger
        entityRepo.save(hypothesis);
        return String.format("redirect:%s/all", PREFIX);
    }
//Group validation missing?
    @PostMapping("/delete/{hypothesisId}")
    @PreAuthorize("hasAuthority('ROLE_MEMBER') and #ownerId == authentication.principal.name")
    public String deleteHypothesis(@PathVariable("hypothesisId") @Valid Long id, @RequestParam String ownerId) {
        HypothesisBase hypothesis = entityRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid hypothesis id"));
        // TODO: set inactive
        entityRepo.delete(hypothesis);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/view/{hypothesisId}")
    public String viewHypothesis(@PathVariable("hypothesisId") @Valid Long id, Model model) {
        HypothesisBase hypothesis = entityRepo.loadByLocalId(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid Hypothesis id"));

        model.addAttribute(HYPOTHESIS_ATTR, hypothesis);
        return String.format("%s/view-hypothesis.html", PREFIX);
    }

    // TODO: Remove references to deleted hypothesis
    @GetMapping("/details/{hypothesisId}")
    @PreAuthorize("hasAuthority('ROLE_MEMBER')")
    public String getHypothesis(@PathVariable("hypothesisId") @Valid Long id, Model model) {
        HypothesisBase hypothesis = entityRepo.loadByLocalId(id)
                .orElse(null); //Throw(() ->new IllegalArgumentException("Invalid Hypothesis id"));
        if (hypothesis == null) return String.format("redirect:%s/all", PREFIX);

        prepareEditModel(model, hypothesis, new Review(), new Reference());
        return getEditPageTemplate();
    }

    @PostMapping("/details/{hypothesisId}")
    @PreAuthorize("hasAuthority('ROLE_MEMBER') and #hypothesis.ownerId == authentication.principal.name")
    public String updateHypothesis(@PathVariable("hypothesisId") @Valid Long id,
                                    @Validated(HypothesisBase.ExistingHypothesisFromUI.class) @ModelAttribute(HYPOTHESIS_ATTR) HypothesisBase hypothesis,
                                    BindingResult result, Model model) {
        if (result.hasErrors()) {
            return failedEdit(model, result, hypothesis, hypothesis);
        }

        HypothesisBase current = entityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid hypothesis id"));
        current.edit(hypothesis);
        entityRepo.save(current);
        return getRedirectToEditPage( id);
    }

    @PostMapping("/{hypothesisId}/review/")
    //@Secured("ROLE_CHECKER")
    @PreAuthorize("hasAuthority('ROLE_CHECKER')")
    public String reviewHypothesis(@PathVariable("hypothesisId") @Valid Long id,
                                   @Validated(Review.NewUserReview.class) @ModelAttribute(REVIEW_ATTR) Review review,
                                   BindingResult result, Model model, Authentication authentication) {
        return reviewEntity(id, review, result, model, authentication);
    }

    @PostMapping("/{hypothesisId}/review/edit/")
    //@Secured("ROLE_CHECKER")
    @PreAuthorize("hasAuthority('ROLE_CHECKER') and #review.ownerId == authentication.principal.name")
    public String editReview(@PathVariable("hypothesisId") @Valid Long id,
                             @Validated(Review.ExistingUserReview.class) @ModelAttribute(REVIEW_ATTR) Review review,
                             BindingResult result, Model model, Authentication authentication) {

        return reviewEntity(id, review, result, model, authentication);
    }

    @PostMapping("/{hypothesisId}/review/del/")
    //@Secured("ROLE_CHECKER")
    @PreAuthorize("hasAuthority('ROLE_CHECKER') and #review.ownerId == authentication.principal.name")
    public String deleteReview (@PathVariable("hypothesisId") @Valid Long id,
                                @Validated(InformizEntity.DeleteEntity.class) @ModelAttribute(REVIEW_ATTR) Review review,
                                BindingResult result, Model model, Authentication authentication) {
        return deleteReview(id, review.getId(), result, model, authentication);
    }


    @PostMapping("/reference/{hypothesisId}")
    //@Secured("ROLE_CHECKER")
    @PreAuthorize("hasAuthority('ROLE_CHECKER')")
    public String addReference(@PathVariable("hypothesisId") @Valid Long id,
                               @Validated(Reference.NewUserReference.class) @ModelAttribute(REFERENCE_ATTR) Reference reference,
                               BindingResult result,Authentication authentication, Model model) {

        return referenceEntity(id, reference, result, model, authentication);
    }


    @PostMapping("/reference/{hypothesisId}/edit/")
    //@Secured("ROLE_CHECKER")
    @PreAuthorize("hasAuthority('ROLE_CHECKER') and #reference.ownerId == authentication.principal.name")
    public String editReference(@PathVariable("hypothesisId") @Valid Long id,
                                @Validated(Reference.ExistingUserReference.class) @ModelAttribute(REFERENCE_ATTR) Reference reference,
                                BindingResult result, Authentication authentication, Model model) {

        return referenceEntity(id, reference, result, model, authentication);
    }

// TODO: remove /ref/ from url
    @PostMapping("/reference/{hypothesisId}/ref/del/")
    //@Secured("ROLE_CHECKER")
    @PreAuthorize("hasAuthority('ROLE_CHECKER') and #reference.ownerId == authentication.principal.name")
    public String deleteReference(@PathVariable("hypothesisId") @Valid Long id,
                                  @Validated(InformizEntity.DeleteEntity.class) @ModelAttribute(REFERENCE_ATTR) Reference reference,
                                  BindingResult result, Model model, Authentication authentication) {
        return deleteReference(id, reference.getId(), result, model, authentication);
    }


    @PostMapping("/source-ref/{hypothesisId}")
    //@Secured("ROLE_CHECKER")
    @PreAuthorize("hasAuthority('ROLE_CHECKER')")
    public String addSource(@PathVariable("hypothesisId") @Valid Long id,
                            @Validated(SourceRef.NewUserSourceReference.class) @ModelAttribute(SOURCE_ATTR) SourceRef srcRef,
                            BindingResult result, Model model, Authentication authentication) {

        return sourceForEntity(id, srcRef, sourceRepo.findByEntityId(srcRef.getSrcEntityId()), result, model, authentication);
    }

    @PostMapping("/source-ref/{hypothesisId}/edit/")
    //@Secured("ROLE_CHECKER")
    @PreAuthorize("hasAuthority('ROLE_CHECKER') and #srcRef.ownerId == authentication.principal.name")
    public String editSrcRef(@PathVariable("hypothesisId") @Valid Long id,
                             @Validated(SourceRef.ExistingUserSourceReference.class) @ModelAttribute(SOURCE_ATTR) SourceRef srcRef,
                             BindingResult result, Model model, Authentication authentication) {

        return sourceForEntity(id, srcRef, sourceRepo.findByEntityId(srcRef.getSrcEntityId()), result, model, authentication);
    }

    @PostMapping("/source-ref/{hypothesisId}/del/")
    //@Secured("ROLE_CHECKER")
    @PreAuthorize("hasAuthority('ROLE_CHECKER') and #srcRef.ownerId == authentication.principal.name")
    public String deleteSrcRef(@PathVariable("hypothesisId") @Valid Long id,
                               @Validated(InformizEntity.DeleteEntity.class) @ModelAttribute(SOURCE_ATTR) SourceRef srcRef,
                               BindingResult result, Model model, Authentication authentication) {
        return deleteSrcReference(id, srcRef.getId(), result, model, authentication);

    }

    private void prepareEditModel(Model model, HypothesisBase hypothesis, Review review, Reference ref) {
        model.addAttribute(HYPOTHESIS_ATTR, hypothesis);
        model.addAttribute(REVIEW_ATTR, review);
        model.addAttribute(REFERENCE_ATTR, ref);
        model.addAttribute(SOURCE_ATTR, new SourceRef());
    }

    protected void modelForError(@NotNull Model model, HypothesisBase current) {
        super.modelForError(model, current);
        model.addAttribute(HYPOTHESIS_ATTR, current);
        if (! model.containsAttribute(REFERENCE_ATTR)) model.addAttribute(REFERENCE_ATTR, new Reference());
        if (! model.containsAttribute(SOURCE_ATTR)) model.addAttribute(SOURCE_ATTR, new SourceRef());
    }

    protected String getEditPageTemplate() { return String.format("%s/update-hypothesis.html", PREFIX); }
    protected String getRedirectToEditPage(Long id) { return String.format("redirect:%s/details/%s", PREFIX, id); }

}