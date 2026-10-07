package kr.yuns.dropthepitchserver.admin.controller;

import kr.yuns.dropthepitchserver.admin.service.AdminCreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/credits")
@RequiredArgsConstructor
public class AdminCreditController {
    private final AdminCreditService adminCreditService;
}
