package com.localmind.infrastructure.calendar.adapter;

import com.localmind.domain.calendar.model.CalendarEvent;
import com.localmind.domain.calendar.port.out.CalendarPort;
import com.localmind.infrastructure.calendar.config.CalendarProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(name = "localmind.calendar.enabled", havingValue = "true")
@EnableConfigurationProperties(CalendarProperties.class)
public class CalDavCalendarAdapter implements CalendarPort {

    private static final Logger log = LoggerFactory.getLogger(CalDavCalendarAdapter.class);
    private static final DateTimeFormatter ICAL_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMdd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC);

    private final CalendarProperties properties;
    private final HttpClient httpClient;

    public CalDavCalendarAdapter(CalendarProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public List<CalendarEvent> listEvents(Instant from, Instant to) {
        try {
            String reportBody = buildCalendarReport(from, to);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getUrl()))
                    .method("REPORT", HttpRequest.BodyPublishers.ofString(reportBody))
                    .header("Content-Type", "application/xml; charset=utf-8")
                    .header("Depth", "1")
                    .header("Authorization", basicAuth())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return parseEventsFromResponse(response.body());
            }
            log.warn("CalDAV REPORT returned status {}", response.statusCode());
            return List.of();
        } catch (Exception e) {
            log.error("Error listing CalDAV events", e);
            return List.of();
        }
    }

    @Override
    public CalendarEvent createEvent(CalendarEvent event) {
        try {
            String eventId = event.getId() != null ? event.getId() : UUID.randomUUID().toString();
            event.setId(eventId);
            String icalBody = buildICalEvent(event);
            String eventUrl = properties.getUrl().endsWith("/")
                    ? properties.getUrl() + eventId + ".ics"
                    : properties.getUrl() + "/" + eventId + ".ics";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(eventUrl))
                    .PUT(HttpRequest.BodyPublishers.ofString(icalBody))
                    .header("Content-Type", "text/calendar; charset=utf-8")
                    .header("Authorization", basicAuth())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("CalDAV event created: {}", eventId);
                return event;
            }
            throw new RuntimeException("CalDAV PUT returned status " + response.statusCode());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error creating CalDAV event", e);
        }
    }

    @Override
    public void deleteEvent(String eventId) {
        try {
            String eventUrl = properties.getUrl().endsWith("/")
                    ? properties.getUrl() + eventId + ".ics"
                    : properties.getUrl() + "/" + eventId + ".ics";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(eventUrl))
                    .DELETE()
                    .header("Authorization", basicAuth())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("CalDAV event deleted: {}", eventId);
            } else {
                log.warn("CalDAV DELETE returned status {} for event {}", response.statusCode(), eventId);
            }
        } catch (Exception e) {
            log.error("Error deleting CalDAV event: {}", eventId, e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getUrl()))
                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                    .header("Authorization", basicAuth())
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() < 400;
        } catch (Exception e) {
            log.debug("CalDAV server not available: {}", e.getMessage());
            return false;
        }
    }

    private String basicAuth() {
        String credentials = properties.getUsername() + ":" + properties.getPassword();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    private String buildCalendarReport(Instant from, Instant to) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<C:calendar-query xmlns:D=\"DAV:\" xmlns:C=\"urn:ietf:params:xml:ns:caldav\">\n" +
                "  <D:prop><D:getetag/><C:calendar-data/></D:prop>\n" +
                "  <C:filter>\n" +
                "    <C:comp-filter name=\"VCALENDAR\">\n" +
                "      <C:comp-filter name=\"VEVENT\">\n" +
                "        <C:time-range start=\"" + ICAL_FORMAT.format(from) +
                "\" end=\"" + ICAL_FORMAT.format(to) + "\"/>\n" +
                "      </C:comp-filter>\n" +
                "    </C:comp-filter>\n" +
                "  </C:filter>\n" +
                "</C:calendar-query>";
    }

    private String buildICalEvent(CalendarEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//LocalMind//CalDAV//EN\r\n");
        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append(event.getId()).append("\r\n");
        sb.append("DTSTART:").append(ICAL_FORMAT.format(event.getStartTime())).append("\r\n");
        sb.append("DTEND:").append(ICAL_FORMAT.format(event.getEndTime())).append("\r\n");
        sb.append("SUMMARY:").append(escapeIcalText(event.getTitle())).append("\r\n");
        if (event.getDescription() != null) {
            sb.append("DESCRIPTION:").append(escapeIcalText(event.getDescription())).append("\r\n");
        }
        if (event.getLocation() != null) {
            sb.append("LOCATION:").append(escapeIcalText(event.getLocation())).append("\r\n");
        }
        if (event.getAttendees() != null) {
            for (String attendee : event.getAttendees()) {
                sb.append("ATTENDEE:mailto:").append(attendee).append("\r\n");
            }
        }
        sb.append("END:VEVENT\r\n");
        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private String escapeIcalText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }

    List<CalendarEvent> parseEventsFromResponse(String responseBody) {
        List<CalendarEvent> events = new ArrayList<>();
        Pattern veventPattern = Pattern.compile("BEGIN:VEVENT(.*?)END:VEVENT", Pattern.DOTALL);
        Matcher matcher = veventPattern.matcher(responseBody);

        while (matcher.find()) {
            String vevent = matcher.group(1);
            CalendarEvent.CalendarEventBuilder builder = CalendarEvent.builder();

            extractProperty(vevent, "UID").ifPresent(builder::id);
            extractProperty(vevent, "SUMMARY").ifPresent(builder::title);
            extractProperty(vevent, "DESCRIPTION").ifPresent(builder::description);
            extractProperty(vevent, "LOCATION").ifPresent(builder::location);
            extractProperty(vevent, "DTSTART").ifPresent(dt -> builder.startTime(parseICalDate(dt)));
            extractProperty(vevent, "DTEND").ifPresent(dt -> builder.endTime(parseICalDate(dt)));

            List<String> attendees = new ArrayList<>();
            Pattern attendeePattern = Pattern.compile("ATTENDEE:mailto:(.+?)\\r?\\n");
            Matcher attendeeMatcher = attendeePattern.matcher(vevent);
            while (attendeeMatcher.find()) {
                attendees.add(attendeeMatcher.group(1).trim());
            }
            if (!attendees.isEmpty()) {
                builder.attendees(attendees);
            }

            events.add(builder.build());
        }
        return events;
    }

    private java.util.Optional<String> extractProperty(String vevent, String property) {
        Pattern pattern = Pattern.compile(property + "(?:;[^:]*)?:(.+?)\\r?\\n");
        Matcher matcher = pattern.matcher(vevent);
        if (matcher.find()) {
            return java.util.Optional.of(unescapeIcalText(matcher.group(1).trim()));
        }
        return java.util.Optional.empty();
    }

    private String unescapeIcalText(String text) {
        return text.replace("\\n", "\n")
                .replace("\\,", ",")
                .replace("\\;", ";")
                .replace("\\\\", "\\");
    }

    private Instant parseICalDate(String dateStr) {
        try {
            return ICAL_FORMAT.parse(dateStr, Instant::from);
        } catch (Exception e) {
            log.warn("Could not parse iCal date: {}", dateStr);
            return Instant.now();
        }
    }
}
