package com.sidorykp.sandbox.vaadin;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sidorykp.sandbox.vaadin.domain.AddressEntity;
import com.sidorykp.sandbox.vaadin.domain.Person;
import com.sidorykp.sandbox.vaadin.ui.BasicCrudView;

import com.vaadin.Application;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.util.EntityManagerPerRequestHelper;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class MyVaadinApplication extends Application implements HttpServletRequestListener {

	public static final String PERSISTENCE_UNIT = "com.sidorykp.sandbox.vaadin";

    protected EntityManagerPerRequestHelper emHelper;

    protected static final Logger log = LoggerFactory.getLogger(MyVaadinApplication.class);

	@Override
	public void init() {
        log.debug("init");
        // TODO trace emHelper executions with Byteman in order to confirm that it really works
        // TODO call emHelper.removeContainer() when sessions expire
        emHelper = new EntityManagerPerRequestHelper();
		setMainWindow(new AutoCrudViews());
	}

    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        log.debug("requestStart");
        if (emHelper != null) {
            emHelper.requestStart();
        }
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        log.debug("requestEnd");
        if (emHelper != null) {
            emHelper.requestEnd();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	class AutoCrudViews extends Window {
		
		public AutoCrudViews() {
			final HorizontalSplitPanel horizontalSplitPanel = new HorizontalSplitPanel();
			Tree navTree = new Tree();
			navTree.addListener(new Property.ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					BasicCrudView cv = (BasicCrudView) event.getProperty()
							.getValue();
					cv.refreshContainer();
					horizontalSplitPanel.setSecondComponent(cv);
				}
			});
			navTree.setSelectable(true);
			navTree.setNullSelectionAllowed(false);
			navTree.setImmediate(true);

			horizontalSplitPanel.setSplitPosition(200,
					HorizontalSplitPanel.UNITS_PIXELS);
			horizontalSplitPanel.addComponent(navTree);
			setContent(horizontalSplitPanel);

			// add a basic crud view for all entities known by the JPA
			// implementation, most often this is not desired and developers
			// should just list those entities they want to have editors for
			Metamodel metamodel = JPAContainerFactory
					.createEntityManagerForPersistenceUnit(PERSISTENCE_UNIT)
					.getEntityManagerFactory().getMetamodel();
			Set<EntityType<?>> entities = metamodel.getEntities();
			for (EntityType<?> entityType : entities) {
				Class<?> javaType = entityType.getJavaType();
				BasicCrudView view = new BasicCrudView(javaType,
						PERSISTENCE_UNIT, emHelper);
				navTree.addItem(view);
				navTree.setItemCaption(view, view.getCaption());
				navTree.setChildrenAllowed(view, false);
				if(javaType == Person.class) {
					view.setVisibleTableProperties("firstName","lastName", "boss");
					view.setVisibleFormProperties("firstName","lastName", "phoneNumber", "addresses", "boss");
				} else if (javaType == AddressEntity.class) {
                    view.setVisibleTableProperties("id", "street", "zipCode", "city");
                    view.setVisibleFormProperties("person", "street", "zipCode", "city");
                }

			}

			// select first entity view
			navTree.setValue(navTree.getItemIds().iterator().next());
		}
	}

	static {
		EntityManager em = JPAContainerFactory
				.createEntityManagerForPersistenceUnit(PERSISTENCE_UNIT);

		long size = (Long) em.createQuery("SELECT COUNT(p) FROM Person p").getSingleResult();
		if (size == 0) {
			// create two Person objects as test data

			em.getTransaction().begin();
			Person boss = new Person();
			boss.setFirstName("John");
			boss.setLastName("Bigboss");
			boss.setPhoneNumber("+358 02 555 221");
			em.persist(boss);

			Person p = new Person();
			p.setFirstName("Marc");
			p.setLastName("Hardworker");
			p.setPhoneNumber("+358 02 555 222");
			p.setBoss(boss);
			em.persist(p);

			em.getTransaction().commit();
		}

	}

}
