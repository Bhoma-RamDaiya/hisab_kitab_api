package com.hisabkitab.backend.user.interfaces;
import com.hisabkitab.backend.user.domain.Gender;
import lombok.Data;

import java.util.Date;

@Data
public class SignupRequest {
    private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private Date dateOfBirth;
    private Gender gender;
}