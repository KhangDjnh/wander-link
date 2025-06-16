package com.khangdev.identity_service.mapper;

import com.khangdev.identity_service.dto.request.UserRequestDTO;
import com.khangdev.identity_service.dto.response.UserResponse;
import com.khangdev.identity_service.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserRequestDTO request);

    UserResponse toUserResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromRequest(@MappingTarget User user, UserRequestDTO request);
}

