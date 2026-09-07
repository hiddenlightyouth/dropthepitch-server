package kr.yuns.dropthepitchserver.ai.service;

import kr.yuns.dropthepitchserver.ai.data.entity.AiUsage;
import kr.yuns.dropthepitchserver.ai.data.repository.AiUsageRepository;
import kr.yuns.dropthepitchserver.ai.service.dto.SaveAiUsageCommand;
import kr.yuns.dropthepitchserver.project.data.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {
    private final AiUsageRepository aiUsageRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public void saveAiUsage(SaveAiUsageCommand command) {
        AiUsage usage = AiUsage.builder()
                .project(projectRepository.getReferenceById(command.projectId()))
                .model(command.model())
                .purpose(command.purpose())
                .inputTokens(command.inputTokens())
                .outputTokens(command.outputTokens())
                .build();
        aiUsageRepository.save(usage);
        log.info("[saveAiUsage] AI 사용량 저장 완료, 목적: {}, INPUT: {}, OUTPUT: {}",
                usage.getPurpose(), usage.getInputTokens(), usage.getOutputTokens());
    }
}
