package org.informiz.ctrl.informi;

import org.informiz.auth.AuthUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

@Controller
@RequestMapping(path = InformiRestController.PREFIX)
public class InformiRestController {

    public static final String PREFIX = "/informi-api";

/*
    @Autowired
    private InformiRepository informiRepo;
*/

    @PostMapping(path = {"/upload"})
    @ResponseBody
    public Map<String, String> uploadInformiMedia(@RequestParam(value="file") MultipartFile file) {
        if (file != null && ! file.isEmpty()) {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            try (InputStream inStream = file.getInputStream()) {
                // TODO: reprocess images, random filename
                String path = AuthUtils.uploadMedia(inStream, fileName);
                return Collections.singletonMap("mediaPath", path);
            } catch (IOException e) {
                // TODO: implement @ControllerAdvice
                e.printStackTrace();
            }
        }
        return Collections.singletonMap("error", "Failed to upload file");
    }

}
