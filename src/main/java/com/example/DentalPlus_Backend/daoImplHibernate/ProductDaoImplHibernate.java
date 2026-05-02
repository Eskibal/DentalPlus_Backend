package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.ProductDao;
import com.example.DentalPlus_Backend.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("hibernate")
public class ProductDaoImplHibernate implements ProductDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Product findById(Long id) {
		return entityManager.find(Product.class, id);
	}

	@Override
	public Product findByName(String name) {
		if (name == null || name.isBlank()) {
			return null;
		}

		return entityManager.createQuery("""
				FROM Product p
				WHERE LOWER(p.name) = :name
				""", Product.class).setParameter("name", name.trim().toLowerCase()).getResultStream().findFirst()
				.orElse(null);
	}

	@Override
	public List<Product> findAll() {
		return entityManager.createQuery("""
				FROM Product p
				ORDER BY p.name ASC
				""", Product.class).getResultList();
	}

	@Override
	public List<Product> findActive() {
		return entityManager.createQuery("""
				FROM Product p
				WHERE p.active = true
				ORDER BY p.name ASC
				""", Product.class).getResultList();
	}

	@Override
	public void save(Product product) {
		entityManager.persist(product);
	}

	@Override
	public Product update(Product product) {
		return entityManager.merge(product);
	}

	@Override
	public void delete(Product product) {
		entityManager.remove(entityManager.contains(product) ? product : entityManager.merge(product));
	}
}