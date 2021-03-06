package com.pickmebackend.controller;

import com.pickmebackend.controller.common.BaseControllerTest;
import com.pickmebackend.domain.Account;
import com.pickmebackend.domain.Experience;
import com.pickmebackend.domain.dto.experience.ExperienceRequestDto;
import com.pickmebackend.repository.ExperienceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExperienceControllerTest extends BaseControllerTest {

    @Autowired
    ExperienceRepository experienceRepository;

    private final String experienceUrl = "/api/experiences/";

    @AfterEach
    void setUp() {
        experienceRepository.deleteAll();
        accountRepository.deleteAll();
        enterpriseRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적으로 경력 생성하기")
    void saveExperience() throws Exception {
        jwt = generateBearerToken();

        String companyName = "D&D 주식회사";
        String description = "D&D 주식회사에서 Spring Boot를 사용해 백엔드 개발을 맡았습니다.";
        String position = "백엔드 개발자";
        LocalDate joinedAt = LocalDate.of(2019, 12, 25);
        LocalDate retiredAt = LocalDate.of(2020, 1, 22);

        ExperienceRequestDto experienceRequestDto = ExperienceRequestDto.builder()
                                                .companyName(companyName)
                                                .description(description)
                                                .position(position)
                                                .joinedAt(joinedAt)
                                                .retiredAt(retiredAt)
                                                .build();

        mockMvc.perform(post(experienceUrl)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(experienceRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("companyName").value(companyName))
                .andExpect(jsonPath("description").value(description))
                .andExpect(jsonPath("position").value(position))
                .andExpect(jsonPath("joinedAt").value(joinedAt.toString()))
                .andExpect(jsonPath("retiredAt").value(retiredAt.toString()))
                .andExpect(jsonPath("account").isNotEmpty())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.update-experience").exists())
                .andExpect(jsonPath("_links.delete-experience").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("create-experience",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("update-experience").description("link to update experience"),
                                linkWithRel("delete-experience").description("link to delete experience"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("Accept Header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Authorization Header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type Header")
                        ),
                        requestFields(
                                fieldWithPath("companyName").description("회사 명"),
                                fieldWithPath("position").description("포지션"),
                                fieldWithPath("joinedAt").description("입사 날짜"),
                                fieldWithPath("retiredAt").description("퇴사 날짜"),
                                fieldWithPath("description").description("경력 설명")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type Header"),
                                headerWithName(HttpHeaders.LOCATION).description("Location Header")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").description("경력 내역 식별자"),
                                fieldWithPath("companyName").description("회사 명"),
                                fieldWithPath("position").description("포지션"),
                                fieldWithPath("joinedAt").description("입사 날짜"),
                                fieldWithPath("retiredAt").description("퇴사 날짜"),
                                fieldWithPath("description").description("경력 설명"),
                                fieldWithPath("account.id").description("경력 내역 등록자 식별자"),
                                fieldWithPath("account.email").description("경력 내역 등록자 이메일"),
                                fieldWithPath("account.nickName").description("경력 내역 등록자 닉네임"),
                                fieldWithPath("account.favorite").description("경력 내역 등록자를 좋아요한 사용자 목록"),
                                fieldWithPath("account.userRole").description("경력 내역 등록자의 권한"),
                                fieldWithPath("account.createdAt").description("경력 내역 등록자의 생성 날짜"),
                                fieldWithPath("account.oneLineIntroduce").description("경력 내역 등록자의 한 줄 소개"),
                                fieldWithPath("account.image").description("경력 내역 등록자의 프로필 이미지"),
                                fieldWithPath("account.enterprise").description("경력 내역 등록자의 기업 정보"),
                                fieldWithPath("account.hits").description("조회 수"),
                                fieldWithPath("account.experiences[*].*").ignored(),
                                fieldWithPath("account.licenses").description("경력 내역 등록자의 자격증"),
                                fieldWithPath("account.prizes").description("경력 내역 등록자의 수상 내역"),
                                fieldWithPath("account.projects").description("경력 내역 등록자의 프로젝트"),
                                fieldWithPath("account.selfInterviews").description("경력 내역 등록자의 셀프 인터뷰"),
                                fieldWithPath("_links.*.*").ignored()
                        )
                ))
        ;
    }

    @Test
    @DisplayName("기업 담당자가 경력 생성할 시 Forbidden")
    void saveExperienceByEnterprise() throws Exception {
        jwt = createEnterpriseJwt();

        String companyName = "D&D 주식회사";
        String description = "D&D 주식회사에서 Spring Boot를 사용해 백엔드 개발을 맡았습니다.";
        String position = "백엔드 개발자";
        LocalDate joinedAt = LocalDate.of(2019, 12, 25);
        LocalDate retiredAt = LocalDate.of(2020, 1, 22);

        ExperienceRequestDto experienceRequestDto = ExperienceRequestDto.builder()
                .companyName(companyName)
                .description(description)
                .position(position)
                .joinedAt(joinedAt)
                .retiredAt(retiredAt)
                .build();

        mockMvc.perform(post(experienceUrl)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(experienceRequestDto)))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    @DisplayName("정상적으로 경력 수정하기")
    void updateExperience() throws Exception {
        Account newAccount = createAccount();
        jwt = generateBearerToken_need_account(newAccount);
        Experience experience = createExperience(newAccount);

        String updateDescription = "사실 프론트엔드 개발자를 맡았었습니다.";
        String updatePosition = "프론트엔트 개발자";

        experience.setDescription(updateDescription);
        experience.setPosition(updatePosition);

        ExperienceRequestDto experienceRequestDto = modelMapper.map(experience, ExperienceRequestDto.class);
        mockMvc.perform(put(experienceUrl + "{experienceId}", experience.getId())
                .accept(MediaTypes.HAL_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(experienceRequestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("companyName").isNotEmpty())
                .andExpect(jsonPath("description").value(updateDescription))
                .andExpect(jsonPath("position").value(updatePosition))
                .andExpect(jsonPath("joinedAt").isNotEmpty())
                .andExpect(jsonPath("retiredAt").isNotEmpty())
                .andExpect(jsonPath("account").isNotEmpty())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.create-experience").exists())
                .andExpect(jsonPath("_links.delete-experience").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("update-experience",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("create-experience").description("link to create experience"),
                                linkWithRel("delete-experience").description("link to delete experience"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("Accept Header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Authorization Header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type Header")
                        ),
                        requestFields(
                                fieldWithPath("companyName").description("수정할 회사 명"),
                                fieldWithPath("position").description("수정할 포지션"),
                                fieldWithPath("joinedAt").description("수정할 입사 날짜"),
                                fieldWithPath("retiredAt").description("수정할 퇴사 날짜"),
                                fieldWithPath("description").description("수정할 경력 설명")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type Header")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").description("수정된 경력 내역 식별자"),
                                fieldWithPath("companyName").description("수정된 회사 명"),
                                fieldWithPath("position").description("수정된 포지션"),
                                fieldWithPath("joinedAt").description("수정된 입사 날짜"),
                                fieldWithPath("retiredAt").description("수정된 퇴사 날짜"),
                                fieldWithPath("description").description("수정된 경력 설명"),
                                fieldWithPath("account.id").description("수정된 경력 내역 등록자 식별자"),
                                fieldWithPath("account.email").description("수정된 경력 내역 등록자 이메일"),
                                fieldWithPath("account.nickName").description("수정된 경력 내역 등록자 닉네임"),
                                fieldWithPath("account.favorite").description("수정된 경력 내역 등록자를 좋아요한 사용자 목록"),
                                fieldWithPath("account.userRole").description("수정된 경력 내역 등록자의 권한"),
                                fieldWithPath("account.createdAt").description("수정된 경력 내역 등록자의 생성 날짜"),
                                fieldWithPath("account.oneLineIntroduce").description("수정된 경력 내역 등록자의 한 줄 소개"),
                                fieldWithPath("account.image").description("수정된 경력 내역 등록자의 프로필 이미지"),
                                fieldWithPath("account.enterprise").description("수정된 경력 내역 등록자의 기업 정보"),
                                fieldWithPath("account.experiences[*].*").ignored(),
                                fieldWithPath("account.licenses").description("수정된 경력 내역 등록자의 자격증"),
                                fieldWithPath("account.prizes").description("수정된 경력 내역 등록자의 수상 내역"),
                                fieldWithPath("account.projects").description("수정된 경력 내역 등록자의 프로젝트"),
                                fieldWithPath("account.selfInterviews").description("수정된 경력 내역 등록자의 셀프 인터뷰"),
                                fieldWithPath("_links.*.*").ignored()
                        )
                ))
        ;
    }

    @Test
    @DisplayName("데이터베이스 저장되어 있지 않은 경력의 수정을 요청")
    void updateExperience_not_found() throws Exception {
        Account newAccount = createAccount();
        jwt = generateBearerToken_need_account(newAccount);
        Experience experience = createExperience(newAccount);

        String updateDescription = "사실 프론트엔드 개발자를 맡았었습니다.";
        String updatePosition = "프론트엔트 개발자";

        experience.setDescription(updateDescription);
        experience.setPosition(updatePosition);

        ExperienceRequestDto experienceRequestDto = modelMapper.map(experience, ExperienceRequestDto.class);
        mockMvc.perform(put(experienceUrl + "{experienceId}", -1)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(experienceRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("권한이 없는 유저가 다른 유저 경력 수정을 요청할 때 Bad Request 반환")
    void updateExperience_invalid_user() throws Exception {
        Account newAccount = createAccount();
        Account anotherAccount = createAnotherAccount();
        jwt = generateBearerToken_need_account(anotherAccount);
        Experience experience = createExperience(newAccount);

        String updateDescription = "사실 프론트엔드 개발자를 맡았었습니다.";
        String updatePosition = "프론트엔트 개발자";

        experience.setDescription(updateDescription);
        experience.setPosition(updatePosition);

        ExperienceRequestDto experienceRequestDto = modelMapper.map(experience, ExperienceRequestDto.class);
        mockMvc.perform(put(experienceUrl + "{experienceId}", experience.getId())
                .accept(MediaTypes.HAL_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(experienceRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("기업담당자가 경력의 수정을 요청할 시 Forbidden")
    void updateExperience_forbidden() throws Exception {
        Account newAccount = createAccount();
        jwt = generateBearerToken_need_account(newAccount);
        Experience experience = createExperience(newAccount);

        jwt = createEnterpriseJwt();

        String updateDescription = "사실 프론트엔드 개발자를 맡았었습니다.";
        String updatePosition = "프론트엔트 개발자";

        experience.setDescription(updateDescription);
        experience.setPosition(updatePosition);

        ExperienceRequestDto experienceRequestDto = modelMapper.map(experience, ExperienceRequestDto.class);
        mockMvc.perform(put(experienceUrl + "{experienceId}", experience.getId())
                .accept(MediaTypes.HAL_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(experienceRequestDto)))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    @DisplayName("정상적으로 경력 삭제하기")
    void deleteExperience() throws Exception {
        Account newAccount = createAccount();
        jwt = generateBearerToken_need_account(newAccount);
        Experience experience = createExperience(newAccount);

        mockMvc.perform(delete(experienceUrl + "{experienceId}", experience.getId())
                .header(HttpHeaders.AUTHORIZATION, jwt))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.create-experience").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("delete-experience",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("create-experience").description("link to create experience"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Authorization Header")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type Header")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").description("삭제된 경력 내역 식별자"),
                                fieldWithPath("companyName").description("삭제된 회사 명"),
                                fieldWithPath("position").description("삭제된 포지션"),
                                fieldWithPath("joinedAt").description("삭제된 입사 날짜"),
                                fieldWithPath("retiredAt").description("삭제된 퇴사 날짜"),
                                fieldWithPath("description").description("삭제된 경력 설명"),
                                fieldWithPath("account.id").description("삭제된 경력 내역 등록자 식별자"),
                                fieldWithPath("account.email").description("삭제된 경력 내역 등록자 이메일"),
                                fieldWithPath("account.nickName").description("삭제된 경력 내역 등록자 닉네임"),
                                fieldWithPath("account.favorite").description("삭제된 경력 내역 등록자를 좋아요한 사용자 목록"),
                                fieldWithPath("account.userRole").description("삭제된 경력 내역 등록자의 권한"),
                                fieldWithPath("account.createdAt").description("삭제된 경력 내역 등록자의 생성 날짜"),
                                fieldWithPath("account.oneLineIntroduce").description("삭제된 경력 내역 등록자의 한 줄 소개"),
                                fieldWithPath("account.image").description("삭제된 경력 내역 등록자의 프로필 이미지"),
                                fieldWithPath("account.enterprise").description("삭제된 경력 내역 등록자의 기업 정보"),
                                fieldWithPath("account.experiences").ignored(),
                                fieldWithPath("account.licenses").description("삭제된 경력 내역 등록자의 자격증"),
                                fieldWithPath("account.prizes").description("삭제된 경력 내역 등록자의 수상 내역"),
                                fieldWithPath("account.projects").description("삭제된 경력 내역 등록자의 프로젝트"),
                                fieldWithPath("account.selfInterviews").description("삭제된 경력 내역 등록자의 셀프 인터뷰"),
                                fieldWithPath("_links.*.*").ignored()
                        ))
                )
        ;
    }

    @Test
    @DisplayName("데이터베이스 저장되어 있지 않은 경력의 삭제를 요청")
    void deleteExperience_not_found() throws Exception {
        Account newAccount = createAccount();
        jwt = generateBearerToken_need_account(newAccount);
        createExperience(newAccount);

        mockMvc.perform(delete(experienceUrl + "{experienceId}", -1)
                .header(HttpHeaders.AUTHORIZATION, jwt))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("권한이 없는 유저가 다른 유저 경력 삭제를 요청할 때 Bad Request 반환")
    void deleteExperience_invalid_user() throws Exception {
        Account newAccount = createAccount();
        Account anotherAccount = createAnotherAccount();
        jwt = generateBearerToken_need_account(anotherAccount);
        Experience experience = createExperience(newAccount);

        mockMvc.perform(delete(experienceUrl + "{experienceId}", experience.getId())
                .header(HttpHeaders.AUTHORIZATION, jwt))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("기업 담당자가 경력의 삭제를 요청할 시 Forbidden")
    void deleteExperience_forbidden() throws Exception {
        Account newAccount = createAccount();
        jwt = generateBearerToken_need_account(newAccount);
        Experience experience = createExperience(newAccount);

        jwt = createEnterpriseJwt();

        mockMvc.perform(delete(experienceUrl + "{experienceId}", experience.getId())
                .header(HttpHeaders.AUTHORIZATION, jwt))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
    }

    private Experience createExperience(Account account) {
        String companyName = "D&D 주식회사";
        String description = "D&D 주식회사에서 Spring Boot를 사용해 백엔드 개발을 맡았습니다.";
        String position = "백엔드 개발자";
        LocalDate joinedAt = LocalDate.of(2019, 12, 25);
        LocalDate retiredAt = LocalDate.of(2020, 1, 22);

        Experience experience = Experience.builder()
                .companyName(companyName)
                .description(description)
                .position(position)
                .joinedAt(joinedAt)
                .retiredAt(retiredAt)
                .account(account)
                .build();

        Experience newExperience = experienceRepository.save(experience);

        assertNotNull(newExperience.getId());
        assertNotNull(newExperience.getCompanyName());
        assertNotNull(newExperience.getDescription());
        assertNotNull(newExperience.getPosition());
        assertNotNull(newExperience.getJoinedAt());
        assertNotNull(newExperience.getRetiredAt());
        assertNotNull(newExperience.getAccount());

        return newExperience;
    }

    private String generateBearerToken() {
        Account newAccount = createAccount();
        return "Bearer " + jwtProvider.generateToken(newAccount);
    }

    private String generateBearerToken_need_account(Account newAccount) {
        return "Bearer " + jwtProvider.generateToken(newAccount);
    }
}