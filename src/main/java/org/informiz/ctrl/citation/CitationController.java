package org.informiz.ctrl.citation;

import org.informiz.ctrl.entity.ChaincodeEntityController;
import org.informiz.model.CitationBase;
import org.informiz.model.Review;
import org.informiz.model.SourceRef;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping(path= CitationController.PREFIX)
public class CitationController extends ChaincodeEntityController<CitationBase> {

    public static final String PREFIX = "/citation";
    public static final String CITATION_ATTR = "citation";
    public static final String SOURCE_ATTR = "source";
    public static final String CITATIONS_ATTR = "citations";


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
                              BindingResult result, Model model) {
        if (result.hasErrors()) {
            return String.format("%s/add-citation.html", PREFIX);
        }

        CitationBase created = entityRepo.save(citation);

        model.addAttribute(CITATION_ATTR, created);
        model.addAttribute(REVIEW_ATTR, new Review());
        model.addAttribute(SOURCE_ATTR, new SourceRef());
        return String.format("%s/update-citation.html", PREFIX);
    }

    @GetMapping("/delete/{id}")
    @Secured("ROLE_MEMBER")
    public String deleteCitation(@PathVariable("id") @Valid Long id) {
        CitationBase citation = entityRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid citation id"));
        // TODO: set inactive
        entityRepo.delete(citation);
        return String.format("redirect:%s/all", PREFIX);
    }

    @GetMapping("/view/{id}")
    public String viewCitation(@PathVariable("id") @Valid Long id, Model model) {
        CitationBase citation = entityRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid citation id"));
        model.addAttribute(CITATION_ATTR, citation);
        return String.format("%s/view-citation.html", PREFIX);
    }

    @GetMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    public String getCitation(@PathVariable("id")  Long id, Model model) {
        CitationBase citation = entityRepo.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Invalid citation id"));
        model.addAttribute(CITATION_ATTR, citation);
        model.addAttribute(REVIEW_ATTR, new Review());
        model.addAttribute(SOURCE_ATTR, new SourceRef());
        return String.format("%s/update-citation.html", PREFIX);
    }

    @PostMapping("/details/{id}")
    @Secured("ROLE_MEMBER")
    public String updateCitation(@PathVariable("id") @Valid Long id,
                                    @Valid @ModelAttribute(CITATION_ATTR) CitationBase citation,
                                    BindingResult result, Model model) {
        if (! result.hasErrors()) {
            CitationBase current = entityRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid citation id"));
            current.edit(citation);
            entityRepo.save(current);
            model.addAttribute(CITATION_ATTR, current);
            return String.format("redirect:%s/all", PREFIX);
        }
        return String.format("%s/update-citation.html", PREFIX);
    }

    @PostMapping("/review/{id}")
    @Secured("ROLE_USER")
    @Transactional
    public String reviewCitation(@PathVariable("id") @Valid Long id,
                                   @Valid @ModelAttribute(REVIEW_ATTR) Review review,
                                   BindingResult result, Authentication authentication, Model model) {

        CitationBase current = entityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Citation id"));

        if ( ! result.hasFieldErrors("rating")) {
            current = reviewEntity(current, review, authentication);
            model.addAttribute(CITATION_ATTR, current);
            model.addAttribute(REVIEW_ATTR, new Review());
        }

        return String.format("redirect:%s/details/%s", PREFIX, current.getId());
    }

    @PostMapping("/source/{id}")
    @Secured("ROLE_USER")
    @Transactional
    public String addSource(@PathVariable("id") @Valid Long id, @ModelAttribute(SOURCE_ATTR) SourceRef source,
                            BindingResult result, Model model) {

        CitationBase current = entityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Citation id"));

        try {
            SourceRef toAdd = new SourceRef(source.getSrcEntityId(), current, source.getLink(), source.getDescription());
            current.addSource(toAdd);
            model.addAttribute(CITATION_ATTR, current);
            model.addAttribute(SOURCE_ATTR, new SourceRef());
        } catch (IllegalArgumentException e) {
            result.addError(new ObjectError("link",
                    "Please provide either a link, a source or both"));
        }
        return String.format("redirect:%s/details/%s", PREFIX, current.getId());
    }
    // TODO: remove/edit source methods

    // TODO: which methods need to be declared transactional?

    // TODO: is it  necessary to add empty objects to the model?
}