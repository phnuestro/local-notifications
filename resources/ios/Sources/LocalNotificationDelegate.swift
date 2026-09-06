import Foundation
import UserNotifications

/// Registered as the app's UNUserNotificationCenterDelegate during plugin init
/// (wired up via `ios.init_function` in nativephp.json, or chained if another
/// plugin — e.g. push notifications — already owns the delegate slot).
class LocalNotificationDelegate: NSObject, UNUserNotificationCenterDelegate {

    static let shared = LocalNotificationDelegate()

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        let id = userInfo["id"] as? String ?? response.notification.request.identifier
        let data = userInfo["data"] as? [String: Any] ?? [:]

        NativeEventDispatcher.dispatch(
            "Paolo\\LocalNotifications\\Events\\NotificationTapped",
            data: ["id": id, "data": data]
        )

        completionHandler()
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // Show the notification even while the app is in the foreground.
        completionHandler([.banner, .sound, .badge])
    }
}
