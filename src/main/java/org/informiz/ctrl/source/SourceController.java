package org.informiz.ctrl.source;

import org.informiz.model.SourceBase;
import org.informiz.repo.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping(path= SourceController.PREFIX)
public class SourceController {

    public static final String PREFIX = "/source";
    public static final String SOURCE_ATTR = "source";
    public static final String SOURCES_ATTR = "sources";
    @Autowired
    private SourceRepository sourceRepo;
    // TODO: chaincode DAO


    @GetMapping(path = {"/", "/all"})
    public String getAllSources(Model model) {
        model.addAttribute(SOURCES_ATTR, sourceRepo.findAll());
        return "all-src.html";
    }

    @GetMapping("/add")
    public String addSourceForm(Model model) {
        model.addAttribute(SOURCE_ATTR, new SourceBase());
        return "add-src.html";
    }

    @PostMapping("/add")
    public String addSource(@Valid @ModelAttribute(SOURCE_ATTR) SourceBase source,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return "add-src.html";
        }
        // TODO: Add to ledger
        sourceRepo.save(source);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/delete/{id}")
    public String deleteSource(@PathVariable("id") long id) {
        SourceBase source = sourceRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid source id"));
        // TODO: set inactive
        sourceRepo.delete(source);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/details/{id}")
    public String getSource(@PathVariable("id")  Long id, Model model) {
        SourceBase source = sourceRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid Source id"));
        model.addAttribute(SOURCE_ATTR, source);
        return "update-src.html";
    }

    @PostMapping("/details/{id}")
    public String updateSource(@PathVariable("id")  Long id,
                                    @Valid @ModelAttribute(SOURCE_ATTR) SourceBase source,
                                    BindingResult result, Model model) {
        if (! result.hasErrors()) {
            SourceBase current = sourceRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid source id"));
            current.edit(source);
            sourceRepo.save(current);
            model.addAttribute(SOURCE_ATTR, current);
        }
        return "update-src.html";
    }
}