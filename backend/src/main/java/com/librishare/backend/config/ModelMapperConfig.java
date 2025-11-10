package com.librishare.backend.config;

import com.librishare.backend.modules.book.dto.BookRequestDTO;
import com.librishare.backend.modules.book.entity.Book;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        PropertyMap<BookRequestDTO, Book> bookCreateMap = new PropertyMap<>() {
            protected void configure() {
                skip(destination.getId());
            }
        };

        modelMapper.addMappings(bookCreateMap);

        return modelMapper;
    }
}