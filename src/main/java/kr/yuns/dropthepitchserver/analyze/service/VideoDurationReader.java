package kr.yuns.dropthepitchserver.analyze.service;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

@Component
@Slf4j
public class VideoDurationReader {

    /**
     * 영상 길이를 초 단위로 읽습니다.
     *
     * @param bytes 영상 내용
     * @return 영상 길이(초). 읽지 못하면 null
     */
    public Integer readSeconds(byte[] bytes) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(new ByteArrayInputStream(bytes))) {
            grabber.start();
            long micros = grabber.getLengthInTime();
            return micros > 0 ? (int) Math.max(1, Math.round(micros / 1_000_000.0)) : null;
        } catch (Exception | LinkageError e) {
            //ffmpeg 네이티브 로딩 실패는 Exception이 아니라 LinkageError로 온다.
            log.warn("[readSeconds] 영상 길이를 읽지 못했습니다.", e);
            return null;
        }
    }
}
