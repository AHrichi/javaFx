package Service.notification;

import Entite.Notification;

import java.util.List;

public interface INotificationService {
    void create(Notification n);
    void update(Notification n);
    void delete(int id);
    Notification findById(int id);
    List<Notification> findAll();
    List<Notification> findByRoleOrUser(Notification.RecipientRole role, Integer userId);
}
