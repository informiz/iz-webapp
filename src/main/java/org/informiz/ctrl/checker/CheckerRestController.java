package org.informiz.ctrl.checker;

import org.informiz.model.FactCheckerBase;
import org.informiz.model.SourceBase;
import org.informiz.repo.checker.FactCheckerRepository;
import org.informiz.repo.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(path = CheckerRestController.PREFIX)
@CrossOrigin(origins = "${iz.webapp.url}")
public class CheckerRestController {

    public static final String PREFIX = "/checker-api";

    @Autowired
    private FactCheckerRepository checkerRepo;

    @GetMapping(path = {"/", "/all"})
    public List<FactCheckerBase> getAllCheckers() {
        return StreamSupport
                .stream(checkerRepo.findAll().spliterator(), false)
                .map(checker -> { checker.setEmail(null); return checker; }) // do not expose email
                .collect(Collectors.toList());
    }

    @GetMapping(path = {"/", "/checker"})
    public FactCheckerBase getChecker(@RequestParam String enntityId) {
        return checkerRepo.findByEntityId(enntityId);
    }

}

