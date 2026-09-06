<?php

namespace Phnuestro\LocalNotifications;

use Illuminate\Support\ServiceProvider;

class LocalNotificationServiceProvider extends ServiceProvider
{
    public function register(): void
    {
        $this->app->singleton(LocalNotification::class, fn () => new LocalNotification());
    }

    public function boot(): void
    {
        //
    }
}
