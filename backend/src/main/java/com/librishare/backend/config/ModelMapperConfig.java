package com.librishare.backend.config;

import com.librishare.backend.modules.book.dto.BookResponseDTO;
import com.librishare.backend.modules.book.entity.Book;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        modelMapper.typeMap(Book.class, BookResponseDTO.class).addMappings(mapper -> {
            mapper.map(src -> src.getUser().getId(), BookResponseDTO::setUserId);
        });

        return modelMapper;
    }
}

