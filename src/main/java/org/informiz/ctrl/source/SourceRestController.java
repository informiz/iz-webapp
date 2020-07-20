package org.informiz.ctrl.source;

import org.informiz.model.SourceBase;
import org.informiz.repo.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
@RequestMapping(path = SourceRestController.PREFIX)
public class SourceRestController {

    public static final String PREFIX = "/source-api";

    @Autowired
    private SourceRepository sourceRepo;

    @GetMapping(path = {"/", "/all"})
    @ResponseBody
    public List<SourceBase> getAllSources() {
        return StreamSupport
                .stream(sourceRepo.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

}
