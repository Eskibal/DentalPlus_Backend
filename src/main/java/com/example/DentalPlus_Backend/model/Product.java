package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

@JsonPropertyOrder({
    "id",
    "name",
    "description",
    "active"
})
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean active;

    public Product() {
    }

    public Product(String name, String description, Boolean active) {
        this.name = normalizeText(name);
        this.description = normalizeText(description);
        this.active = active != null ? active : true;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalizeText(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = normalizeText(description);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active != null ? active : true;
    }

    public static boolean isNameValid(String name) {
        return name != null && !name.isBlank() && name.trim().length() <= 120;
    }

    public static boolean isDescriptionValid(String description) {
        return description == null || description.isBlank() || description.trim().length() <= 500;
    }

    public static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }
}