package com.cs206.GoogleCalendarAPI;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.*;

import javax.crypto.SecretKey;

import com.cs206.User.User;
import com.cs206.User.UserNotFoundException;
import com.cs206.User.UserRepository;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.*;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.api.client.extensions.jetty.auth.oauth2.*;
import com.Encryption.*;

import java.security.GeneralSecurityException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoogleCalendarAPIService {

    private final String TOKENS_DIRECTORY_PATH = "tokens";

    private final String APPLICATION_NAME = "SchedEase";

    @Autowired
    private UserRepository userRepository;

    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {

        InputStream in = GoogleCalendarAPIService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {

            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        System.out.println("1");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(GsonFactory.getDefaultInstance(),
                new InputStreamReader(in));
        System.out.println("2");
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), clientSecrets,
                Collections.singletonList(CalendarScopes.CALENDAR))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setApprovalPrompt("force")
                .setAccessType("offline")
                .build();
        System.out.println("3");
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        String URL = receiver.getRedirectUri();
        System.out.println(URL);

        // Debug LocalServerReceiver Error400 zzz
        // System.out.println("LocalServerReceiver is configured to use port: " +
        // receiver.getPort());
        System.out.println("5");
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        // System.out.println(credential.getRefreshToken());
        // returns an authorized Credential object.
        System.out.println("6");
        return credential;
    }

    private Credential createCredentialForUser(String accessToken, String refreshToken, NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GoogleCalendarAPIService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(GsonFactory.getDefaultInstance(),
                new InputStreamReader(in));

        // Create the credential
        Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientAuthentication(new ClientParametersAuthentication(
                        clientSecrets.getDetails().getClientId(), clientSecrets.getDetails().getClientSecret()))
                .setTokenServerEncodedUrl("https://oauth2.googleapis.com/token")
                .build();

        // Set the stored access and refresh tokens.
        credential.setAccessToken(accessToken);
        credential.setRefreshToken(refreshToken);

        return credential;
    }

    public List<Event> getEvents(String userId, String eventStartDateTime, String eventEndDateTime)
            throws IOException, GeneralSecurityException, Exception {

        // Load client secrets.
        InputStream in = GoogleCalendarAPIService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            // // Print the absolute path of the file being searched for
            // System.out.println("File path: " +
            // getClass().getResource(CREDENTIALS_FILE_PATH));

            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        Credential credentials;

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            SecretKey k = EncryptionUtil.getSecretKeyFromSecretString(user.getSerialisedKey());

            String accessToken = EncryptionUtil.decrypt(user.getEncryptedAccessToken(), k);
            String refreshToken = EncryptionUtil.decrypt(user.getEncryptedRefreshToken(), k);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            credentials = createCredentialForUser(accessToken, refreshToken, HTTP_TRANSPORT);

            // Build a new authorized API client service.
            if (credentials == null) {
                getCredentials(HTTP_TRANSPORT);
            } else {
                credentials.refreshToken();
            }
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), credentials)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Format the start and end date/time to RFC3339
            String startDateTimeStr = eventStartDateTime + 'Z';
            String endDateTimeStr = eventEndDateTime + 'Z';

            DateTime startDateTime = new DateTime(startDateTimeStr);
            DateTime endDateTime = new DateTime(endDateTimeStr);

            Events events = service.events().list("primary")
                    .setTimeMax(endDateTime)
                    .setTimeMin(startDateTime)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            List<Event> items = events.getItems();
            if (items.isEmpty()) {
                System.out.println("No upcoming events found.");
                return Collections.emptyList();
            } else {
                System.out.println("Upcoming events");
                for (Event event : items) {
                    DateTime start = event.getStart().getDateTime();
                    DateTime end = event.getEnd().getDateTime();
                    if (start == null) {
                        start = event.getStart().getDate();
                    }
                    if (end == null) {
                        end = event.getEnd().getDate();
                    }
                    System.out.printf("%s (%s) to (%s)\n", event.getSummary(), start, end);
                }
                return items;
            }
        } else {
            throw new UserNotFoundException(userId);
        }
    }

    public Event addEvent(String userId, Event event)
            throws IOException, GeneralSecurityException, Exception {

        // Load client secrets.
        InputStream in = GoogleCalendarAPIService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        Credential credentials;

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent() && optionalUser.get().getSerialisedKey() != null) {
            User user = optionalUser.get();
            SecretKey k = EncryptionUtil.getSecretKeyFromSecretString(user.getSerialisedKey());
            String accessToken = EncryptionUtil.decrypt(user.getEncryptedAccessToken(), k);
            String refreshToken = EncryptionUtil.decrypt(user.getEncryptedRefreshToken(), k);
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            credentials = createCredentialForUser(accessToken, refreshToken, httpTransport);

            // Build a new authorized API client service.
            if (credentials == null) {
                getCredentials(httpTransport);
            } else {
                credentials.refreshToken();
            }
            Calendar service = new Calendar.Builder(httpTransport, GsonFactory.getDefaultInstance(), credentials)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            String calendarId = "primary";
            return service.events().insert(calendarId, event).execute();
        }
        return null;
    }

    public Event buildEvent(String eventName, LocalDateTime eventStartDateTime, LocalDateTime eventEndDateTime) {
        Event event = new Event()
                .setSummary(eventName);

        DateTime startDateTime = new DateTime(eventStartDateTime.toString() + ":00.00Z");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Singapore");
        event.setStart(start);

        DateTime endDateTime = new DateTime(eventEndDateTime.toString() + ":00.00Z");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Singapore");
        event.setEnd(end);
        return event;
    }

    public void deleteEvent(String userId, Event event)
            throws IOException, GeneralSecurityException, Exception {

        // Load client secrets.
        InputStream in = GoogleCalendarAPIService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        Credential credentials;

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent() && optionalUser.get().getSerialisedKey() != null) {
            User user = optionalUser.get();
            SecretKey k = EncryptionUtil.getSecretKeyFromSecretString(user.getSerialisedKey());
            String accessToken = EncryptionUtil.decrypt(user.getEncryptedAccessToken(), k);
            String refreshToken = EncryptionUtil.decrypt(user.getEncryptedRefreshToken(), k);
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            credentials = createCredentialForUser(accessToken, refreshToken, httpTransport);

            // Build a new authorized API client service.
            if (credentials == null) {
                getCredentials(httpTransport);
            } else {
                credentials.refreshToken();
            }
            Calendar service = new Calendar.Builder(httpTransport, GsonFactory.getDefaultInstance(), credentials)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            String calendarId = "primary";
            String eventName = event.getSummary();
            System.out.println(eventName);
            List<Event> items = service.events()
                    .list(calendarId)
                    .setTimeMin(event.getStart().getDateTime())
                    .setMaxResults(100)
                    .execute()
                    .getItems();
            if (items.isEmpty()) {
                return;
            } else {
                for (Event item : items) {
                    System.out.println(item.getSummary());
                    if (item.getSummary().equals(eventName)) {
                        service.events().delete(calendarId, item.getId()).execute();
                        return;
                    }
                }
            }

        }
    }

}
