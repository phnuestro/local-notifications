<?php

namespace Phnuestro\LocalNotifications\Facades;

use Illuminate\Support\Facades\Facade;

/**
 * @method static bool requestPermission()
 * @method static string schedule(string $title, string $body, array $options = [])
 * @method static bool cancel(string $id)
 * @method static bool cancelAll()
 *
 * @see \Phnuestro\LocalNotifications\LocalNotification
 */
class LocalNotification extends Facade
{
    protected static function getFacadeAccessor(): string
    {
        return \Phnuestro\LocalNotifications\LocalNotification::class;
    }
}
