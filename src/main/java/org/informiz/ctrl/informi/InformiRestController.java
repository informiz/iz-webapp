package org.informiz.ctrl.informi;

import org.informiz.model.InformiBase;
import org.informiz.repo.informi.InformiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(path = InformiRestController.PREFIX)
public class InformiRestController {

    public static final String PREFIX = "/informi-api";

    @Autowired
    private InformiRepository informiRepo;

    @GetMapping(path = {"/", "/all"})
    public List<InformiBase> getAllInformiz() {
        return StreamSupport
                .stream(informiRepo.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @GetMapping(path = {"/", "/informi"})
    public InformiBase getInformi(@RequestParam String entityId) {
        return informiRepo.findByEntityId(entityId);
    }

}
