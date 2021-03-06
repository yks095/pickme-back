package com.pickmebackend.controller;

import com.pickmebackend.annotation.CurrentUser;
import com.pickmebackend.common.ErrorsFormatter;
import com.pickmebackend.domain.Account;
import com.pickmebackend.domain.Experience;
import com.pickmebackend.domain.dto.experience.ExperienceRequestDto;
import com.pickmebackend.domain.dto.experience.ExperienceResponseDto;
import com.pickmebackend.repository.ExperienceRepository;
import com.pickmebackend.resource.ExperienceResource;
import com.pickmebackend.service.ExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import static com.pickmebackend.error.ErrorMessageConstant.EXPERIENCENOTFOUND;
import static com.pickmebackend.error.ErrorMessageConstant.UNAUTHORIZEDUSER;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/api/experiences", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class ExperienceController {

    private final ExperienceService experienceService;

    private final ExperienceRepository experienceRepository;

    private final ErrorsFormatter errorsFormatter;

    @PostMapping
    ResponseEntity<?> saveExperience(@RequestBody ExperienceRequestDto experienceRequestDto, @CurrentUser Account currentUser) {
        ExperienceResponseDto experienceResponseDto =  experienceService.saveExperience(experienceRequestDto, currentUser);

        WebMvcLinkBuilder selfLinkBuilder = linkTo(ExperienceController.class).slash(experienceResponseDto.getId());
        ExperienceResource experienceResource = new ExperienceResource(experienceResponseDto);
        experienceResource.add(selfLinkBuilder.withRel("update-experience"));
        experienceResource.add(selfLinkBuilder.withRel("delete-experience"));
        experienceResource.add(new Link("/docs/index.html#resources-experiences-create").withRel("profile"));

        return ResponseEntity.created(selfLinkBuilder.toUri()).body(experienceResource);
    }

    @PutMapping(value = "/{experienceId}")
    ResponseEntity<?> updateExperience(@PathVariable Long experienceId, @RequestBody ExperienceRequestDto experienceRequestDto, @CurrentUser Account currentUser) {
        Optional<Experience> experienceOptional = this.experienceRepository.findById(experienceId);
        if (!experienceOptional.isPresent()) {
            return new ResponseEntity<>(errorsFormatter.formatAnError(EXPERIENCENOTFOUND), HttpStatus.BAD_REQUEST);
        }

        Experience experience = experienceOptional.get();
        if (!experience.getAccount().getId().equals(currentUser.getId())) {
            return new ResponseEntity<>(errorsFormatter.formatAnError(UNAUTHORIZEDUSER), HttpStatus.BAD_REQUEST);
        }

        ExperienceResponseDto modifiedExperienceResponseDto = experienceService.updateExperience(experience, experienceRequestDto);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(ExperienceController.class).slash(modifiedExperienceResponseDto.getId());
        ExperienceResource experienceResource = new ExperienceResource(modifiedExperienceResponseDto);
        experienceResource.add(linkTo(ExperienceController.class).withRel("create-experience"));
        experienceResource.add(selfLinkBuilder.withRel("delete-experience"));
        experienceResource.add(new Link("/docs/index.html#resources-experiences-update").withRel("profile"));

        return new ResponseEntity<>(experienceResource, HttpStatus.OK);
    }

    @DeleteMapping(value = "/{experienceId}")
    ResponseEntity<?> deleteExperience(@PathVariable Long experienceId, @CurrentUser Account currentUser) {
        Optional<Experience> experienceOptional = this.experienceRepository.findById(experienceId);
        if (!experienceOptional.isPresent()) {
            return new ResponseEntity<>(errorsFormatter.formatAnError(EXPERIENCENOTFOUND), HttpStatus.BAD_REQUEST);
        }

        Experience experience = experienceOptional.get();
        if (!experience.getAccount().getId().equals(currentUser.getId())) {
            return new ResponseEntity<>(errorsFormatter.formatAnError(UNAUTHORIZEDUSER), HttpStatus.BAD_REQUEST);
        }

        ExperienceResponseDto experienceResponseDto = experienceService.deleteExperience(experience);
        ExperienceResource experienceResource = new ExperienceResource(experienceResponseDto);
        experienceResource.add(linkTo(ExperienceController.class).withRel("create-experience"));
        experienceResource.add(new Link("/docs/index.html#resources-experiences-delete").withRel("profile"));

        return new ResponseEntity<>(experienceResource, HttpStatus.OK);
    }
}
