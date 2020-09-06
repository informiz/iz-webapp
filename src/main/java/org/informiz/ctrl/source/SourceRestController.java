package org.informiz.ctrl.source;

import org.informiz.model.SourceBase;
import org.informiz.repo.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(path = SourceRestController.PREFIX)
public class SourceRestController {

    public static final String PREFIX = "/source-api";

    @Autowired
    private SourceRepository sourceRepo;

    @GetMapping(path = {"/", "/all"})
    public List<SourceBase> getAllSources() {
        return StreamSupport
                .stream(sourceRepo.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @GetMapping(path = {"/", "/source"})
    public SourceBase getSource(@RequestParam String enntityId) {
        return sourceRepo.findByEntityId(enntityId);
    }

}
