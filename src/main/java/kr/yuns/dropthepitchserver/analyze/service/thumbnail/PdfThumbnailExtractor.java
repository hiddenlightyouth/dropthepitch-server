package kr.yuns.dropthepitchserver.analyze.service.thumbnail;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

//PDF 첫 페이지를 그림으로 만든다. PDFBox는 순수 자바라 서버에 따로 설치할 것이 없다.
@Component
public class PdfThumbnailExtractor {

    //화면의 작은 사각형에 들어갈 그림이라 인쇄 품질(300)까지는 필요 없다.
    private static final float RENDER_DPI = 96f;
    private static final int FIRST_PAGE = 0;

    /**
     * PDF 첫 페이지를 썸네일로 만듭니다.
     *
     * @param bytes PDF 내용
     * @return JPG 바이트
     */
    public byte[] extract(byte[] bytes) throws Exception {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            BufferedImage image = new PDFRenderer(document).renderImageWithDPI(FIRST_PAGE, RENDER_DPI);
            return ThumbnailImageWriter.toThumbnail(image);
        }
    }
}
