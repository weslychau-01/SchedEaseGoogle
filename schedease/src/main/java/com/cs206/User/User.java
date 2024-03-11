package com.cs206.User;

import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
   @Id
   private String id;
   private String userName;
   private String userEmail;
   private String userPassword;
   private List<String> userEventIds;
   private List<String> userMeetingIds;
   // private Credential credential;
   // private String encryptedRefreshToken;
   // private String encryptedAccessToken;
   // private String serialisedKey;
   private String refreshToken;
   private String accessToken;


}
