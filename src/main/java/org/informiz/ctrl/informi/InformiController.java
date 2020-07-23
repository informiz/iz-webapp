package org.informiz.ctrl.informi;

import org.informiz.auth.AuthUtils;
import org.informiz.model.InformiBase;
import org.informiz.repo.informi.InformiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@Controller
@RequestMapping(path = InformiController.PREFIX)
public class InformiController {

    public static final String PREFIX = "/informi";
    public static final String INFORMI_ATTR = "informi";
    public static final String INFORMIZ_ATTR = "informiz";
    @Autowired
    private InformiRepository informiRepo;
    // TODO: chaincode DAO


    @GetMapping(path = {"/", "/all"})
    public String getAllInformiz(Model model) {
        model.addAttribute(INFORMIZ_ATTR, informiRepo.findAll());
        return String.format("%s/all-informiz.html", PREFIX);
    }

    @GetMapping("/add")
    public String addInformiForm(Model model) {
        model.addAttribute(INFORMI_ATTR, new InformiBase());
        return String.format("%s/add-informi.html", PREFIX);
    }

    @PostMapping("/add")
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
                String path = AuthUtils.uploadMedia(file.getBytes(), fileName);
                informi.setMediaPath(path);
                informiRepo.save(informi);
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
    public String deleteInformi(@PathVariable("id") long id) {
        InformiBase informi = informiRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid informi id"));
        // TODO: set inactive
        informiRepo.delete(informi);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/view/{id}")
    public String viewInformi(@PathVariable("id")  Long id, Model model) {
        InformiBase informi = informiRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid informi id"));
        model.addAttribute(INFORMI_ATTR, informi);
        return String.format("%s/view-informi.html", PREFIX);
    }

    @GetMapping("/details/{id}")
    public String getInformi(@PathVariable("id")  Long id, Model model) {
        InformiBase informi = informiRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid informi id"));
        model.addAttribute(INFORMI_ATTR, informi);
        return String.format("%s/update-informi.html", PREFIX);
    }

    @PostMapping("/details/{id}")
    public String updateInformi(@PathVariable("id")  Long id,
                                    @Valid @ModelAttribute(INFORMI_ATTR) InformiBase informi,
                                    BindingResult result, Model model) {
        if (! result.hasErrors()) {
            InformiBase current = informiRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid informi id"));
            current.edit(informi);
            informiRepo.save(current);
            model.addAttribute(INFORMI_ATTR, current);
            return String.format("redirect:%s/all", PREFIX);
        }
        return String.format("%s/update-informi.html", PREFIX);
    }
}