package org.informiz.ctrl.checker;

import org.informiz.model.FactCheckerBase;
import org.informiz.model.SourceBase;
import org.informiz.repo.checker.FactCheckerRepository;
import org.informiz.repo.source.SourceRepository;
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
@RequestMapping(path = CheckerRestController.PREFIX)
public class CheckerRestController {

    public static final String PREFIX = "/checker-api";

    @Autowired
    private FactCheckerRepository checkerRepo;

    @GetMapping(path = {"/", "/all"})
    @ResponseBody
    public List<FactCheckerBase> getAllCheckers() {
        return StreamSupport
                .stream(checkerRepo.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @GetMapping(path = {"/", "/source"})
    @ResponseBody
    public FactCheckerBase getChecker(@RequestParam String enntityId) {
        return checkerRepo.findByEntityId(enntityId);
    }

}
