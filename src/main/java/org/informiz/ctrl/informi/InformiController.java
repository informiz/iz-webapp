package org.informiz.ctrl.informi;

import jakarta.validation.Valid;
import org.informiz.auth.AuthUtils;
import org.informiz.ctrl.entity.ChaincodeEntityController;
import org.informiz.model.InformiBase;
import org.informiz.model.InformizEntity;
import org.informiz.model.Reference;
import org.informiz.model.Review;
import org.informiz.repo.reference.ReferenceRepository;
import org.informiz.repo.review.ReviewRepository;
import org.informiz.repo.informi.InformiRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping(path = InformiController.PREFIX)
@Validated
public class InformiController extends ChaincodeEntityController<InformiBase> {

    public static final String PREFIX = "/informi";
    public static final String INFORMI_ATTR = "informi";
    public static final String INFORMIZ_ATTR = "informiz";

    @Autowired
    public InformiController(InformiRepository repository, ReviewRepository reviewRepo, ReferenceRepository refRepo) {
        super(repository, reviewRepo, refRepo, null);
    }

    @GetMapping(path = {"/", "/all"})
    public String getAllInformiz(Model model) {
        model.addAttribute(INFORMIZ_ATTR, entityRepo.findAll());
        return String.format("%s/all-informiz.html", PREFIX);
    }

    @GetMapping("/upload")
    @Secured("ROLE_MEMBER")
    public String uploadInformiMedia() {
        return String.format("%s/upload-media.html", PREFIX);
    }

