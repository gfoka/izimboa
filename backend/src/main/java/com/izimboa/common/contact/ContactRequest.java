package com.izimboa.common.contact;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Demande reçue par un formulaire du site (ex. entreprise qui souhaite faire livrer ses colis par Izimboa). */
@Entity
@Table(name = "contact_request", schema = "common")
public class ContactRequest {

    @Id private UUID id;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(nullable = false) private String product;
    @Column(nullable = false) private String kind;
    private String company;
    @Column(name = "contact_name", nullable = false) private String contactName;
    @Column(nullable = false) private String phone;
    private String email;
    private String city;
    private String activity;
    private String volume;
    private String message;
    @Column(nullable = false) private String lang;
    @Column(nullable = false) private String status;

    protected ContactRequest() {}

    ContactRequest(Instant now, String product, String kind, String company, String contactName, String phone,
                   String email, String city, String activity, String volume, String message, String lang) {
        this.id = UUID.randomUUID();
        this.createdAt = now;
        this.product = product;
        this.kind = kind;
        this.company = company;
        this.contactName = contactName;
        this.phone = phone;
        this.email = email;
        this.city = city;
        this.activity = activity;
        this.volume = volume;
        this.message = message;
        this.lang = lang;
        this.status = "NEW";
    }

    public UUID getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
    public String getProduct() { return product; }
    public String getKind() { return kind; }
    public String getCompany() { return company; }
    public String getContactName() { return contactName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getCity() { return city; }
    public String getActivity() { return activity; }
    public String getVolume() { return volume; }
    public String getMessage() { return message; }
    public String getLang() { return lang; }
    public String getStatus() { return status; }
}
