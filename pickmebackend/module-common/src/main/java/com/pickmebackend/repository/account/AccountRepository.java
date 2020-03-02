package com.pickmebackend.repository.account;

import com.pickmebackend.domain.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>, AccountRepositoryCustom {
    @Query("SELECT acc FROM Account acc WHERE acc.userRole = 'USER' ORDER BY acc.createdAt DESC")
    Page<Account> findAllAccountsDesc(Pageable pageable);

    @Query("SELECT acc FROM Account acc WHERE acc.userRole = 'USER' ORDER BY acc.favoriteCount DESC, acc.createdAt DESC")
    Page<Account> findAllAccountsDescAndOrderByFavorite(Pageable pageable);

    @Query("select acc from Account acc where acc.userRole = 'USER' ORDER BY acc.hits DESC, acc.createdAt DESC")
    Page<Account> findAllAccountsDescAndOrderByHits(Pageable pageable);

    Optional<Account> findByEmail(String email);
}