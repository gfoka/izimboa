package com.izimboa.delivery;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.izimboa.common.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DeliveryZonesController.class)
@Import(SecurityConfig.class)
class DeliveryZonesControllerTest {

    @Autowired MockMvc mvc;

    @Test
    void lesZonesSontPubliques() throws Exception {
        mvc.perform(get("/v1/delivery/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Douala"));
    }
}
