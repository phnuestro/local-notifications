<?php

namespace Paolo\LocalNotifications\Facades;

use Illuminate\Support\Facades\Facade;

/**
 * @method static bool requestPermission()
 * @method static string schedule(string $title, string $body, array $options = [])
 * @method static bool cancel(string $id)
 * @method static bool cancelAll()
 *
 * @see \Paolo\LocalNotifications\LocalNotification
 */
class LocalNotification extends Facade
{
    protected static function getFacadeAccessor(): string
    {
        return \Paolo\LocalNotifications\LocalNotification::class;
    }
}
