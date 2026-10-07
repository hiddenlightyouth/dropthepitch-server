package kr.yuns.dropthepitchserver.admin.controller;

import kr.yuns.dropthepitchserver.admin.service.AdminPersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/personas")
@RequiredArgsConstructor
public class AdminPersonaController {
    private final AdminPersonaService adminPersonaService;
}
