<?php

namespace Paolo\LocalNotifications;

use Illuminate\Support\Str;

class LocalNotification
{
    /**
     * Ask the user for permission to show notifications.
     * Required on Android 13+ (POST_NOTIFICATIONS) and on iOS before anything can be shown.
     */
    public function requestPermission(): bool
    {
        $result = $this->call('LocalNotification.RequestPermission');

        return (bool) ($result['granted'] ?? false);
    }

    /**
     * Schedule a local notification.
     *
     * @param  string  $title
     * @param  string  $body
     * @param  array{
     *     id?: string,
     *     delay?: int,
     *     at?: int|\DateTimeInterface,
     *     data?: array,
     *     sound?: bool|string,
     *     badge?: int,
     *     channelId?: string,
     *     channelName?: string,
     * }  $options
     * @return string The notification id (generated automatically if not supplied)
     */
    public function schedule(string $title, string $body, array $options = []): string
    {
        $id = $options['id'] ?? (string) Str::uuid();

        $at = $options['at'] ?? null;
        if ($at instanceof \DateTimeInterface) {
            $at = $at->getTimestamp();
        }

        $payload = [
            'id' => $id,
            'title' => $title,
            'body' => $body,
            'delay' => $options['delay'] ?? null,
            'at' => $at,
            'data' => $options['data'] ?? [],
            'sound' => $options['sound'] ?? true,
            'badge' => $options['badge'] ?? null,
            'channelId' => $options['channelId'] ?? 'default',
            'channelName' => $options['channelName'] ?? 'General',
        ];

        $this->call('LocalNotification.Schedule', $payload);

        return $id;
    }

    /**
     * Cancel a single pending or already-delivered notification.
     */
    public function cancel(string $id): bool
    {
        $result = $this->call('LocalNotification.Cancel', ['id' => $id]);

        return (bool) ($result['cancelled'] ?? false);
    }

    /**
     * Cancel every notification scheduled by this plugin.
     */
    public function cancelAll(): bool
    {
        $result = $this->call('LocalNotification.CancelAll');

        return (bool) ($result['cancelled'] ?? false);
    }

    /**
     * Send the bridge call and decode the response.
     */
    protected function call(string $function, array $params = []): array
    {
        if (! function_exists('nativephp_call')) {
            return [];
        }

        $response = nativephp_call($function, json_encode($params));
        $decoded = json_decode($response, true);

        return $decoded['data'] ?? [];
    }
}
