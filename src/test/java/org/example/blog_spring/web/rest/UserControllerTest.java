package org.example.blog_spring.web.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.List;
import org.example.blog_spring.dto.ApiResponse;
import org.example.blog_spring.dto.UserDto;
import org.example.blog_spring.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class UserControllerTest {

    private final UserService userService = Mockito.mock(UserService.class);

    private final UserController controller = new UserController(userService);

    @Test
    void getUsers_returnsPagedUsersWrappedInApiResponse() {
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

        given(userService.getUsers(Mockito.any(Pageable.class))).willReturn(page);

        var responseEntity = controller.getUsers(PageRequest.of(0, 20));
        assertEquals(200, responseEntity.getStatusCode().value());

        ApiResponse<Page<UserDto>> body = responseEntity.getBody();
        assertEquals("Users retrieved successfully", body.message());
        assertEquals("jdoe", body.data().getContent().getFirst().username());
    }
}

