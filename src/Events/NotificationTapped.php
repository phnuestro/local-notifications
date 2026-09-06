<?php

namespace Paolo\LocalNotifications\Events;

class NotificationTapped
{
    public function __construct(
        public string $id,
        public array $data = [],
    ) {}
}
