package kr.yuns.dropthepitchserver.admin.controller;

import kr.yuns.dropthepitchserver.admin.service.AdminAiUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/ai-usage")
@RequiredArgsConstructor
public class AdminAiUsageController {
    private final AdminAiUsageService adminAiUsageService;
}
