package org.example.blog_spring.web.rest;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import org.example.blog_spring.dto.UserDto;
import org.example.blog_spring.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getUsers_returnsPagedUsersWrappedInApiResponse() throws Exception {
        var user = new UserDto(
                1L,
                "jdoe",
                "jdoe@example.com",
                "John Doe",
                Instant.now(),
                Instant.now()
        );

        Page<UserDto> page =
                new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1);

        given(userService.getUsers(PageRequest.of(0, 20))).willReturn(page);

        mockMvc.perform(get("/api/users?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].username").value("jdoe"));
    }
}

