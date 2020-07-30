package org.informiz.ctrl.hypothesis;

import org.informiz.model.HypothesisBase;
import org.informiz.repo.hypothesis.HypothesisRepository;
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
@RequestMapping(path = HypothesisRestController.PREFIX)
public class HypothesisRestController {

    public static final String PREFIX = "/claim-api";

    @Autowired
    private HypothesisRepository hypothesisRepo;

    @GetMapping(path = {"/", "/all"})
    @ResponseBody
    public List<HypothesisBase> getAllClaims() {
        return StreamSupport
                .stream(hypothesisRepo.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @GetMapping(path = {"/", "/claim"})
    @ResponseBody
    public HypothesisBase getHypothesis(@RequestParam String entityId) {
        return hypothesisRepo.findByEntityId(entityId);
    }

}
