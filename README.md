# Local Notifications for NativePHP Mobile

Schedule, cancel, and react to local (on-device) notifications from Laravel — no Firebase or server round-trip required.

## Installation

In your NativePHP Mobile app:

```bash
composer require phnuestro/local-notifications

php artisan vendor:publish --tag=nativephp-plugins-provider
php artisan native:plugin:register phnuestro/local-notifications
php artisan native:plugin:validate
```

If you haven't tagged a stable release on GitHub yet, pin to the branch instead:

```bash
composer require phnuestro/local-notifications:dev-main
```

Working on the plugin itself alongside an app? Use a local path repo so edits are picked up without a Packagist round-trip:

```bash
composer config repositories.local-notifications path ../local-notifications
composer require phnuestro/local-notifications:@dev
```

Native code only takes effect after a full native build:

```bash
php artisan native:run android
php artisan native:run ios
```

## Usage (PHP / Livewire / Blade)

```php
use Phnuestro\LocalNotifications\Facades\LocalNotification;
use Phnuestro\LocalNotifications\Events\NotificationTapped;
use Native\Mobile\Attributes\OnNative;

// Ask for permission once, e.g. on first app launch or a settings screen.
LocalNotification::requestPermission();

// Fire in 10 seconds
LocalNotification::schedule('Hello!', 'Your first local notification', [
    'id' => 'welcome',
    'delay' => 10,
]);

// Fire at a specific time
LocalNotification::schedule('Invoice due', 'Invoice #42 is due tomorrow.', [
    'id' => 'invoice-42',
    'at' => now()->addDay(),
    'data' => ['invoice_id' => 42],
    'channelId' => 'billing',
    'channelName' => 'Billing reminders',
]);

// Cancel one, or everything
LocalNotification::cancel('invoice-42');
LocalNotification::cancelAll();

// React to taps
#[OnNative(NotificationTapped::class)]
public function handleTap($id, $data = [])
{
    // e.g. navigate based on $id / $data
}
```

## Usage (Vue / React / Inertia)

```js
import { requestPermission, schedule, cancel, cancelAll } from '#nativephp/local-notification';

await requestPermission();

await schedule('Hello!', 'Your first local notification', { id: 'welcome', delay: 10 });

await cancel('welcome');
await cancelAll();
```

## `schedule()` options

| Option        | Type              | Description                                              |
| ------------- | ----------------- | ---------------------------------------------------------- |
| `id`          | `string`          | Unique id. Auto-generated if omitted.                       |
| `delay`       | `int`             | Seconds from now to fire.                                   |
| `at`          | `int\|DateTime`   | Absolute time to fire. Takes precedence over `delay`.        |
| `data`        | `array`           | Custom payload, returned via `NotificationTapped`.           |
| `sound`       | `bool`            | Play the default notification sound. Default `true`.         |
| `badge`       | `int`             | App icon badge count (iOS).                                  |
| `channelId`   | `string`          | Android notification channel id. Default `default`.          |
| `channelName` | `string`          | Android notification channel display name.                   |

## Notes / next steps

- **Android 13+** requires `POST_NOTIFICATIONS` runtime permission — call `requestPermission()` before your first `schedule()`.
- **Exact alarms**: on Android 12+, scheduling precise-time alarms needs `SCHEDULE_EXACT_ALARM` (already declared in `nativephp.json`); the bridge falls back to an inexact alarm if the user hasn't granted it.
- **iOS delegate**: `LocalNotificationDelegate` needs to be set as `UNUserNotificationCenter.current().delegate` during app init. If you also install `nativephp/mobile-firebase` for push notifications, chain the delegates so both receive their callbacks (see that plugin's docs for the pattern).
- Repeating notifications, action buttons, and custom sounds aren't implemented yet — the bridge functions and manifest are structured so you can add them without breaking the PHP API.
- Run `php artisan native:plugin:validate` after any manifest or bridge-function change.
