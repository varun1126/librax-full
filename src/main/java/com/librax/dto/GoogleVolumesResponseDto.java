package com.librax.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleVolumesResponseDto {
    private List<GoogleVolumeDto> items;

    public List<GoogleVolumeDto> getItems() { return items; }
    public void setItems(List<GoogleVolumeDto> items) { this.items = items; }
}
