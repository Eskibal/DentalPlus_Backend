package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.DocumentDao;
import com.example.DentalPlus_Backend.model.Document;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("hibernate")
public class DocumentDaoImplHibernate implements DocumentDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Document findById(Long id) {
		return entityManager.find(Document.class, id);
	}

	@Override
	public List<Document> findByPatientId(Long patientId) {
		return entityManager.createQuery("""
				FROM Document d
				WHERE d.patient.id = :patientId
				ORDER BY d.id DESC
				""", Document.class).setParameter("patientId", patientId).getResultList();
	}

	@Override
	public List<Document> findActiveByPatientId(Long patientId) {
		return entityManager.createQuery("""
				FROM Document d
				WHERE d.patient.id = :patientId
				  AND d.active = true
				ORDER BY d.id DESC
				""", Document.class).setParameter("patientId", patientId).getResultList();
	}

	@Override
	public void save(Document document) {
		entityManager.persist(document);
	}

	@Override
	public Document update(Document document) {
		return entityManager.merge(document);
	}

	@Override
	public void delete(Document document) {
		entityManager.remove(entityManager.contains(document) ? document : entityManager.merge(document));
	}
}