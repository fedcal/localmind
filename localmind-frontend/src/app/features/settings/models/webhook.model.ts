export interface Webhook {
  id: string;
  name: string;
  url: string;
  eventType: string;
  active: boolean;
}

export interface WebhookRequest {
  name: string;
  url: string;
  eventType: string;
  active: boolean;
}
