package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

@JsonPropertyOrder({ "id", "box", "product", "quantity", "minimumQuantity", "active", "notes" })
@Entity
@Table(name = "inventory")
public class Inventory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "box_id", nullable = false)
	private Box box;

	@ManyToOne(optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false)
	private Integer minimumQuantity;

	@Column(nullable = false)
	private Boolean active;

	@Column(length = 500)
	private String notes;

	public Inventory() {
	}

	public Inventory(Box box, Product product, Integer quantity, Integer minimumQuantity, Boolean active,
			String notes) {
		this.box = box;
		this.product = product;
		this.quantity = normalizeNumber(quantity);
		this.minimumQuantity = normalizeNumber(minimumQuantity);
		this.active = active != null ? active : true;
		this.notes = normalizeText(notes);
	}

	public Long getId() {
		return id;
	}

	public Box getBox() {
		return box;
	}

	public void setBox(Box box) {
		this.box = box;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = normalizeNumber(quantity);
	}

	public Integer getMinimumQuantity() {
		return minimumQuantity;
	}

	public void setMinimumQuantity(Integer minimumQuantity) {
		this.minimumQuantity = normalizeNumber(minimumQuantity);
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active != null ? active : true;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = normalizeText(notes);
	}

	public static boolean isQuantityValid(Integer quantity) {
		return quantity != null && quantity >= 0;
	}

	public static boolean isMinimumQuantityValid(Integer minimumQuantity) {
		return minimumQuantity != null && minimumQuantity >= 0;
	}

	public static boolean isNotesValid(String notes) {
		return notes == null || notes.isBlank() || notes.trim().length() <= 500;
	}

	public static Integer normalizeNumber(Integer number) {
		return number == null || number < 0 ? 0 : number;
	}

	public static String normalizeText(String text) {
		return text == null ? null : text.trim();
	}
}