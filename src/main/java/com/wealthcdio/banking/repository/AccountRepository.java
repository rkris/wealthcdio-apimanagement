package com.wealthcdio.banking.repository;

import com.wealthcdio.banking.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
}