    //@PostMapping("/upload")
    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = "multipart/form-data")
    @Secured("ROLE_MEMBER")
    public String uploadInformiMedia(@RequestParam("file") MultipartFile file, Model model) {
        if (file != null && ! file.isEmpty()) {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            try (InputStream inStream = file.getInputStream()) {
                // TODO: reprocess images, random filename
                String path = AuthUtils.uploadMedia(inStream, fileName);
                InformiBase informi = new InformiBase();
                informi.setMediaPath(path);
                model.addAttribute(INFORMI_ATTR, informi);
                return String.format("%s/add-informi.html", PREFIX);
            } catch (IOException e) {
                // TODO: implement @ControllerAdvice
                e.printStackTrace();
            }
        }
        return String.format("%s/upload-media.html", PREFIX);
    }

    @GetMapping("/add")
    @Secured("ROLE_MEMBER")
    public String addInformiForm(Model model) {
        model.addAttribute(INFORMI_ATTR, new InformiBase());
        return String.format("%s/add-informi.html", PREFIX);
    }

    @PostMapping("/add")
    @Secured("ROLE_MEMBER")
    public String addInformi(@Validated(InformiBase.InformiFromUI.class) @ModelAttribute(INFORMI_ATTR) InformiBase informi,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            return String.format("%s/add-informi.html", PREFIX);
        }
        model.addAttribute(INFORMI_ATTR, entityRepo.save(informi));
        return String.format("redirect:%s/all", PREFIX);
    }

    @PostMapping("/delete/{informiId}")
    @Secured("ROLE_MEMBER")
    @PreAuthorize("#ownerId == authentication.principal.name")
    public String deleteInformi(@PathVariable("informiId") @Valid Long id, @RequestParam String ownerId) {
        InformiBase informi = entityRepo.loadByLocalId(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid informi id"));
        // TODO: set inactive
        entityRepo.delete(informi);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/view/{informiId}")
    public String viewInformi(@PathVariable("informiId") @Valid Long id, Model model) {
        InformiBase informi = entityRepo.loadByLocalId(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid informi id"));
        model.addAttribute(INFORMI_ATTR, informi);
        return String.format("%s/view-informi.html", PREFIX);
    }

    @GetMapping("/details/{informiId}")
    @Secured("ROLE_MEMBER")
    public String getInformi(@PathVariable("informiId") @Valid Long id, Model model) {
        InformiBase informi = entityRepo.loadByLocalId(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid informi id"));
        prepareEditModel(model, informi, new Review(), new Reference());
        return getEditPageTemplate();
    }

    @PostMapping("/details/{informiId}")
    @Secured("ROLE_MEMBER")
    @PreAuthorize("#informi.getOwnerId() == principal.getAttributes().get('eid')")
    public String updateInformi(@PathVariable("informiId") @Valid Long id,
                                    @Validated(InformiBase.InformiFromUI.class) @ModelAttribute(INFORMI_ATTR) InformiBase informi,
                                    BindingResult result, Model model) {
        if (! result.hasErrors()) {
            InformiBase current = entityRepo.loadByLocalId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid informi id"));
            current.edit(informi);
            entityRepo.save(current);
        }
        return failedEdit(model, result, informi, informi);
    }

    @PostMapping("/{informiId}/review/")
    @Secured("ROLE_CHECKER")
    public String reviewInformi(@PathVariable("informiId") @Valid Long id,
                                @Validated(Review.UserReview.class) @ModelAttribute(REVIEW_ATTR) Review review,
                                BindingResult result, Model model, Authentication authentication) {
        return reviewEntity(id, review, result, model, authentication);
    }

    @PostMapping("/{informiId}/review/edit/")
    @Secured("ROLE_CHECKER")
    @PreAuthorize("#review.ownerId == authentication.principal.name")
    public String editReview(@PathVariable("informiId") @Valid Long id,
                                    @Validated(Review.UserReview.class) @ModelAttribute(REVIEW_ATTR) Review review,
                                    BindingResult result, Model model, Authentication authentication) {
        return reviewEntity(id, review, result, model, authentication);
    }

    @PostMapping("/{informiId}/review/del/")
    @Secured("ROLE_CHECKER")
    @PreAuthorize("#review.ownerId == authentication.principal.name")
    public String deleteReview (@PathVariable("informiId") @Valid Long id,
                                @Validated(InformizEntity.DeleteEntity.class) @ModelAttribute(REVIEW_ATTR) Review review,
                                BindingResult result, Model model, Authentication authentication) {
        return deleteReview(id, review.getId(), result, model, authentication);
    }

    @PostMapping("/reference/{informiId}")
    @Secured("ROLE_CHECKER")
    public String addReference(@PathVariable("informiId") @Valid Long id,
                               @Validated(Reference.UserReference.class) @ModelAttribute(REFERENCE_ATTR) Reference reference,
                               BindingResult result, Model model, Authentication authentication) {
        return referenceEntity(id, reference, result, model, authentication);
    }

    @PostMapping("/reference/{informiId}/edit/")
    @Secured("ROLE_CHECKER")
    @PreAuthorize("#reference.ownerId == authentication.principal.name")
    public String editReference(@PathVariable("informiId") @Valid Long id,
                                @Validated(Reference.UserReference.class) @ModelAttribute(REFERENCE_ATTR) Reference reference,
                               BindingResult result, Model model, Authentication authentication) {
        return referenceEntity(id, reference, result, model, authentication);
    }

    @PostMapping("/reference/{informiId}/ref/del/")
    @Secured("ROLE_CHECKER")
    @PreAuthorize("#reference.ownerId == authentication.principal.name")
    public String deleteReference(@PathVariable("informiId") @Valid Long id,
                                  @Validated(InformizEntity.DeleteEntity.class) @ModelAttribute(REFERENCE_ATTR) Reference reference,
                                  BindingResult result, Model model, Authentication authentication) {
        return deleteReference(id, reference.getId(), result, model, authentication);
    }

    private void prepareEditModel(Model model, InformiBase informi, Review review, Reference ref) {
        model.addAttribute(INFORMI_ATTR, informi);
        model.addAttribute(REVIEW_ATTR, review);
        model.addAttribute(REFERENCE_ATTR, ref);
    }

    @Override
    protected void modelForError(@NotNull Model model, InformiBase current) {
        super.modelForError(model, current);
        model.addAttribute(INFORMI_ATTR, current);
        if (! model.containsAttribute(REFERENCE_ATTR)) model.addAttribute(REFERENCE_ATTR, new Reference());
    }

    protected String getEditPageTemplate() { return String.format("%s/update-informi.html", PREFIX); }
    protected String getRedirectToEditPage(Long id) { return String.format("redirect:%s/details/%s", PREFIX, id); }

}

