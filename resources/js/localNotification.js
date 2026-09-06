const baseUrl = '/_native/api/call';

async function bridgeCall(method, params = {}) {
    const response = await fetch(baseUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ method, params }),
    });

    return response.json();
}

export async function requestPermission() {
    const result = await bridgeCall('LocalNotification.RequestPermission');
    return result?.data?.granted ?? false;
}

export async function schedule(title, body, options = {}) {
    const result = await bridgeCall('LocalNotification.Schedule', {
        id: options.id,
        title,
        body,
        delay: options.delay,
        at: options.at,
        data: options.data ?? {},
        sound: options.sound ?? true,
        badge: options.badge,
        channelId: options.channelId ?? 'default',
        channelName: options.channelName ?? 'General',
    });

    return result?.data?.id;
}

export async function cancel(id) {
    const result = await bridgeCall('LocalNotification.Cancel', { id });
    return result?.data?.cancelled ?? false;
}

export async function cancelAll() {
    const result = await bridgeCall('LocalNotification.CancelAll');
    return result?.data?.cancelled ?? false;
}
