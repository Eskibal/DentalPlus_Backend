package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.dao.BoxDao;
import com.example.DentalPlus_Backend.dao.InventoryDao;
import com.example.DentalPlus_Backend.dao.ProductDao;
import com.example.DentalPlus_Backend.model.Box;
import com.example.DentalPlus_Backend.model.Inventory;
import com.example.DentalPlus_Backend.model.Product;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

	private final ProductDao productDao;
	private final InventoryDao inventoryDao;
	private final BoxDao boxDao;

	public InventoryService(ProductDao productDao, InventoryDao inventoryDao, BoxDao boxDao) {
		this.productDao = productDao;
		this.inventoryDao = inventoryDao;
		this.boxDao = boxDao;
	}

	public Product getProductById(Long productId) {
		Product product = productDao.findById(productId);

		if (product == null) {
			throw new IllegalArgumentException("Product not found");
		}

		return product;
	}

	public List<Product> getAllProducts() {
		return productDao.findAll();
	}

	public List<Product> getActiveProducts() {
		return productDao.findActive();
	}

	@Transactional
	public Product createProduct(String name, String description, Boolean active) {
		validateProductData(name, description);

		Product existingProduct = productDao.findByName(name);
		if (existingProduct != null) {
			throw new IllegalArgumentException("Product already exists");
		}

		Product product = new Product(name, description, active);

		productDao.save(product);

		return product;
	}

	@Transactional
	public Product updateProduct(Long productId, String name, String description, Boolean active) {
		Product product = getProductById(productId);

		if (name != null) {
			if (!Product.isNameValid(name)) {
				throw new IllegalArgumentException("Invalid product name");
			}

			Product existingProduct = productDao.findByName(name);
			if (existingProduct != null && !existingProduct.getId().equals(product.getId())) {
				throw new IllegalArgumentException("Product name already in use");
			}

			product.setName(name);
		}

		if (description != null) {
			if (!Product.isDescriptionValid(description)) {
				throw new IllegalArgumentException("Invalid product description");
			}

			product.setDescription(description);
		}

		if (active != null) {
			product.setActive(active);
		}

		return productDao.update(product);
	}

	@Transactional
	public Product deactivateProduct(Long productId) {
		Product product = getProductById(productId);
		product.setActive(false);

		return productDao.update(product);
	}

	public Inventory getInventoryById(Long inventoryId) {
		Inventory inventory = inventoryDao.findById(inventoryId);

		if (inventory == null) {
			throw new IllegalArgumentException("Inventory not found");
		}

		return inventory;
	}

	public Inventory getInventoryByBoxAndProduct(Long boxId, Long productId) {
		Inventory inventory = inventoryDao.findByBoxIdAndProductId(boxId, productId);

		if (inventory == null) {
			throw new IllegalArgumentException("Inventory not found");
		}

		return inventory;
	}

	public List<Inventory> getInventoryByBox(Long boxId) {
		ensureBoxExists(boxId);

		return inventoryDao.findByBoxId(boxId);
	}

	public List<Inventory> getActiveInventoryByBox(Long boxId) {
		ensureBoxExists(boxId);

		return inventoryDao.findActiveByBoxId(boxId);
	}

	public List<Inventory> getInventoryByClinic(Long clinicId) {
		return inventoryDao.findByClinicId(clinicId);
	}

	public List<Inventory> getActiveInventoryByClinic(Long clinicId) {
		return inventoryDao.findActiveByClinicId(clinicId);
	}

	public List<Inventory> getLowStockByBox(Long boxId) {
		ensureBoxExists(boxId);

		return inventoryDao.findLowStockByBoxId(boxId);
	}

	public List<Inventory> getLowStockByClinic(Long clinicId) {
		return inventoryDao.findLowStockByClinicId(clinicId);
	}

	@Transactional
	public Inventory createInventory(Long boxId, Long productId, Integer quantity, Integer minimumQuantity,
			Boolean active, String notes) {
		Box box = findBoxOrThrow(boxId);
		Product product = getProductById(productId);

		validateInventoryData(quantity, minimumQuantity, notes);

		Inventory existingInventory = inventoryDao.findByBoxIdAndProductId(boxId, productId);
		if (existingInventory != null) {
			throw new IllegalArgumentException("Inventory already exists for this box and product");
		}

		Inventory inventory = new Inventory(box, product, quantity, minimumQuantity, active, notes);

		inventoryDao.save(inventory);

		return inventory;
	}

	@Transactional
	public Inventory updateInventory(Long inventoryId, Integer quantity, Integer minimumQuantity, Boolean active,
			String notes) {
		Inventory inventory = getInventoryById(inventoryId);

		if (quantity != null) {
			if (!Inventory.isQuantityValid(quantity)) {
				throw new IllegalArgumentException("Invalid quantity");
			}

			inventory.setQuantity(quantity);
		}

		if (minimumQuantity != null) {
			if (!Inventory.isMinimumQuantityValid(minimumQuantity)) {
				throw new IllegalArgumentException("Invalid minimumQuantity");
			}

			inventory.setMinimumQuantity(minimumQuantity);
		}

		if (active != null) {
			inventory.setActive(active);
		}

		if (notes != null) {
			if (!Inventory.isNotesValid(notes)) {
				throw new IllegalArgumentException("Invalid notes");
			}

			inventory.setNotes(notes);
		}

		return inventoryDao.update(inventory);
	}

	@Transactional
	public Inventory increaseStock(Long boxId, Long productId, Integer amount) {
		validateStockAmount(amount);

		Inventory inventory = getInventoryByBoxAndProduct(boxId, productId);
		inventory.setQuantity(inventory.getQuantity() + amount);

		return inventoryDao.update(inventory);
	}

	@Transactional
	public Inventory decreaseStock(Long boxId, Long productId, Integer amount) {
		validateStockAmount(amount);

		Inventory inventory = getInventoryByBoxAndProduct(boxId, productId);

		if (inventory.getQuantity() < amount) {
			throw new IllegalArgumentException("Not enough stock");
		}

		inventory.setQuantity(inventory.getQuantity() - amount);

		return inventoryDao.update(inventory);
	}

	public void validateEnoughStock(Long boxId, Long productId, Integer requiredAmount) {
		validateStockAmount(requiredAmount);

		Inventory inventory = getInventoryByBoxAndProduct(boxId, productId);

		if (!inventory.getActive()) {
			throw new IllegalArgumentException("Inventory is inactive");
		}

		if (!inventory.getProduct().getActive()) {
			throw new IllegalArgumentException("Product is inactive");
		}

		if (!inventory.getBox().getActive()) {
			throw new IllegalArgumentException("Box is inactive");
		}

		if (inventory.getQuantity() < requiredAmount) {
			throw new IllegalArgumentException("Not enough stock");
		}
	}

	@Transactional
	public Inventory deactivateInventory(Long inventoryId) {
		Inventory inventory = getInventoryById(inventoryId);
		inventory.setActive(false);

		return inventoryDao.update(inventory);
	}

	private void validateProductData(String name, String description) {
		if (!Product.isNameValid(name)) {
			throw new IllegalArgumentException("Invalid product name");
		}

		if (!Product.isDescriptionValid(description)) {
			throw new IllegalArgumentException("Invalid product description");
		}
	}

	private void validateInventoryData(Integer quantity, Integer minimumQuantity, String notes) {
		if (!Inventory.isQuantityValid(quantity)) {
			throw new IllegalArgumentException("Invalid quantity");
		}

		if (!Inventory.isMinimumQuantityValid(minimumQuantity)) {
			throw new IllegalArgumentException("Invalid minimumQuantity");
		}

		if (!Inventory.isNotesValid(notes)) {
			throw new IllegalArgumentException("Invalid notes");
		}
	}

	private void validateStockAmount(Integer amount) {
		if (amount == null || amount <= 0) {
			throw new IllegalArgumentException("Invalid stock amount");
		}
	}

	private Box findBoxOrThrow(Long boxId) {
		Box box = boxDao.findById(boxId);

		if (box == null) {
			throw new IllegalArgumentException("Box not found");
		}

		return box;
	}

	private void ensureBoxExists(Long boxId) {
		findBoxOrThrow(boxId);
	}
}