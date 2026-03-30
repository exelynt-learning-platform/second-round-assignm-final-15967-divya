package com.example.demo.Mappers;

import org.mapstruct.Mapper;

import com.example.demo.DTO.RegisterRequest;
import com.example.demo.Entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

	User toUser(RegisterRequest request);
}