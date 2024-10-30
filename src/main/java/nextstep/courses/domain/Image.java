package nextstep.courses.domain;

import java.util.Objects;

public class Image {
    private Long id;
    private ImageSize imageSize;
    private ImageType imageType;
    private ImagePixel imagePixel;

    public Image() {
        this(null, null, null, null);
    }

    public Image(Long id, ImageSize imageSize, ImageType imageType, ImagePixel imagePixel) {
        this.id = id;
        this.imageSize = imageSize;
        this.imageType = imageType;
        this.imagePixel = imagePixel;
    }

    public Long getId() {
        return id;
    }

    public ImageSize getImageSize() {
        return imageSize;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public ImagePixel getImageProperty() {
        return imagePixel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Image)) {
            return false;
        }
        Image image = (Image) o;
        return Objects.equals(getId(), image.getId()) && Objects.equals(getImageSize(), image.getImageSize()) && getImageType() == image.getImageType() && Objects.equals(getImageProperty(), image.getImageProperty());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getImageSize(), getImageType(), getImageProperty());
    }
}
