package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.PersonDao;
import com.example.DentalPlus_Backend.model.Person;
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
public class PersonDaoImplHibernate implements PersonDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Person findById(Long id) {
		return entityManager.find(Person.class, id);
	}

	@Override
	public Person findByEmail(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Person> cq = cb.createQuery(Person.class);
		Root<Person> person = cq.from(Person.class);

		cq.select(person)
				.where(cb.equal(cb.lower(person.get("email")), email.trim().toLowerCase()));

		List<Person> persons = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return persons.isEmpty() ? null : persons.get(0);
	}

	@Override
	public List<Person> findAll() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Person> cq = cb.createQuery(Person.class);
		Root<Person> person = cq.from(Person.class);

		cq.select(person);

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public void save(Person person) {
		entityManager.persist(person);
	}

	@Override
	public Person update(Person person) {
		return entityManager.merge(person);
	}

	@Override
	public void delete(Person person) {
		entityManager.remove(entityManager.contains(person) ? person : entityManager.merge(person));
	}
}