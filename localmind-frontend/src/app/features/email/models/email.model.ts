export interface EmailMessage {
  id: string;
  from: string;
  to: string[];
  subject: string;
  body: string;
  receivedAt: string;
  read: boolean;
}

export interface SendEmailRequest {
  to: string[];
  subject: string;
  body: string;
}

export interface DraftReplyResponse {
  draftBody: string;
}
