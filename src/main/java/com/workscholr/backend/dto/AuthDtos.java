package com.workscholr.backend.dto;

import com.workscholr.backend.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank @Size(min = 4, max = 100) String fullName,
            @NotBlank @Email String email,
            @NotBlank
            @Size(min = 8, message = "Password must have at least 8 characters")
            @Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).+$",
                    message = "Password must include uppercase, lowercase, number, and special character"
            )
            String password
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {
    }

    public record AuthResponse(
            String token,
            Long userId,
            String fullName,
            String email,
            Role role
    ) {
    }
}
