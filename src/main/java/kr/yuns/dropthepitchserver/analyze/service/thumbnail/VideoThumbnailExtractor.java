package kr.yuns.dropthepitchserver.analyze.service.thumbnail;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

//영상에서 한 프레임을 뽑는다.
//ffmpeg 바이너리가 의존성(org.bytedeco:ffmpeg)에 들어있어 서버에 따로 설치할 것이 없다.
//순수 자바 디코더(JCodec)도 시도했으나 우리 영상 두 개를 모두 읽지 못해 ffmpeg를 쓴다.
@Component
@Slf4j
public class VideoThumbnailExtractor {

    /**
     * 영상 중간 지점의 한 프레임을 썸네일로 만듭니다.
     *
     * @param bytes 영상 내용
     * @return JPG 바이트
     */
    public byte[] extract(byte[] bytes) throws Exception {
        //파일이 아니라 메모리에서 바로 읽는다. 임시 파일을 만들고 지울 필요가 없다.
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
