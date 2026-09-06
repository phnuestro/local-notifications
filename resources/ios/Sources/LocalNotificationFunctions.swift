import Foundation
import UserNotifications

enum LocalNotificationFunctions {

    class RequestPermission: BridgeFunction {
        func execute(parameters: [String: Any]) throws -> [String: Any] {
            let semaphore = DispatchSemaphore(value: 0)
            var granted = false

            UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { result, _ in
                granted = result
                semaphore.signal()
            }

            semaphore.wait()

            return BridgeResponse.success(data: ["granted": granted])
        }
    }

    class Schedule: BridgeFunction {
        func execute(parameters: [String: Any]) throws -> [String: Any] {
            guard let id = parameters["id"] as? String else {
                return BridgeResponse.error(message: "Missing required 'id' parameter")
            }

            let title = parameters["title"] as? String ?? ""
            let body = parameters["body"] as? String ?? ""
            let soundEnabled = (parameters["sound"] as? Bool) ?? true
            let badge = parameters["badge"] as? Int
            let data = parameters["data"] as? [String: Any] ?? [:]

            let content = UNMutableNotificationContent()
            content.title = title
            content.body = body
            content.sound = soundEnabled ? .default : nil
            content.userInfo = ["id": id, "data": data]
            if let badge {
                content.badge = NSNumber(value: badge)
            }

            let trigger = makeTrigger(parameters: parameters)

            let request = UNNotificationRequest(identifier: id, content: content, trigger: trigger)

            let semaphore = DispatchSemaphore(value: 0)
            var errorMessage: String?

            UNUserNotificationCenter.current().add(request) { error in
                errorMessage = error?.localizedDescription
                semaphore.signal()
            }

            semaphore.wait()

            if let errorMessage {
                return BridgeResponse.error(message: errorMessage)
            }

            return BridgeResponse.success(data: ["id": id])
        }

        private func makeTrigger(parameters: [String: Any]) -> UNNotificationTrigger? {
            if let at = parameters["at"] as? Double {
                let date = Date(timeIntervalSince1970: at)
                let components = Calendar.current.dateComponents(
                    [.year, .month, .day, .hour, .minute, .second],
                    from: date
                )
                return UNCalendarNotificationTrigger(dateMatching: components, repeats: false)
            }

            if let delay = parameters["delay"] as? Double {
                return UNTimeIntervalNotificationTrigger(timeInterval: max(delay, 1), repeats: false)
            }

            // No timing supplied — fire almost immediately.
            return UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
        }
    }

    class Cancel: BridgeFunction {
        func execute(parameters: [String: Any]) throws -> [String: Any] {
            guard let id = parameters["id"] as? String else {
                return BridgeResponse.error(message: "Missing required 'id' parameter")
            }

            let center = UNUserNotificationCenter.current()
            center.removePendingNotificationRequests(withIdentifiers: [id])
            center.removeDeliveredNotifications(withIdentifiers: [id])

            return BridgeResponse.success(data: ["cancelled": true])
        }
    }

    class CancelAll: BridgeFunction {
        func execute(parameters: [String: Any]) throws -> [String: Any] {
            let center = UNUserNotificationCenter.current()
            center.removeAllPendingNotificationRequests()
            center.removeAllDeliveredNotifications()

            return BridgeResponse.success(data: ["cancelled": true])
        }
    }
}
