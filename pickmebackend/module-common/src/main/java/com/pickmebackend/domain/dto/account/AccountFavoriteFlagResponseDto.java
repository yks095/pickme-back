package com.pickmebackend.domain.dto.account;

import com.pickmebackend.domain.*;
import com.pickmebackend.domain.enums.UserRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AccountFavoriteFlagResponseDto {

    private Long id;

    private String email;

    private String nickName;

    private int favoriteCount;

    private String oneLineIntroduce;

    private String image;

    private UserRole userRole;

    private String socialLink;

    private String career;

    private LocalDateTime createdAt;

    private Set<Experience> experiences;

    private Set<License> licenses;

    private Set<Prize> prizes;

    private Set<Project> projects;

    private Set<SelfInterview> selfInterviews;

    private List<Technology> technologies = new ArrayList<>();

    private Set<String> positions;

    private long hits;

    private boolean favoriteFlag;

    public AccountFavoriteFlagResponseDto (Account account, Account currentUser) {
        this.id = account.getId();
        this.email = account.getEmail();
        this.nickName = account.getNickName();
        this.favoriteCount = account.getFavorite().size();
        this.oneLineIntroduce = account.getOneLineIntroduce();
        this.career = account.getCareer();
        this.image = account.getImage();
        this.positions = account.getPositions();
        this.userRole = account.getUserRole();
        this.socialLink = account.getSocialLink();
        this.experiences = account.getExperiences();
        this.licenses = account.getLicenses();
        this.prizes = account.getPrizes();
        this.projects = account.getProjects();
        this.selfInterviews = account.getSelfInterviews();
        this.createdAt = account.getCreatedAt();
        this.userRole = account.getUserRole();
        this.hits = account.getHits();
        this.technologies = account.getAccountTechSet().stream()
                .map(AccountTech::getTechnology)
                .collect(Collectors.toList());
        this.favoriteFlag = account.getFavorite().contains(currentUser);
    }
}
