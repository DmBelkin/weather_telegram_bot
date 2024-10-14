
import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.NativeQuery;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;


@Getter
public class DBConnection {

    private StandardServiceRegistry registry;

    private Metadata metadata;

    private SessionFactory sessionFactory;

    public DBConnection() {
        registry = new StandardServiceRegistryBuilder().
                configure("hibernate.cfg.xml").build();
        metadata = new MetadataSources(registry).getMetadataBuilder().build();
        sessionFactory = metadata.getSessionFactoryBuilder().build();
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

    public String selectHistory(String userName) {
        StringBuilder b = new StringBuilder();
        Session session = sessionFactory.openSession();
        String hqlQuery = "SELECT * FROM query_history  WHERE query_history.user_name =:username";
        NativeQuery<QueryEntity> query = session.createNativeQuery(hqlQuery, QueryEntity.class);
        query.setParameter("username", userName.trim());
        List<QueryEntity> l = query.getResultList();
        b.append(userName + "\n");
        for (QueryEntity entity : l) {
            b.append(entity.getTown() + "\n");
            b.append("date: " + entity.getDateAndTime() + "\n");
            b.append(entity.getResponse() + "\n\n");
        }
        session.close();
        try {
            writeAndSendResult(l, userName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b.toString();
    }

    public void writeAndSendResult(List<QueryEntity> res, String usrName) throws IOException {
        File file = new File("out/history" + "_" + usrName + ".txt");
        PrintWriter writer = new PrintWriter(file);
        StringBuilder builder = new StringBuilder();
        for (QueryEntity entity : res) {
            builder.append(entity + "\n");
        }
        writer.write(builder.toString());
        writer.flush();
        writer.close();
    }
}
