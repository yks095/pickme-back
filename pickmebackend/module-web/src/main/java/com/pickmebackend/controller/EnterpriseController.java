package com.pickmebackend.controller;

import com.pickmebackend.annotation.CurrentUser;
import com.pickmebackend.common.ErrorsFormatter;
import com.pickmebackend.domain.Account;
import com.pickmebackend.domain.Enterprise;
import com.pickmebackend.domain.dto.enterprise.EnterpriseFilterRequestDto;
import com.pickmebackend.domain.dto.enterprise.EnterpriseRequestDto;
import com.pickmebackend.domain.dto.enterprise.EnterpriseResponseDto;
import com.pickmebackend.repository.account.AccountRepository;
import com.pickmebackend.resource.EnterpriseResource;
import com.pickmebackend.service.EnterpriseService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import javax.mail.MessagingException;
import javax.validation.Valid;
import java.util.Optional;
import static com.pickmebackend.error.ErrorMessageConstant.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/enterprises", produces = MediaTypes.HAL_JSON_VALUE)
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    private final AccountRepository accountRepository;

    private final ModelMapper modelMapper;

    private final ErrorsFormatter errorsFormatter;

    @GetMapping("/profile")
    public ResponseEntity<?> loadProfile(@CurrentUser Account currentUser)   {
        if (currentUser == null) {
            return new ResponseEntity<>(errorsFormatter.formatAnError(USER_NOT_FOUND), HttpStatus.BAD_REQUEST);
        }
        Optional<Account> accountOptional = accountRepository.findById(currentUser.getId());
        if (!accountOptional.isPresent()) {
            return new ResponseEntity<>(errorsFormatter.formatAnError(USER_NOT_FOUND), HttpStatus.BAD_REQUEST);
        }
        Account account = accountOptional.get();
        EnterpriseResponseDto enterpriseResponseDto = enterpriseService.loadProfile(account);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(EnterpriseController.class).slash(enterpriseResponseDto.getId());
        EnterpriseResource enterpriseResource = new EnterpriseResource(enterpriseResponseDto);
        enterpriseResource.add(selfLinkBuilder.withRel("update-enterprise"));
        enterpriseResource.add(selfLinkBuilder.withRel("delete-enterprise"));
        enterpriseResource.add(new Link("/docs/index.html#resources-profile-load").withRel("profile"));

        return new ResponseEntity<>(enterpriseResource, HttpStatus.OK);
    }

    @GetMapping("/{enterpriseId}")
    public ResponseEntity<?> loadEnterprise(@PathVariable Long enterpriseId) {
        if(enterpriseService.isNonEnterprise(enterpriseId)) {
            return ResponseEntity.badRequest().body(errorsFormatter.formatAnError(USER_NOT_FOUND));
        }
        Optional<Account> accountOptional = this.accountRepository.findById(enterpriseId);
        Account account = accountOptional.get();
        EnterpriseResponseDto enterpriseResponseDto = enterpriseService.loadEnterprise(account);
        EnterpriseResource enterpriseResource = new EnterpriseResource(enterpriseResponseDto);
        enterpriseResource.add(new Link("/docs/index.html#resources-enterprise-load").withRel("profile"));

        return new ResponseEntity<>(enterpriseResource, HttpStatus.OK);
    }

    @GetMapping
    ResponseEntity<?> loadEnterprisesWithFilter(@RequestParam(required = false) String name,
                                              @RequestParam(required = false) String address,
                                              Pageable pageable,
                                              PagedResourcesAssembler<Enterprise> assembler)    {

        EnterpriseFilterRequestDto enterpriseFilterRequestDto = EnterpriseFilterRequestDto
                .builder()
                .name(name)
                .address(address)
                .build();

        Page<Enterprise> filteredEnterprises = enterpriseService.loadEnterprisesWithFilter(enterpriseFilterRequestDto, pageable);
        PagedModel<EnterpriseResource> enterpriseResources = getEnterpriseResources(assembler, filteredEnterprises);
        enterpriseResources.add(new Link("/docs/index.html#resources-enterprises-load").withRel("profile"));

        return new ResponseEntity<>(enterpriseResources, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> saveEnterprise(@Valid @RequestBody EnterpriseRequestDto enterpriseRequestDto, Errors errors) {
        if(errors.hasErrors())  {
            return ResponseEntity.badRequest().body(errorsFormatter.formatErrors(errors));
        }
        if(enterpriseService.isDuplicatedEnterprise(enterpriseRequestDto)) {
            return ResponseEntity.badRequest().body(errorsFormatter.formatAnError(DUPLICATEDUSER));
        }
        EnterpriseResponseDto enterpriseResponseDto = enterpriseService.saveEnterprise(enterpriseRequestDto);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(EnterpriseController.class).slash(enterpriseResponseDto.getId());
        EnterpriseResource enterpriseResource = new EnterpriseResource(enterpriseResponseDto);
        enterpriseResource.add(linkTo(LoginController.class).withRel("login-enterprise"));
        enterpriseResource.add(new Link("/docs/index.html#resources-enterprise-create").withRel("profile"));

        return ResponseEntity.created(selfLinkBuilder.toUri()).body(enterpriseResource);
    }

    @PutMapping("/{enterpriseId}")
    public ResponseEntity<?> updateEnterprise(@PathVariable Long enterpriseId, @Valid @RequestBody EnterpriseRequestDto enterpriseRequestDto, Errors errors, @CurrentUser Account currentUser) {
        if(errors.hasErrors())  {
            return ResponseEntity.badRequest().body(errorsFormatter.formatErrors(errors));
        }
        if(enterpriseService.isNonEnterprise(enterpriseId)) {
            return ResponseEntity.badRequest().body(errorsFormatter.formatAnError(USER_NOT_FOUND));
        }
        Optional<Account> accountOptional = this.accountRepository.findById(enterpriseId);
        if (!enterpriseId.equals(currentUser.getId())) {
            return new ResponseEntity<>(errorsFormatter.formatAnError(UNAUTHORIZEDUSER), HttpStatus.BAD_REQUEST);
        }
        EnterpriseResponseDto enterpriseResponseDto = enterpriseService.updateEnterprise(accountOptional.get(), enterpriseRequestDto);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(EnterpriseController.class).slash(enterpriseResponseDto.getId());
        EnterpriseResource enterpriseResource = new EnterpriseResource(enterpriseResponseDto);
        enterpriseResource.add(selfLinkBuilder.withRel("delete-enterprise"));
        enterpriseResource.add(new Link("/docs/index.html#resources-enterprise-update").withRel("profile"));

        return new ResponseEntity<>(enterpriseResource, HttpStatus.OK);
    }

    @DeleteMapping("/{enterpriseId}")
    public ResponseEntity<?> deleteEnterprise(@PathVariable Long enterpriseId, @CurrentUser Account currentUser)    {
        if(enterpriseService.isNonEnterprise(enterpriseId)) {
            return ResponseEntity.badRequest().body(errorsFormatter.formatAnError(USER_NOT_FOUND));
        }
        if (!enterpriseId.equals(currentUser.getId())) {
            return new ResponseEntity<>(errorsFormatter.formatAnError(UNAUTHORIZEDUSER), HttpStatus.BAD_REQUEST);
        }
        Optional<Account> optionalAccount = this.accountRepository.findById(enterpriseId);
        EnterpriseResponseDto enterpriseResponseDto = enterpriseService.deleteEnterprise(optionalAccount.get());
        EnterpriseResource enterpriseResource = new EnterpriseResource(enterpriseResponseDto);
        enterpriseResource.add(linkTo(LoginController.class).withRel("login-enterprise"));
        enterpriseResource.add(new Link("/docs/index.html#resources-enterprise-delete").withRel("profile"));

        return new ResponseEntity<>(enterpriseResource, HttpStatus.OK);
    }

    @GetMapping("/suggestion")
    public ResponseEntity<?> sendSuggestion(@RequestParam(value = "accountId") Long accountId, @CurrentUser Account currentUser) throws MessagingException {
        if(currentUser == null) {
            return ResponseEntity.badRequest().body(errorsFormatter.formatAnError(USER_NOT_FOUND));
        }
        return enterpriseService.sendSuggestion(accountId, currentUser);
    }

    private PagedModel<EnterpriseResource> getEnterpriseResources(PagedResourcesAssembler<Enterprise> assembler, Page<Enterprise> filteredEnterprises) {
        return assembler
                .toModel(filteredEnterprises, e -> {
                    EnterpriseResponseDto enterpriseResponseDto = modelMapper.map(e, EnterpriseResponseDto.class);
                    enterpriseResponseDto.setEmail(e.getAccount().getEmail());
                    return new EnterpriseResource(enterpriseResponseDto);
                });
    }
}
