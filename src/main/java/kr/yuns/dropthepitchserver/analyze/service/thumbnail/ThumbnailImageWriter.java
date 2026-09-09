package kr.yuns.dropthepitchserver.analyze.service.thumbnail;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

//PDF와 영상에서 뽑아낸 그림을 같은 규격으로 맞춘다.
//원본 그대로 두면 영상 한 프레임이 PNG로 560KB까지 나오는데, 화면에서는 작은 사각형으로만 보인다.
//가로 640px JPG로 줄이면 20KB 안팎이 된다.
public class ThumbnailImageWriter {

    public static final String CONTENT_TYPE = "image/jpeg";
    public static final String EXTENSION = "jpg";

    private static final int TARGET_WIDTH = 640;
    private static final String FORMAT = "JPG";

    /**
     * 그림을 썸네일 규격(가로 640px JPG)으로 바꿉니다.
     *
     * @param source 원본 그림
     * @return JPG 바이트
     */
    public static byte[] toThumbnail(BufferedImage source) throws IOException {
        BufferedImage resized = resize(source);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        //쓸 수 있는 인코더가 없으면 예외 없이 false를 돌려주고 빈 배열이 나간다. 여기서 막는다.
        if (!ImageIO.write(resized, FORMAT, out)) {
            throw new IOException("JPG 인코더를 찾지 못했습니다.");
        }
        return out.toByteArray();
    }

    //JPG는 투명도를 담지 못한다. TYPE_INT_RGB에 다시 그려서 알파 채널을 없앤다.
    //이 과정을 건너뛰면 투명한 PDF나 영상 프레임이 저장 시점에 깨진다.
    private static BufferedImage resize(BufferedImage source) {
        boolean alreadySmall = source.getWidth() <= TARGET_WIDTH;

        int width = alreadySmall ? source.getWidth() : TARGET_WIDTH;
        int height = alreadySmall
                ? source.getHeight()
                : Math.round(source.getHeight() * (TARGET_WIDTH / (float) source.getWidth()));

        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();

        try {
            graphics.drawImage(source.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        } finally {
            //Graphics는 네이티브 자원을 잡는다. 반납하지 않으면 요청이 쌓일수록 새어나간다.
            graphics.dispose();
        }
        return resized;
    }
}
