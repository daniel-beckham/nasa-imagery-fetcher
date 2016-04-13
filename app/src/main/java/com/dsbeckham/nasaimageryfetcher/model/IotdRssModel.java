package com.dsbeckham.nasaimageryfetcher.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "rss")
public class IotdRssModel
{
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Element(name = "channel")
    private Channel channel;

    public static class Channel {
        @ElementList(name="item", inline = true)
        List<Item> items;

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

        public static class Item {
            @Element(name = "title")
            private String title;
            @Element(name = "link")
            private String link;
            @Element(name = "description")
            private String description;
            @Element(name = "enclosure")
            private Enclosure enclosure;

            public static class Enclosure {
                @Attribute(name = "url")
                private String url;

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }
            }

            @Element(name = "pubDate")
            private String pubDate;

            public String getTitle()
            {
                return title;
            }

            public void setTitle(String title)
            {
                this.title = title;
            }

            public String getLink()
            {
                return link;
            }

            public void setLink(String link)
            {
                this.link = link;
            }

            public String getDescription()
            {
                return description;
            }

            public void setDescription(String description)
            {
                this.description = description;
            }

            public Enclosure getEnclosure()
            {
                return enclosure;
            }

            public void setEnclosure(Enclosure enclosure)
            {
                this.enclosure = enclosure;
            }

            public String getPubDate()
            {
                return pubDate;
            }

            public void setPubDate(String pubDate)
            {
                this.pubDate = pubDate;
            }

            @Override
            public boolean equals(Object object) {
                return this == object || (!(object == null || getClass() != object.getClass()) && pubDate.equals(((Item) object).pubDate));
            }
        }
    }
}
