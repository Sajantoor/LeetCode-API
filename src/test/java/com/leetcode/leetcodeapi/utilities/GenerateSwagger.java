package com.leetcode.leetcodeapi.utilities;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
public class GenerateSwagger {
    @Autowired
    WebApplicationContext context;

    @Test
    public void generateSwagger() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockMvc.perform(MockMvcRequestBuilders.get("/v2/api-docs").accept(MediaType.APPLICATION_JSON))
                .andDo((result) -> {
                    ObjectMapper mapper = new ObjectMapper();
                    Object json = mapper.readValue(result.getResponse().getContentAsString(), Object.class);
                    mapper.writerWithDefaultPrettyPrinter().writeValue(new File("swagger.json"), json);
                });

    }
}