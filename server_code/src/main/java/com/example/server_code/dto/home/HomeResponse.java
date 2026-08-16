package com.example.server_code.dto.home;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HomeResponse {
    private CityDto city;
    private List<BannerDto> banners = new ArrayList<>();
    private List<CategoryDto> categories = new ArrayList<>();
    private List<HeritageProjectSummaryDto> heritageProjects = new ArrayList<>();
    private List<InheritorSummaryDto> inheritors = new ArrayList<>();
    private List<ProductSummaryDto> products = new ArrayList<>();
    private List<CourseSummaryDto> courses = new ArrayList<>();
    private List<ActivitySummaryDto> activities = new ArrayList<>();
    private List<NewsSummaryDto> news = new ArrayList<>();
}
