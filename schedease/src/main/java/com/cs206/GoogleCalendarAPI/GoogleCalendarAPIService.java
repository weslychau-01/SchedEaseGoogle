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
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.client.extensions.jetty.auth.oauth2.*;
import com.Encryption.*;

import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoogleCalendarAPIService {
    /**
     * Global instance of the JSON factory.
     */
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private final String TOKENS_DIRECTORY_PATH = "tokens";

    private final String APPLICATION_NAME = "SchedEase";

    @Autowired
    private UserRepository userRepository;

    /**
     * Global instance of the scopes.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json is not found.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {

        // Load client secrets.
        InputStream in = GoogleCalendarAPIService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            // // Print the absolute path of the file being searched for
            // System.out.println("File path: " +
            // getClass().getResource(CREDENTIALS_FILE_PATH));

            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setApprovalPrompt("force")
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        // Debug LocalServerReceiver Error400 zzz
        // System.out.println("LocalServerReceiver is configured to use port: " +
        // receiver.getPort());

        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        // System.out.println(credential.getRefreshToken());
        // returns an authorized Credential object.
        return credential;
    }

    
    public void getCredentials(String userId) throws Exception {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Credential credentials = getCredentials(HTTP_TRANSPORT);

            User user;
            SecretKey secretKey = EncryptionUtil.generateSecretKey();
            
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {

                user = optionalUser.get();
                // user.setCredential(credentials);
                user.setSerialisedKey(EncryptionUtil.serialiseSecretString(secretKey));
                String aToken = credentials.getAccessToken();
                String rToken = credentials.getRefreshToken();
// System.out.println(aToken);
// System.out.println(rToken);
                user.setEncryptedAccessToken(EncryptionUtil.encrypt(aToken, secretKey));
                user.setEncryptedRefreshToken(EncryptionUtil.encrypt(rToken, secretKey));
                userRepository.save(user);
            }

            // Use the credentials as needed
            return;
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            System.out.println("Failed to get credentials: " + e.getMessage());
            return;
        }
    }
    
    /*
    @GetMapping("/{userId}/getCredentials")
    public ResponseEntity<?> getCredentials(@PathVariable(value = "userId") String userId) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Credential credentials = getCredentials(HTTP_TRANSPORT);

            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                // user.setCredential(credentials);
                user.setAccessToken(credentials.getAccessToken());
                System.out.println(credentials.getRefreshToken());
                user.setRefreshToken(credentials.getRefreshToken());
                userRepository.save(user);
            }

            // Use the credentials as needed
            return ResponseEntity.ok().build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to get credentials: " + e.getMessage());
        }
    }
    */

    private Credential createCredentialForUser(String accessToken, String refreshToken, NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GoogleCalendarAPIService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Create the credential
        Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
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
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
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
    
/*
    @GetMapping("/{userId}/getEvents/{eventStartDateTime}/{eventEndDateTime}")
    public ResponseEntity<List<Event>> getEvents(
            @PathVariable(value = "userId") String userId,
            @PathVariable(value = "eventStartDateTime") String eventStartDateTime,
            @PathVariable(value = "eventEndDateTime") String eventEndDateTime)
            throws IOException, GeneralSecurityException {

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

            String accessToken = user.getAccessToken();
            String refreshToken = user.getRefreshToken();

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            credentials = createCredentialForUser(accessToken, refreshToken, HTTP_TRANSPORT);

            // Build a new authorized API client service.
            if (credentials == null) {
                getCredentials(HTTP_TRANSPORT);
            } else {
                credentials.refreshToken();
            }
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
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
                return ResponseEntity.ok().body(Collections.emptyList());
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
                return ResponseEntity.ok().body(items);
            }
        } else {
            throw new UserNotFoundException(userId);
        }
    }
     */

     

    public void test() throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credentials = getCredentials(HTTP_TRANSPORT);
        if (credentials == null) {
            getCredentials(HTTP_TRANSPORT);
        } else {
            credentials.refreshToken();
        }
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                // .setApplicationName(APPLICATION_NAME)
                .build();

        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }
        return;
    }
}
