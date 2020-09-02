package org.informiz.ctrl.hypothesis;

import org.informiz.model.HypothesisBase;
import org.informiz.repo.hypothesis.HypothesisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(path = HypothesisRestController.PREFIX)
@CrossOrigin(origins = "${iz.webapp.url}")
public class HypothesisRestController {

    public static final String PREFIX = "/claim-api";

    @Autowired
    private HypothesisRepository hypothesisRepo;

    @GetMapping(path = {"/", "/all"})
    public List<HypothesisBase> getAllClaims() {
        return StreamSupport
                .stream(hypothesisRepo.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @GetMapping(path = {"/", "/claim"})
    public HypothesisBase getHypothesis(@RequestParam String entityId) {
        return hypothesisRepo.findByEntityId(entityId);
    }

}
