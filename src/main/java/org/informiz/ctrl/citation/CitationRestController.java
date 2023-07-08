package org.informiz.ctrl.citation;

import com.fasterxml.jackson.annotation.JsonView;
import org.informiz.model.CitationBase;
import org.informiz.model.Utils;
import org.informiz.repo.citation.CitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(path = CitationRestController.PREFIX)
public class CitationRestController {

    public static final String PREFIX = "/citation-api";

    @Autowired
    private CitationRepository citationRepo;

    @GetMapping(path = {"/", "/all"})
    @JsonView(Utils.Views.EntityDefaultView.class)
    public List<CitationBase> getAllCitations() {
        return StreamSupport
                .stream(citationRepo.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @GetMapping(path = {"/", "/citation"})
    public CitationBase getCitation(@RequestParam String entityId) {
        return citationRepo.findByEntityId(entityId);
    }

}
