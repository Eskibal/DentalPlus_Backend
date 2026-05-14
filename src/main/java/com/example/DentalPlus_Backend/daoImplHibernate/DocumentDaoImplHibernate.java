package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.DocumentDao;
import com.example.DentalPlus_Backend.model.Document;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
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
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Document> cq = cb.createQuery(Document.class);
		Root<Document> document = cq.from(Document.class);

		fetchDocumentRelations(document);

		cq.select(document)
				.distinct(true)
				.where(cb.equal(document.get("id"), id));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public List<Document> findByPatientId(Long patientId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Document> cq = cb.createQuery(Document.class);
		Root<Document> document = cq.from(Document.class);

		fetchDocumentRelations(document);

		cq.select(document)
				.distinct(true)
				.where(cb.equal(document.get("patient").get("id"), patientId))
				.orderBy(cb.desc(document.get("id")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<Document> findActiveByPatientId(Long patientId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Document> cq = cb.createQuery(Document.class);
		Root<Document> document = cq.from(Document.class);

		fetchDocumentRelations(document);

		cq.select(document)
				.distinct(true)
				.where(cb.and(
						cb.equal(document.get("patient").get("id"), patientId),
						cb.isTrue(document.get("active"))))
				.orderBy(cb.desc(document.get("id")));

		return entityManager.createQuery(cq).getResultList();
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

	private void fetchDocumentRelations(Root<Document> document) {
		Fetch<Object, Object> patient = document.fetch("patient", JoinType.LEFT);
		patient.fetch("person", JoinType.LEFT);
		patient.fetch("clinic", JoinType.LEFT);
	}
}