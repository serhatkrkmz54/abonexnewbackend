package com.abonex.abonexbackend.service.subs;

import com.abonex.abonexbackend.dto.subs.response.NotificationResponse;
import com.abonex.abonexbackend.dto.subs.response.UnreadCountResponse;
import com.abonex.abonexbackend.entity.Notification;
import com.abonex.abonexbackend.entity.User;
import com.abonex.abonexbackend.repository.NotificationRepository;
import com.abonex.abonexbackend.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthService authService;

    /**
     * Oturum açmış kullanıcının tüm bildirimlerini, en yeniden eskiye doğru listeler.
     * @return Bildirimlerin DTO listesi.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications() {
        User user = authService.getAuthenticatedUser();
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Oturum açmış kullanıcının okunmamış bildirim sayısını döndürür.
     * @return UnreadCountResponse nesnesi.
     */
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadNotificationCount() {
        User user = authService.getAuthenticatedUser();
        long count = notificationRepository.countByUserAndIsReadFalse(user);
        return new UnreadCountResponse(count);
    }

    /**
     * ID'si verilen tek bir bildirimi okundu olarak işaretler.
     * @param notificationId Okundu olarak işaretlenecek bildirimin ID'si.
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        User user = authService.getAuthenticatedUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bildirim bulunamadı."));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu işlem için yetkiniz yok.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Oturum açmış kullanıcının tüm okunmamış bildirimlerini okundu olarak işaretler.
     */
    @Transactional
    public void markAllAsRead() {
        User user = authService.getAuthenticatedUser();
        notificationRepository.markAllAsReadForUser(user);
    }
}
