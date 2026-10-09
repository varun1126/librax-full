package com.librax.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Flattened, UI-friendly shape produced by GoogleBooksService from the
 * raw GoogleVolumeDto. This is what the search UI and the import mapper
 * consume; repository code never sees this type.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleBookDto {
    private String id;
    private String volumeId;
    private String title;
    private String subtitle;
    private String authorNames; // comma-joined
    private String publisher;
    private String publishedDate;
    private String description;
    private String isbn;
    private Integer pageCount;
    private String category;
    private String thumbnailUrl;

    public String getId() {
        return id != null ? id : (volumeId != null ? volumeId : isbn);
    }
    public void setId(String id) {
        this.id = id;
        if (this.volumeId == null) this.volumeId = id;
    }

    public String getVolumeId() {
        return volumeId != null ? volumeId : id;
    }
    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
        if (this.id == null) this.id = volumeId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getAuthorNames() { return authorNames; }
    public void setAuthorNames(String authorNames) { this.authorNames = authorNames; }

    public String getAuthors() { return authorNames; }
    public void setAuthors(String authors) { this.authorNames = authors; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getPublishedDate() { return publishedDate; }
    public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getThumbnail() { return thumbnailUrl; }
    public void setThumbnail(String thumbnail) { this.thumbnailUrl = thumbnail; }

    @Override
    public String toString() {
        return title + (authorNames != null && !authorNames.isBlank() ? " — " + authorNames : "");
    }
}
