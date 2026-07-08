package com.petconnect.auth.application.mappers;

import com.petconnect.auth.application.dto.AuthResponse;
import com.petconnect.auth.domain.AuthUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "role", expression = "java(authUser.getRole().name())")
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "expiresIn", ignore = true)
    AuthResponse toAuthResponse(AuthUser authUser);
}