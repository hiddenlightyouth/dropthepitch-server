package kr.yuns.dropthepitchserver.analyze.service.thumbnail;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

@Component
@Slf4j
public class VideoThumbnailExtractor {

    @PostConstruct
    void configureFfmpegLog() {
        FFmpegLogCallback.set();
        FFmpegLogCallback.setLevel(avutil.AV_LOG_ERROR);
    }

    /**
     * 영상 중간 지점의 한 프레임을 썸네일로 만듭니다.
     *
     * @param bytes 영상 내용
     * @return JPG 바이트
     */
    public byte[] extract(byte[] bytes) throws Exception {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(new ByteArrayInputStream(bytes))) {
            grabber.start();

            //도입부는 로고나 자막만 나오는 경우가 많아 내용을 알아보기 어렵다. 중간을 쓴다.
            grabber.setTimestamp(grabber.getLengthInTime() / 2);

            try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
                Frame frame = grabber.grabImage();

                if (frame == null) {
                    log.warn("[extract] 영상에서 프레임을 얻지 못했습니다.");
                    return null;
                }

                return ThumbnailImageWriter.toThumbnail(converter.getBufferedImage(frame));
            }
        }
    }
}
