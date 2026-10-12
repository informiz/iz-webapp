package org.informiz.ctrl.checker;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.api.client.util.Lists;
import org.informiz.model.FactCheckerBase;
import org.informiz.model.Utils;
import org.informiz.repo.checker.FactCheckerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = CheckerRestController.CHECKER_API_PREFIX)
public class CheckerRestController {

    public static final String CHECKER_API_PREFIX = "/checker-api";

    private final FactCheckerRepository checkerRepo;

    @Autowired
    public CheckerRestController(FactCheckerRepository checkerRepo) {
        this.checkerRepo = checkerRepo;
    }

    @GetMapping("/all")
    @JsonView(Utils.Views.EntityDefaultView.class)
    public List<FactCheckerBase> getAllCheckers() {
        return Lists.newArrayList(checkerRepo.findAll());
    }

    @GetMapping("/{entityId}")
    public Boolean isMember(@PathVariable("entityId") String entityId) {
        return checkerRepo.isMember(entityId);
    }
}

