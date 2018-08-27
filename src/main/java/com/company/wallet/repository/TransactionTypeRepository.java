package com.company.wallet.repository;

import com.company.wallet.entities.TransactionType;
import com.company.wallet.exceptions.WalletException;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

/**
 * Transaction type JPA repository
 *  <p> Generates SQL queries to access the database to manage TransactionType entities</p>
 * @author Elena Medvedeva
 */
@Transactional(rollbackOn = WalletException.class)
public interface TransactionTypeRepository extends JpaRepository<TransactionType, String> {
}
