package org.informiz.ctrl.reference;

import org.informiz.model.ReferenceTextBase;
import org.informiz.repo.reference.ReferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping(path= ReferenceController.PREFIX)
public class ReferenceController {

    public static final String PREFIX = "/reference";
    public static final String REFERENCE_ATTR = "reference";
    public static final String REFERENCES_ATTR = "references";
    @Autowired
    private ReferenceRepository referenceRepository;
    // TODO: chaincode DAO


    @GetMapping(path = {"/", "/all"})
    public String getAllReferences(Model model) {
        model.addAttribute(REFERENCES_ATTR, referenceRepository.findAll());
        return String.format("%s/all-references.html", PREFIX);
    }

    @GetMapping("/add")
    @Secured("ROLE_MEMBER")
    public String addReferenceForm(Model model) {
        model.addAttribute(REFERENCE_ATTR, new ReferenceTextBase());
        return String.format("%s/add-reference.html", PREFIX);
    }

    @PostMapping("/add")
    @Secured("ROLE_MEMBER")
    public String addReference(@Valid @ModelAttribute(REFERENCE_ATTR) ReferenceTextBase reference,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return String.format("%s/add-reference.html", PREFIX);
        }
        // TODO: Add to ledger
        referenceRepository.save(reference);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/delete/{id}")
    public String deleteReference(@PathVariable("id") long id) {
        ReferenceTextBase reference = referenceRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid reference id"));
        // TODO: set inactive
        referenceRepository.delete(reference);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/view/{id}")
    public String viewReference(@PathVariable("id")  Long id, Model model) {
        ReferenceTextBase reference = referenceRepository.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid reference id"));
        model.addAttribute(REFERENCE_ATTR, reference);
        return String.format("%s/view-reference.html", PREFIX);
    }

    @GetMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    public String getReference(@PathVariable("id")  Long id, Model model) {
        ReferenceTextBase reference = referenceRepository.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid reference id"));
        model.addAttribute(REFERENCE_ATTR, reference);
        return String.format("%s/update-reference.html", PREFIX);
    }

    @PostMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    public String updateReference(@PathVariable("id")  Long id,
                                    @Valid @ModelAttribute(REFERENCE_ATTR) ReferenceTextBase reference,
                                    BindingResult result, Model model) {
        if (! result.hasErrors()) {
            ReferenceTextBase current = referenceRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid reference id"));
            current.edit(reference);
            referenceRepository.save(current);
            model.addAttribute(REFERENCE_ATTR, current);
            return String.format("redirect:%s/all", PREFIX);
        }
        return String.format("%s/update-reference.html", PREFIX);
    }
}