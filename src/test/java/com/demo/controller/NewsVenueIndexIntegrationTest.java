package com.demo.controller;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NewsVenueIndexIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Tag("P0")
    void itHome01_index_shouldContainHomeModelAttributes() throws Exception {
        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("news_list"))
                .andExpect(model().attributeExists("venue_list"))
                .andExpect(model().attributeExists("message_list"));
    }

    @Test
    @Tag("P1")
    void itHome02_adminIndex_shouldReturnAdminIndexView() throws Exception {
        mockMvc.perform(get("/admin_index"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin_index"));
    }

    @Test
    @Tag("P1")
    void itNews01_newsListPage_shouldReturnViewAndTotal() throws Exception {
        mockMvc.perform(get("/news_list"))
                .andExpect(status().isOk())
                .andExpect(view().name("news_list"))
                .andExpect(model().attributeExists("news_list"))
                .andExpect(model().attributeExists("total"));
    }

    @Test
    @Tag("P1")
    void itNews02_newsGetList_shouldReturnPageJson() throws Exception {
        mockMvc.perform(get("/news/getNewsList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(5)));
    }

    @Test
    @Tag("P1")
    void itNews03_newsDetail_shouldReturnNewsView() throws Exception {
        mockMvc.perform(get("/news").param("newsID", "12"))
                .andExpect(status().isOk())
                .andExpect(view().name("news"))
                .andExpect(model().attributeExists("news"));
    }

    @Test
    @Tag("P2")
    void itNews04_newsGetListPageZero_shouldReturn5xx() throws Exception {
        mockMvc.perform(get("/news/getNewsList").param("page", "0"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @Tag("P1")
    void itVenue01_venueListPage_shouldReturnViewAndTotal() throws Exception {
        mockMvc.perform(get("/venue_list"))
                .andExpect(status().isOk())
                .andExpect(view().name("venue_list"))
                .andExpect(model().attributeExists("venue_list"))
                .andExpect(model().attributeExists("total"));
    }

    @Test
    @Tag("P1")
    void itVenue02_venueGetList_shouldReturnPageJson() throws Exception {
        mockMvc.perform(get("/venuelist/getVenueList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(5)));
    }

    @Test
    @Tag("P1")
    void itVenue03_venueDetail_shouldReturnVenueView() throws Exception {
        mockMvc.perform(get("/venue").param("venueID", "16"))
                .andExpect(status().isOk())
                .andExpect(view().name("venue"))
                .andExpect(model().attributeExists("venue"));
    }

    @Test
    @Tag("P2")
    void itVenue04_venueGetListPageZero_shouldReturn5xx() throws Exception {
        mockMvc.perform(get("/venuelist/getVenueList").param("page", "0"))
                .andExpect(status().is5xxServerError());
    }
}
