package com.nexusmarket.domain.repository;

import com.nexusmarket.domain.model.Invoice;
public interface BillingRepository {
    Invoice save(Invoice inv);
}
