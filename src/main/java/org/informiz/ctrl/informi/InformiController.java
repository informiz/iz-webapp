package org.informiz.ctrl.informi;

import org.informiz.auth.AuthUtils;
import org.informiz.ctrl.entity.ChaincodeEntityController;
import org.informiz.model.InformiBase;
import org.informiz.model.Review;
import org.informiz.repo.informi.InformiRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
@RequestMapping(path = InformiController.PREFIX)
public class InformiController extends ChaincodeEntityController<InformiBase> {

    public static final String PREFIX = "/informi";
    public static final String INFORMI_ATTR = "informi";
    public static final String INFORMIZ_ATTR = "informiz";


    @GetMapping(path = {"/", "/all"})
    public String getAllInformiz(Model model) {
        model.addAttribute(INFORMIZ_ATTR, entityRepo.findAll());
        return String.format("%s/all-informiz.html", PREFIX);
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
                                 BindingResult result) {
        if (result.hasErrors()) {
            if (result.getErrorCount() > 1 || ! result.hasFieldErrors("mediaPath"))
                return String.format("%s/add-informi.html", PREFIX);
        }

        MultipartFile file = informi.getFile();
        if (! file.isEmpty()) {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            try {
                // TODO: reprocess images, random filename
                String path = AuthUtils.uploadMedia(file.getBytes(), fileName);
                informi.setMediaPath(path);
                entityRepo.save(informi);
            } catch (IOException e) {
                // TODO: error
                return String.format("%s/add-informi.html", PREFIX);
            }
        } else {
            // TODO: error
            return String.format("%s/add-informi.html", PREFIX);
        }
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
        model.addAttribute(INFORMI_ATTR, informi);
        model.addAttribute(REVIEW_ATTR, new Review());
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
            return String.format("redirect:%s/all", PREFIX);
        }
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
            model.addAttribute(REVIEW_ATTR, new Review());
        }

        return String.format("redirect:%s/details/%s", PREFIX, current.getId());
    }
}