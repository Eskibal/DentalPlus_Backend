package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.UserDao;
import com.example.DentalPlus_Backend.model.User;
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
public class UserDaoImplHibernate implements UserDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public User findById(Long id) {
		return entityManager.find(User.class, id);
	}

	@Override
	public User findByUsername(String username) {
		if (username == null || username.isBlank()) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> user = cq.from(User.class);

		cq.select(user)
				.where(cb.equal(user.get("username"), username.trim()));

		List<User> users = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return users.isEmpty() ? null : users.get(0);
	}

	@Override
	public List<User> findAll() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> user = cq.from(User.class);

		cq.select(user);

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public void save(User user) {
		entityManager.persist(user);
	}

	@Override
	public User update(User user) {
		return entityManager.merge(user);
	}

	@Override
	public void delete(User user) {
		entityManager.remove(entityManager.contains(user) ? user : entityManager.merge(user));
	}
}