package org.informiz.ctrl.citation;

import org.informiz.model.CitationBase;
import org.informiz.repo.citation.CitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
@RequestMapping(path = CitationRestController.PREFIX)
public class CitationRestController {

    public static final String PREFIX = "/citation-api";

    @Autowired
    private CitationRepository citationRepo;

    @GetMapping(path = {"/", "/all"})
    @ResponseBody
    public List<CitationBase> getAllClaims() {
        return StreamSupport
                .stream(citationRepo.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @GetMapping(path = {"/", "/citation"})
    @ResponseBody
    public CitationBase getCitation(@RequestParam String entityId) {
        return citationRepo.findByEntityId(entityId);
    }

}
