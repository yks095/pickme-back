package com.pickmebackend.domain.dto;

import lombok.*;
import org.springframework.lang.Nullable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AccountDto {

    @Email @NotBlank
    private String email;

    @NotBlank @Size(min = 8, max = 20)
    private String password;

    @NotBlank
    private String nickName;

    @Nullable
    private List<String> technology = new ArrayList<>();

    @Nullable
    private String oneLineIntroduce;
}
