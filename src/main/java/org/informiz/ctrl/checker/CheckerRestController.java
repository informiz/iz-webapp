package org.informiz.ctrl.checker;

import org.informiz.model.FactCheckerBase;
import org.informiz.repo.checker.FactCheckerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(path = CheckerRestController.PREFIX)
public class CheckerRestController {

    public static final String PREFIX = "/checker-api";

    private final FactCheckerRepository checkerRepo;

    @Autowired
    public CheckerRestController(FactCheckerRepository checkerRepo) {
        this.checkerRepo = checkerRepo;
    }

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

