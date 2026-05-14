package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.ProductDao;
import com.example.DentalPlus_Backend.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Product> cq = cb.createQuery(Product.class);
		Root<Product> product = cq.from(Product.class);

		cq.select(product)
				.where(cb.equal(cb.lower(product.get("name")), name.trim().toLowerCase()));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public List<Product> findAll() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Product> cq = cb.createQuery(Product.class);
		Root<Product> product = cq.from(Product.class);

		cq.select(product)
				.orderBy(cb.asc(product.get("name")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<Product> findActive() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Product> cq = cb.createQuery(Product.class);
		Root<Product> product = cq.from(Product.class);

		cq.select(product)
				.where(cb.isTrue(product.get("active")))
				.orderBy(cb.asc(product.get("name")));

		return entityManager.createQuery(cq).getResultList();
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