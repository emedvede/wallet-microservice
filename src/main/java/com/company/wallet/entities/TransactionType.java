package com.company.wallet.entities;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 *  Transaction type entity.
 *  <p>
 *  Type can be credit or debit ('C' and 'D' respectively )
 *  </p>
 *  @author Elena Medvedeva
 */
@Entity
@Table(name = "transaction_type")
@EntityListeners(AuditingEntityListener.class)
public class TransactionType {

    @Id
    @Column(name = "id",nullable = false, unique = true)
    private String id;

    @Column(name = "description")
    private String description;

    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;

    public TransactionType(){}

    public TransactionType(String  id,String description, String lastUpdatedBy) {
        this.id = id;
        this.description = description;
        this.lastUpdated = new Date();
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

}
