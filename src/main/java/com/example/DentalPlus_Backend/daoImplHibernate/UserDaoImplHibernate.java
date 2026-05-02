package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.UserDao;
import com.example.DentalPlus_Backend.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
		List<User> users = entityManager.createQuery("""
				FROM User u
				WHERE u.username = :username
				""", User.class).setParameter("username", username.trim()).setMaxResults(1).getResultList();
		return users.isEmpty() ? null : users.get(0);
	}

	@Override
	public List<User> findAll() {
		return entityManager.createQuery("FROM User", User.class).getResultList();
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