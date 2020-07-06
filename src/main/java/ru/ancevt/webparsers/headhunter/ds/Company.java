package ru.ancevt.webparsers.headhunter.ds;

import ru.ancevt.util.string.ToStringBuilder;
import ru.ancevt.webdatagrabber.ds.IEntity;
import ru.ancevt.webdatagrabber.ds.Image;

/**
 * @author ancevt
 */
public class Company implements IEntity {

    private final long id;
    private final String type;
    private final String name;
    private final String info;
    private final String infoHtml;
    private final String area;
    private final String pageUrl;
    private final String webSite;
    private final boolean trusted;
    private final Image image;

    public Company(
            int id,
            String type,
            String name,
            String info,
            String infoHtml,
            String area,
            String pageUrl,
            String webSite,
            boolean trusted,
            Image image
    ) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.info = info;
        this.infoHtml = infoHtml;
        this.area = area;
        this.pageUrl = pageUrl;
        this.webSite = webSite;
        this.trusted = trusted;
        this.image = image;
    }

    public Image getImage() {
        return image;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public String getInfoHtml() {
        return infoHtml;
    }

    public String getArea() {
        return area;
    }

    public String getWebSite() {
        return webSite;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, true)
                .appendAll("id", "type", "name", "info", "area", "pageUrl", "webSite", "image", "trusted")
                .build();
    }

    @Override
    public String getShortDisplayName() {
        return String.format("%d %s", id, name);
    }

}
