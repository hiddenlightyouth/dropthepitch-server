package kr.yuns.dropthepitchserver.admin.controller;

import kr.yuns.dropthepitchserver.admin.service.AdminProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/projects")
@RequiredArgsConstructor
public class AdminProjectController {
    private final AdminProjectService adminProjectService;
}
