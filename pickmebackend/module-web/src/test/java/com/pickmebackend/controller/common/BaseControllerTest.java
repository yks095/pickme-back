package com.pickmebackend.controller.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickmebackend.config.jwt.JwtProvider;
import com.pickmebackend.domain.Account;
import com.pickmebackend.domain.Enterprise;
import com.pickmebackend.domain.dto.enterprise.EnterpriseRequestDto;
import com.pickmebackend.domain.enums.UserRole;
import com.pickmebackend.properties.AppProperties;
import com.pickmebackend.repository.AccountRepository;
import com.pickmebackend.repository.EnterpriseRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    protected JwtProvider jwtProvider;

    @Autowired
    protected AppProperties appProperties;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected EnterpriseRepository enterpriseRepository;

    protected String jwt;

    protected Account createAccount() {
        Account account = Account.builder()
                .email(appProperties.getTestEmail())
                .password(appProperties.getTestPassword())
                .nickName(appProperties.getTestNickname())
                .createdAt(LocalDateTime.now())
                .career("신입")
                .positions(new HashSet<>(Arrays.asList("BackEnd", "FrontEnd")))
                .userRole(UserRole.USER)
                .build();
        return accountRepository.save(account);
    }

    protected Account createAnotherAccount() {
        Account account = Account.builder()
                .email(appProperties.getTestAnotherEmail())
                .password(appProperties.getTestPassword())
                .nickName(appProperties.getTestAnotherNickname())
                .createdAt(LocalDateTime.now())
                .career("5년차 이상")
                .positions(new HashSet<>(Arrays.asList("Designer")))
                .userRole(UserRole.USER)
                .build();
        return accountRepository.save(account);
    }

    protected void createAccounts(int i) {
        Account account = Account.builder()
                .email(i + appProperties.getTestEmail())
                .password(i + appProperties.getTestPassword())
                .nickName(i + appProperties.getTestNickname())
                .oneLineIntroduce("한 줄 소개")
                .createdAt(LocalDateTime.now())
                .career(i + "년차")
                .positions(new HashSet<>(Collections.singletonList("개발자" + i)))
                .userRole(UserRole.USER)
                .build();
        accountRepository.save(account);
    }

    protected EnterpriseRequestDto createEnterpriseDto() {
        EnterpriseRequestDto enterpriseRequestDto =
                EnterpriseRequestDto.builder()
                .email(appProperties.getTestEmail())
                .password(appProperties.getTestPassword())
                .registrationNumber(appProperties.getTestRegistrationNumber())
                .name(appProperties.getTestName())
                .address(appProperties.getTestAddress())
                .ceoName(appProperties.getTestCeoName())
                .build();
        Enterprise enterprise = modelMapper.map(enterpriseRequestDto, Enterprise.class);

        Account account = modelMapper.map(enterpriseRequestDto, Account.class);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setCreatedAt(LocalDateTime.now());
        account.setUserRole(UserRole.ENTERPRISE);
        account.setEnterprise(enterprise);

        enterprise.setAccount(account);

        accountRepository.save(account);
        enterpriseRepository.save(enterprise);

        return enterpriseRequestDto;
    }

    protected EnterpriseRequestDto createAnotherEnterpriseDto() {
        EnterpriseRequestDto enterpriseRequestDto =
                EnterpriseRequestDto.builder()
                        .email("another" + appProperties.getTestEmail())
                        .password("another" + appProperties.getTestPassword())
                        .registrationNumber("another" + appProperties.getTestRegistrationNumber())
                        .name("another" + appProperties.getTestName())
                        .address("another" + appProperties.getTestAddress())
                        .ceoName("another" + appProperties.getTestCeoName())
                        .build();
        Enterprise enterprise = modelMapper.map(enterpriseRequestDto, Enterprise.class);

        Account account = modelMapper.map(enterpriseRequestDto, Account.class);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setCreatedAt(LocalDateTime.now());
        account.setUserRole(UserRole.ENTERPRISE);
        account.setEnterprise(enterprise);

        enterprise.setAccount(account);

        accountRepository.save(account);
        enterpriseRepository.save(enterprise);

        return enterpriseRequestDto;
    }

    protected String createEnterpriseJwt() {
        EnterpriseRequestDto enterpriseRequestDto = createAnotherEnterpriseDto();
        Optional<Account> accountOptional = accountRepository.findByEmail(enterpriseRequestDto.getEmail());
        Account account = accountOptional.get();

        return "Bearer " + jwtProvider.generateToken(account);
    }

    protected String createAccountJwt() {
        Account newAccount = createAnotherAccount();
        return "Bearer " + jwtProvider.generateToken(newAccount);
    }
}
