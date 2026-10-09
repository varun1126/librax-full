package com.librax.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Near-1:1 mirror of a single Google Books API "volume" JSON object. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleVolumeDto {
    private String id;

    @JsonProperty("volumeInfo")
    private VolumeInfo volumeInfo;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public VolumeInfo getVolumeInfo() { return volumeInfo; }
    public void setVolumeInfo(VolumeInfo volumeInfo) { this.volumeInfo = volumeInfo; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VolumeInfo {
        private String title;
        private String subtitle;
        private java.util.List<String> authors;
        private String publisher;
        private String publishedDate;
        private String description;
        private Integer pageCount;
        private java.util.List<String> categories;
        private java.util.List<IndustryIdentifier> industryIdentifiers;
        private ImageLinks imageLinks;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getSubtitle() { return subtitle; }
        public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
        public java.util.List<String> getAuthors() { return authors; }
        public void setAuthors(java.util.List<String> authors) { this.authors = authors; }
        public String getPublisher() { return publisher; }
        public void setPublisher(String publisher) { this.publisher = publisher; }
        public String getPublishedDate() { return publishedDate; }
        public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getPageCount() { return pageCount; }
        public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
        public java.util.List<String> getCategories() { return categories; }
        public void setCategories(java.util.List<String> categories) { this.categories = categories; }
        public java.util.List<IndustryIdentifier> getIndustryIdentifiers() { return industryIdentifiers; }
        public void setIndustryIdentifiers(java.util.List<IndustryIdentifier> industryIdentifiers) { this.industryIdentifiers = industryIdentifiers; }
        public ImageLinks getImageLinks() { return imageLinks; }
        public void setImageLinks(ImageLinks imageLinks) { this.imageLinks = imageLinks; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IndustryIdentifier {
        private String type;
        private String identifier;
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageLinks {
        private String thumbnail;
        public String getThumbnail() { return thumbnail; }
        public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    }
}
