package com.workscholr.backend.config;

import com.workscholr.backend.dto.AuthDtos.RegisterRequest;
import com.workscholr.backend.dto.JobDtos.CreateJobRequest;
import com.workscholr.backend.model.JobOpportunity;
import com.workscholr.backend.model.User;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        Converter<RegisterRequest, User> registerRequestConverter = context -> {
            RegisterRequest source = context.getSource();
            if (source == null) {
                return null;
            }

            User destination = context.getDestination() == null ? new User() : context.getDestination();
            destination.setFullName(source.fullName());
            destination.setEmail(source.email());
            destination.setPassword(source.password());
            return destination;
        };

        Converter<CreateJobRequest, JobOpportunity> createJobRequestConverter = context -> {
            CreateJobRequest source = context.getSource();
            if (source == null) {
                return null;
            }

            JobOpportunity destination = context.getDestination() == null ? new JobOpportunity() : context.getDestination();
            destination.setTitle(source.title());
            destination.setDepartment(source.department());
            destination.setDescription(source.description());
            destination.setHoursPerWeek(source.hoursPerWeek());
            destination.setMonthlyStipend(source.monthlyStipend());
            destination.setLocation(source.location());
            return destination;
        };

        modelMapper.addConverter(registerRequestConverter, RegisterRequest.class, User.class);
        modelMapper.addConverter(createJobRequestConverter, CreateJobRequest.class, JobOpportunity.class);

        return modelMapper;
    }
}
