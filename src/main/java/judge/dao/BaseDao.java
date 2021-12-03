package judge.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

@SuppressWarnings("unchecked")
public class BaseDao extends HibernateDaoSupport implements IBaseDao {
    private final static Logger log = LoggerFactory.getLogger(BaseDao.class);

    /**
     * 保存
     * 添加或修改
     * @param entity
     */
    public void addOrModify(Object entity) {
        System.out.println("数据库被改变");
        Session session = super.getSession();
        Transaction tx = session.beginTransaction();
        try {
            sessionAddOrModify(session, entity);
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
        } finally {
            super.releaseSession(session);
        }
    }

    public void delete(Object entity) {
        this.getHibernateTemplate().merge(entity);
        this.getHibernateTemplate().delete(entity);
    }

    public void delete(Class entityClass, Serializable id) {
        Object entity = (Object) this.getHibernateTemplate().get(entityClass, id);
        this.getHibernateTemplate().merge(entity);
        this.getHibernateTemplate().delete(entity);
    }

    public void execute(String statement) {
        Session session = super.getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.createQuery(statement).executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
        } finally {
            super.releaseSession(session);
        }
    }

    public void execute(String statement, Map parMap) {
        Session session = super.getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.createQuery(statement).setProperties(parMap).executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
        } finally {
            super.releaseSession(session);
        }
    }

    ///////////////////////////////////////////////////

    public Object query(Class entityClass, Serializable id) {
        Object entity = (Object) this.getHibernateTemplate().get(entityClass, id);
        if (entity != null) {
            this.getHibernateTemplate().refresh(entity);
        }
        return entity;
    }

    public List query(String hql) {
        return this.getHibernateTemplate().find(hql);
    }

    public List query(String queryString, int FirstResult, int MaxResult) {
        Session session = super.getSession();
        List list = null;
        try {
            list = session.createQuery(queryString).setFirstResult(FirstResult).setMaxResults(MaxResult).list();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            super.releaseSession(session);
        }
        return list;
    }

    public List query(String hql, Map parMap) {
        Session session = super.getSession();
        List list = null;
        try {
            list = session.createQuery(hql).setProperties(parMap).list();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            super.releaseSession(session);
        }
        return list;
    }

    public List query(String hql, Map parMap, int FirstResult, int MaxResult) {
        Session session = super.getSession();
        List list = null;
        try {
            list = session.createQuery(hql).setProperties(parMap).setFirstResult(FirstResult).setMaxResults(MaxResult).list();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            super.releaseSession(session);
        }
        return list;
    }

    ///////////////////////////////////////////////////

    private void sessionAddOrModify(Session session, Object data) {
        if (data instanceof Collection){
            Collection data1 = (Collection) data;
            for (Object object : data1) {
                sessionAddOrModify(session, object);
            }
        } else {
            session.saveOrUpdate(data);
        }
    }

    public Session createSession() {
        return super.getSession();
    }

    public void closeSession(Session session) {
        super.releaseSession(session);
    }




}
