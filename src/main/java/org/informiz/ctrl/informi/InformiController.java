package org.informiz.ctrl.informi;

import org.informiz.auth.AuthUtils;
import org.informiz.ctrl.entity.ChaincodeEntityController;
import org.informiz.model.InformiBase;
import org.informiz.model.Reference;
import org.informiz.model.Review;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping(path = InformiController.PREFIX)
public class InformiController extends ChaincodeEntityController<InformiBase> {

    public static final String PREFIX = "/informi";
    public static final String INFORMI_ATTR = "informi";
    public static final String INFORMIZ_ATTR = "informiz";
    public static final String REFERENCE_ATTR = "reference";


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
    public String addInformi(@Valid @ModelAttribute(INFORMI_ATTR) InformiBase informi,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            return String.format("%s/add-informi.html", PREFIX);
        }
        model.addAttribute(INFORMI_ATTR, entityRepo.save(informi));
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/delete/{id}")
    @Secured("ROLE_MEMBER")
    public String deleteInformi(@PathVariable("id") @Valid Long id) {
        InformiBase informi = entityRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid informi id"));
        // TODO: set inactive
        entityRepo.delete(informi);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/view/{id}")
    public String viewInformi(@PathVariable("id") @Valid Long id, Model model) {
        InformiBase informi = entityRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid informi id"));
        model.addAttribute(INFORMI_ATTR, informi);
        return String.format("%s/view-informi.html", PREFIX);
    }

    @GetMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    public String getInformi(@PathVariable("id") @Valid Long id, Model model) {
        InformiBase informi = entityRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid informi id"));
        prepareEditModel(model, informi, new Review(), new Reference());
        return String.format("%s/update-informi.html", PREFIX);
    }

    @PostMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    public String updateInformi(@PathVariable("id") @Valid Long id,
                                    @Valid @ModelAttribute(INFORMI_ATTR) InformiBase informi,
                                    BindingResult result, Model model) {
        if (! result.hasErrors()) {
            InformiBase current = entityRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid informi id"));
            current.edit(informi);
            entityRepo.save(current);
            model.addAttribute(INFORMI_ATTR, current);
            return String.format("redirect:%s/view/%s", PREFIX, id);
        }
        prepareEditModel(model, informi, new Review(), new Reference());
        return String.format("%s/update-informi.html", PREFIX);
    }

    @PostMapping("/review/{id}")
    @Secured("ROLE_USER")
    @Transactional
    public String reviewInformi(@PathVariable("id") @Valid Long id,
                                   @Valid @ModelAttribute(REVIEW_ATTR) Review review,
                                   BindingResult result, Authentication authentication, Model model) {

        InformiBase current = entityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid source id"));

        if ( ! result.hasFieldErrors("rating")) {
            current = reviewEntity(current, review, authentication);
            model.addAttribute(INFORMI_ATTR, current);
            return String.format("redirect:%s/view/%s", PREFIX, id);
        }
        prepareEditModel(model, current, review, new Reference());
        return String.format("%s/update-informi.html", PREFIX);
    }

    @PostMapping("/reference/{id}")
    @Secured("ROLE_USER")
    @Transactional
    public String addReference(@PathVariable("id") @Valid Long id,
                                @Valid @ModelAttribute(REFERENCE_ATTR) Reference reference,
                                BindingResult result, Model model) {

        InformiBase current = entityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid source id"));

        if ( ! (result.hasFieldErrors("citationId") || result.hasFieldErrors("entailment"))) {
            current = addReference(current, reference);
            model.addAttribute(INFORMI_ATTR, current);
            return String.format("redirect:%s/view/%s", PREFIX, id);
        }
        prepareEditModel(model, current, new Review(), reference);
        return String.format("%s/update-informi.html", PREFIX);
    }

    protected InformiBase addReference(InformiBase informi, Reference reference) {
        reference.setReviewed(informi);
        Reference current = informi.getReference(reference);
        if (current != null) {
            current.setEntailment(reference.getEntailment());
            current.setComment(reference.getComment());
        } else {
            informi.addReference(new Reference(informi, reference.getCitationId(),
                    reference.getEntailment(), reference.getComment()));
        }
        return informi;
    }

    private void prepareEditModel(Model model, InformiBase informi, Review review, Reference ref) {
        model.addAttribute(INFORMI_ATTR, informi);
        model.addAttribute(REVIEW_ATTR, review);
        model.addAttribute(REFERENCE_ATTR, ref);
    }
}