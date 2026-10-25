
package com.payment.paymentservice.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private String methodName;

    @Column(nullable = false)
    private Instant validFrom;

    @Column(nullable = false)
    private Instant validTo;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    protected Subscription() {
    }

    public Subscription(String companyName,
                        String serviceName,
                        String methodName,
                        Instant validFrom,
                        Instant validTo,
                        String token) {
        this.companyName = companyName;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidTo() {
        return validTo;
    }

    public String getToken() {
        return token;
    }
}
