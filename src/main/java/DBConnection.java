import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.time.LocalDateTime;
import java.util.List;


@Getter
public class DBConnection {

    private StandardServiceRegistry registry;

    private Metadata metadata;

    private SessionFactory sessionFactory;

    private Transaction transaction;

    private CriteriaBuilder builder;

    public DBConnection() {
        registry = new StandardServiceRegistryBuilder().
                configure("hibernate.cfg.xml").build();
        metadata = new MetadataSources(registry).getMetadataBuilder().build();
        sessionFactory = metadata.getSessionFactoryBuilder().build();
//        CriteriaBuilder builder = session.getCriteriaBuilder();
    }

    public void dbTransaction(String userName, String result, String place) {
        try {
            Session session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();
            QueryEntity query = new QueryEntity();
            query.setResponse(result);
            query.setDateAndTime(LocalDateTime.now());
            query.setTown(place);
            query.setUserName(userName);
            session.persist(query);
            transaction.commit();
            session.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void selectHistory(String userId) {
        String hqlQuery = "SELECT * FROM QueryEntity q  WHERE q.user_id = " + userId;

    }

    public void writeAndSendResult(List<String> res, String usrName) {

    }
}
